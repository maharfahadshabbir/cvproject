package com.example.cvmaker.fragments.profiledetail

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cvmaker.R
import com.example.cvmaker.databinding.FragmentReferencesDetailBinding
import com.example.cvmaker.fragments.profiledetail.adapters.ReferencesAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.model.workingmodels.ReferenceModel
import com.example.cvmaker.utils.showToastSafe
import com.example.cvmaker.viewmodels.SharedViewModel

class ReferencesDetailFragment : Fragment() {

    private lateinit var binding: FragmentReferencesDetailBinding
    private lateinit var adapter: ReferencesAdapter

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    /** The index inside cvModelRequestDb.referenceList currently being edited in the big block */
    private var currentIndex = 0

    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReferencesDetailBinding.inflate(inflater, container, false)
        configureBackPress()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupKeyboardPadding(view)
        ensureListHasAtLeastOne()
        bindCurrentItemToViews()
        attachLiveFieldWriters()
        setupClicks()
        setupOptionalRecycler()

        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    /* ---------------- Back press ---------------- */

    private fun configureBackPress() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = backPressed()
        }
        onBackPressedCallback?.let {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it)
        }
    }

    private fun backPressed() {
        // On back, just leave list as-is (user may be in the middle of editing)
        findNavController().popBackStack()
    }

    /* ---------------- Keyboard padding ---------------- */

    private fun setupKeyboardPadding(root: View) {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = root.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                binding.scrollReferences.setPadding(
                    binding.scrollReferences.paddingLeft,
                    binding.scrollReferences.paddingTop,
                    binding.scrollReferences.paddingRight,
                    keypadHeight
                )
            }
        }
    }

    /* ---------------- Data helpers ---------------- */

    private fun ensureListHasAtLeastOne() {
        val list = sharedViewModel.cvModelRequestDb.referenceList
        if (list.isEmpty()) list.add(ReferenceModel())
        // If we navigated back from somewhere and index > last, fix it.
        if (currentIndex !in list.indices) currentIndex = list.lastIndex
    }

    private fun currentItem(): ReferenceModel =
        sharedViewModel.cvModelRequestDb.referenceList[currentIndex]

    private fun bindCurrentItemToViews() = with(binding) {
        val item = currentItem()
        etRefName.setText(item.name.orEmpty())
        etRefPhone.setText(item.phone.orEmpty())
        etRefEmail.setText(item.email.orEmpty())
        etRefDesignation.setText(item.designation.orEmpty())
        etRefCompany.setText(item.companyName.orEmpty())

        if (item.name.isNullOrEmpty()) etRefName.requestFocus()
    }

    /** As-you-type, write back to the model */
    private fun attachLiveFieldWriters() = with(binding) {
        ViewUtils.applyCapitalizeFilter(etRefName)
        ViewUtils.applyCapitalizeFilter(etRefDesignation)
        ViewUtils.applyCapitalizeFilter(etRefCompany)

        etRefName.addTextChangedListener {
            currentItem().name = it?.toString()?.takeIf { s -> s.isNotBlank() }
        }

        etRefDesignation.addTextChangedListener {
            currentItem().designation = it?.toString()?.takeIf { s -> s.isNotBlank() }
        }

        etRefCompany.addTextChangedListener {
            currentItem().companyName = it?.toString()?.takeIf { s -> s.isNotBlank() }
        }

        etRefEmail.addTextChangedListener { s ->
            val text = s?.toString()?.trim().orEmpty()

            if (text.isEmpty()) {
                currentItem().email = null
                return@addTextChangedListener
            }

            // cap length at 34 like adapter
            val trimmed = text.take(34)
            if (trimmed != text) {
                etRefEmail.setText(trimmed)
                etRefEmail.setSelection(trimmed.length)
            }
            currentItem().email = trimmed
        }

        etRefPhone.addTextChangedListener { s ->
            val text = s?.toString()?.trim().orEmpty()

            if (text.isEmpty()) {
                currentItem().phone = null
                return@addTextChangedListener
            }

            if (text.length > 14) {
                val trimmed = text.take(14)
                etRefPhone.setText(trimmed)
                etRefPhone.setSelection(trimmed.length)
                currentItem().phone = trimmed
            } else {
                currentItem().phone = text
            }
        }
    }

    /* ---------------- Clicks ---------------- */

    private fun setupClicks() = with(binding) {
        backButton.setOnClickListener { backPressed() }

        addMoreReferences.setOnClickListener {
            // 1) Validate current before moving on
            if (!validateCurrent()) return@setOnClickListener

            // 2) Add new item, move index, bind fresh UI
            val list = sharedViewModel.cvModelRequestDb.referenceList
            list.add(ReferenceModel())
            currentIndex = list.lastIndex
            bindCurrentItemToViews()
        }

        btnSave.setOnClickListener {
            // Validate all non-empty references
            if (!validateAll()) return@setOnClickListener

            // Drop fully empty references before saving
            val list = sharedViewModel.cvModelRequestDb.referenceList
            val cleaned = list.filter { !isEmpty(it) }.toMutableList()

            if (cleaned.isEmpty()) {
                showToastSafe("Please add at least one reference.")
                return@setOnClickListener
            }

            sharedViewModel.cvModelRequestDb.referenceList = cleaned
            showToastSafe(getString(R.string.saved_successfully))
            findNavController().popBackStack()
        }
    }

    /* ---------------- Validation ---------------- */

    private fun validateCurrent(): Boolean {
        val item = currentItem()

        // if user hasn't entered anything at all, let them move on
        if (isEmpty(item)) return true

        val email = item.email?.trim().orEmpty()
        val phone = item.phone?.trim().orEmpty()

        // email required & valid
        if (email.isEmpty()) {
            binding.etRefEmail.requestFocus()
            return false
        }
        if (!email.matches(ViewUtils.emailPattern.toRegex())) {
            binding.etRefEmail.requestFocus()
            return false
        }

        // phone required & basic validation
        if (phone.isEmpty()) {
            binding.etRefPhone.requestFocus()
            return false
        }
        val phoneOk = "^[+]?[0-9]{1,14}$".toRegex().matches(phone)
        if (!phoneOk) {
            binding.etRefPhone.requestFocus()
            return false
        }

        // Duplicate email across list (excluding current index)
        if (isDuplicateEmail(email, excludeIndex = currentIndex)) {
            showToastSafe(getString(R.string.dublicate_email_founded))
            return false
        }

        return true
    }

    private fun validateAll(): Boolean {
        val list = sharedViewModel.cvModelRequestDb.referenceList

        // At least one non-empty reference
        val hasNonEmpty = list.any { !isEmpty(it) }
        if (!hasNonEmpty) {
            showToastSafe("Please add at least one reference.")
            return false
        }

        // Validate only non-empty items
        list.forEachIndexed { index, ref ->
            if (isEmpty(ref)) return@forEachIndexed

            val email = ref.email?.trim().orEmpty()
            val phone = ref.phone?.trim().orEmpty()

            if (email.isEmpty() || !email.matches(ViewUtils.emailPattern.toRegex())) {
                currentIndex = index
                bindCurrentItemToViews()
                binding.etRefEmail.requestFocus()
                return false
            }

            if (phone.isEmpty() || !"^[+]?[0-9]{1,14}$".toRegex().matches(phone)) {
                currentIndex = index
                bindCurrentItemToViews()
                binding.etRefPhone.requestFocus()
                return false
            }
        }

        // Duplicate emails across all non-empty entries
        val emails = list
            .filter { !isEmpty(it) }
            .mapNotNull { it.email?.trim()?.lowercase() }
            .filter { it.isNotEmpty() }

        if (emails.size != emails.toSet().size) {
            showToastSafe(getString(R.string.dublicate_email_founded))
            return false
        }

        return true
    }

    private fun isDuplicateEmail(email: String, excludeIndex: Int): Boolean {
        val list = sharedViewModel.cvModelRequestDb.referenceList
        val lower = email.lowercase()
        list.forEachIndexed { idx, ref ->
            if (idx != excludeIndex && ref.email?.lowercase() == lower) return true
        }
        return false
    }

    private fun isEmpty(ref: ReferenceModel): Boolean {
        return ref.name.isNullOrBlank() &&
                ref.designation.isNullOrBlank() &&
                ref.companyName.isNullOrBlank() &&
                ref.email.isNullOrBlank() &&
                ref.phone.isNullOrBlank()
    }

    /* ---------------- Optional list (kept wired for later) ---------------- */

    private fun setupOptionalRecycler() = with(binding) {
        referencesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReferencesAdapter()
        referencesRecyclerView.adapter = adapter

        // Seed adapter (in case you toggle visibility later)
        adapter.submitList(sharedViewModel.cvModelRequestDb.referenceList.toList())

        adapter.setOnEditTextCompleteListener(object :
            ReferencesAdapter.OnEditTextCompleteListener {
            override fun onReferenceNameTextChange(position: Int, text: String) {
                if (position in sharedViewModel.cvModelRequestDb.referenceList.indices) {
                    sharedViewModel.cvModelRequestDb.referenceList[position].name =
                        text.ifBlank { null }
                }
            }

            override fun onJobTitleTextChange(position: Int, text: String) {
                if (position in sharedViewModel.cvModelRequestDb.referenceList.indices) {
                    sharedViewModel.cvModelRequestDb.referenceList[position].designation =
                        text.ifBlank { null }
                }
            }

            override fun onCompanyNameTextChange(position: Int, text: String) {
                if (position in sharedViewModel.cvModelRequestDb.referenceList.indices) {
                    sharedViewModel.cvModelRequestDb.referenceList[position].companyName =
                        text.ifBlank { null }
                }
            }

            override fun onEmailTextChange(position: Int, text: String) {
                if (position in sharedViewModel.cvModelRequestDb.referenceList.indices) {
                    val list = sharedViewModel.cvModelRequestDb.referenceList
                    val dup = list
                        .mapIndexed { i, r -> i to (r.email?.trim().orEmpty()) }
                        .any { (i, e) -> i != position && e.equals(text, true) && e.isNotEmpty() }

                    ViewUtils.error =
                        if (dup) getString(R.string.dublicate_email_founded) else ""

                    if (!dup) {
                        list[position].email = text.ifBlank { null }
                    }
                }
            }

            override fun onPhoneTextChange(position: Int, text: String) {
                if (position in sharedViewModel.cvModelRequestDb.referenceList.indices) {
                    sharedViewModel.cvModelRequestDb.referenceList[position].phone =
                        text.ifBlank { null }
                }
            }

            override fun requestFocus(editText: EditText) {
                editText.requestFocus()
            }

            override fun onRemoveItem(position: Int, itemRemoved: () -> Unit) {
                val list = sharedViewModel.cvModelRequestDb.referenceList
                if (position in list.indices) {
                    list.removeAt(position)
                    adapter.submitList(list.toList())
                    itemRemoved.invoke()

                    // If currentIndex is now out-of-range, fix it
                    if (currentIndex !in list.indices) {
                        if (list.isEmpty()) {
                            list.add(ReferenceModel())
                        }
                        currentIndex = list.lastIndex
                        bindCurrentItemToViews()
                    }
                }
            }
        })
    }
}
