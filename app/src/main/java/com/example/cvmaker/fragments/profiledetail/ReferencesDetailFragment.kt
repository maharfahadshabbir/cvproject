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

    // If you later want to show the list, this adapter already supports ReferenceModel.
    private lateinit var adapter: ReferencesAdapter

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    /** The index inside cvModelRequestDb.referenceList currently being edited in the big block */
    private var currentIndex = 0

    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
            } else {
                // no-op: keep default padding
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
        // Clear inline errors

        // Focus name if empty
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
            currentItem().email = text.ifBlank { null }

            // simple inline validation + length cap 34 like your adapter
            if (text.isEmpty()) {
//                emailErrorText.text = null
            } else {
                val trimmed = text.substring(0, 34)
                etRefEmail.setText(trimmed)
                etRefEmail.setSelection(trimmed.length)
                currentItem().email = trimmed
            }
        }

        etRefPhone.addTextChangedListener { s ->
            val text = s?.toString()?.trim().orEmpty()
            currentItem().phone = text.ifBlank { null }

            if (text.isEmpty()) {
//                do nothing
            } else if (text.length > 14) {
                val trimmed = text.substring(0, 14)
                etRefPhone.setText(trimmed)
                etRefPhone.setSelection(trimmed.length)
                currentItem().phone = trimmed
            }
        }
    }

    /* ---------------- Clicks ---------------- */

    private fun setupClicks() = with(binding) {
        backButton.setOnClickListener { backPressed() }

        // Preview optional:
        // previewCv.setOnClickListener { previewCv(sharedViewModel, R.id.fragmentPreviewApi) }

        addMoreReferences.setOnClickListener {
            // 1) Validate current before moving on
            if (!validateCurrent()) return@setOnClickListener
            // 2) "Close" previous (we already bound it to list)
            // 3) Add new item, move index, bind fresh UI
            val list = sharedViewModel.cvModelRequestDb.referenceList
            list.add(ReferenceModel())
            currentIndex = list.lastIndex
            bindCurrentItemToViews()
        }

        binding.btnSave.setOnClickListener {
            // Save = validate all
            if (!validateAll()) return@setOnClickListener
            // Everything is already in ViewModel list; navigate or show success
            showToastSafe(getString(R.string.saved_successfully))
            // Navigate if needed:
            // findNavController().navigate(R.id.customHomeFragment)
        }
    }

    /* ---------------- Validation ---------------- */

    private fun validateCurrent(): Boolean {
        val item = currentItem()

        // Email + phone required
        val email = item.email?.trim().orEmpty()
        val phone = item.phone?.trim().orEmpty()

        // inline errors
        if (email.isEmpty()) {
            binding.etRefEmail.requestFocus()
            return false
        }
        if (!email.matches(ViewUtils.emailPattern.toRegex())) {
            binding.etRefEmail.requestFocus()
            return false
        }
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
        // passed

        return true
    }

    private fun validateAll(): Boolean {
        val list = sharedViewModel.cvModelRequestDb.referenceList

        // At least one
        if (list.isEmpty()) {
            return false
        }

        // Email + phone with simple validation on each
        list.forEachIndexed { index, ref ->
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

        // Duplicate emails across all
        val emails = list.mapNotNull { it.email?.trim()?.lowercase() }.filter { it.isNotEmpty() }
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

    /* ---------------- Optional list (kept wired for later) ---------------- */

    private fun setupOptionalRecycler() = with(binding) {
        // If/when you decide to show the list; currently RecyclerView is GONE in XML
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
                    // prevent duplicates here too
                    val dup = sharedViewModel.cvModelRequestDb.referenceList
                        .mapIndexed { i, r -> i to (r.email?.trim().orEmpty()) }
                        .any { (i, e) -> i != position && e.equals(text, true) && e.isNotEmpty() }
                    ViewUtils.error = if (dup) getString(R.string.dublicate_email_founded) else ""
                    if (!dup) {
                        sharedViewModel.cvModelRequestDb.referenceList[position].email =
                            text.ifBlank { null }
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
                // Optional remove handler if you unhide the list UI
                if (position in sharedViewModel.cvModelRequestDb.referenceList.indices) {
                    sharedViewModel.cvModelRequestDb.referenceList.removeAt(position)
                    adapter.submitList(sharedViewModel.cvModelRequestDb.referenceList.toList())
                    itemRemoved.invoke()
                    // If currentIndex is now out-of-range, pull it back
                    if (currentIndex !in sharedViewModel.cvModelRequestDb.referenceList.indices) {
                        currentIndex =
                            (sharedViewModel.cvModelRequestDb.referenceList.lastIndex).coerceAtLeast(0)
                        ensureListHasAtLeastOne()
                        bindCurrentItemToViews()
                    }
                }
            }
        })
    }
}
