package com.example.cvmaker.fragments.profiledetail

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cvmaker.R
import com.example.cvmaker.activities.MainActivity
import com.example.cvmaker.databinding.FragmentSkillsDetailsBinding
import com.example.cvmaker.fragments.profiledetail.adapters.SkillAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.workingmodels.SkillsModel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel

class SkillsDetailsFragment : Fragment() {

    private lateinit var binding: FragmentSkillsDetailsBinding
    private var adapter: SkillAdapter? = null
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var onBackPressedCallback: OnBackPressedCallback? = null
    private var isSkillNameAnalyticSent = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSkillsDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureBackPress()
        initListener()

        // keyboard padding behavior
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                onKeyboardShown(keypadHeight)
            } else {
                onKeyboardHidden()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    /* -------------------- Back press -------------------- */

    private fun backPressed() {
        removeIncompleteItems()
        findNavController().popBackStack()
    }

    private fun configureBackPress() {
        tryCatch {
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = backPressed()
            }
            onBackPressedCallback?.let {
                getViewLifecycleOwnerOrNull()?.let { owner ->
                    activity?.onBackPressedDispatcher?.addCallback(owner, it)
                }
            }
        }
    }

    /* -------------------- Keyboard paddings -------------------- */

    private fun onKeyboardShown(keyboardHeight: Int) {
        binding.scrollSkills.setPadding(
            binding.scrollSkills.paddingLeft,
            binding.scrollSkills.paddingTop,
            binding.scrollSkills.paddingRight,
            keyboardHeight
        )
    }

    private fun onKeyboardHidden() {
        binding.scrollSkills.setPadding(
            binding.scrollSkills.paddingLeft,
            binding.scrollSkills.paddingTop,
            binding.scrollSkills.paddingRight,
            0
        )
    }

    /* -------------------- Init -------------------- */

    private fun initListener() {
        tryCatch {
            // we show RecyclerView and hide the static single block
            binding.skillRecyclerView.visibility = View.VISIBLE
            binding.skillContainer.visibility = View.GONE

            adapterListener()
            populateFromDb()
            clickListener()
            binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
        }
    }

    private fun clickListener() = with(binding) {
        backButton.setOnClickListener { backPressed() }

        addMoreSkill.setOnClickListener {
            addNewSkillItem()
        }

        // previewCv click left as needed
        previewCv.setOnClickListener {
            // preview if you need
        }

        btnSave.setOnClickListener {
            if (!validateFields()) return@setOnClickListener
            // All changes are already in cvModelRequestDb.skillsList
            findNavController().navigateUp()
            Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
        }
    }

    /* -------------------- Adapter -------------------- */

    private fun adapterListener() {
        tryCatch {
            context?.let { ctx ->
                binding.skillRecyclerView.layoutManager = LinearLayoutManager(ctx)
                adapter = SkillAdapter().also { binding.skillRecyclerView.adapter = it }

                adapter?.setOnEditTextCompleteListener(object :
                    SkillAdapter.OnEditTextCompleteListener {

                    override fun requestFocusForNewItem(editText: EditText) {
                        editText.requestFocus()
                    }

                    override fun onSkillNameTextChange(position: Int, text: String) {
                        tryCatch {
                            if (text.isNotEmpty() && !isSkillNameAnalyticSent) {
                                (activity as? MainActivity)?.let { isSkillNameAnalyticSent = true }
                            }
                            updateModel(position) { m ->
                                m.skillName = text.ifBlank { null }
                            }
                        }
                    }

                    override fun onRatingChanged(position: Int, rating: Int) {
                        tryCatch {
                            updateModel(position) { m -> m.skillLevel = rating }
                        }
                    }

                    override fun onItemRemoved(position: Int) {
                        showRemoveItemBottomSheet(position)
                    }
                })
            }
        }
    }

    /* -------------------- DB <-> UI -------------------- */

    private fun populateFromDb() = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.skillsList
        if (db.isEmpty()) {
            db.add(SkillsModel()) // seed one
        }
        adapter?.submitList(db.toList())
        // expand last one by default
        adapter?.expandOnly(db.lastIndex.coerceAtLeast(0))
    }

    private fun showRemoveItemBottomSheet(position: Int) {
        val sheet = RemoveItemBottomSheet { removeItemFromDb(position) }
        sheet.show(parentFragmentManager, "RemoveItemBottomSheet")
    }

    private fun removeItemFromDb(position: Int) = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.skillsList
        if (position !in db.indices) return@tryCatch

        db.removeAt(position)

        if (db.isEmpty()) {
            findNavController().popBackStack()
            return@tryCatch
        }

        binding.scrollSkills.isEnabled = false
        binding.scrollSkills.clearFocus()

        adapter?.submitList(db.toList())
        // expand a sensible row after deletion
        val expandIndex = position.coerceAtMost(db.lastIndex)
        adapter?.expandOnly(expandIndex)

        binding.scrollSkills.post { binding.scrollSkills.isEnabled = true }
    }

    /* -------------------- Add / Update -------------------- */

    private fun addNewSkillItem() = tryCatch {
        // Add only if current are valid
        if (validateFields()) {
            val db = sharedViewModel.cvModelRequestDb.skillsList
            db.add(SkillsModel()) // default empty; rating 0 until user sets

            adapter?.submitList(db.toList())
            val newIndex = db.lastIndex
            binding.skillRecyclerView.scrollToPosition(newIndex)
            adapter?.expandOnly(newIndex) // collapse previous, open new
        }
    }

    private inline fun updateModel(index: Int, update: (SkillsModel) -> Unit) {
        val db = sharedViewModel.cvModelRequestDb.skillsList
        if (index in db.indices) update(db[index])
    }

    /* -------------------- Validation / Cleanup -------------------- */

    private fun validateFields(): Boolean {
        val list = sharedViewModel.cvModelRequestDb.skillsList
        val ok = list.all { m ->
            val name = m.skillName?.trim().orEmpty()
            val lvl = m.skillLevel ?: 0
            name.isNotEmpty() && lvl in 1..5
        }
        if (!ok) {
            Toast.makeText(
                requireContext(),
                "Please enter a skill name and select 1–5 stars for each skill.",
                Toast.LENGTH_SHORT
            ).show()
        }
        return ok
    }

    private fun removeIncompleteItems() = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.skillsList
        if (db.isEmpty()) return@tryCatch

        val complete = db.filter { isModelComplete(it) }.toMutableList()
        val partialCount = db.count { !isModelEmpty(it) && !isModelComplete(it) }

        sharedViewModel.cvModelRequestDb.skillsList = complete

        if (partialCount > 0) {
            Toast.makeText(
                requireContext(),
                "Removed $partialCount incomplete skill item(s).",
                Toast.LENGTH_SHORT
            ).show()
        }

        adapter?.submitList(complete.toList())
        if (complete.isNotEmpty()) adapter?.expandOnly(complete.lastIndex)
    }

    private fun isModelComplete(m: SkillsModel): Boolean {
        val name = m.skillName?.trim().orEmpty()
        val lvl = m.skillLevel ?: 0
        return name.isNotEmpty() && lvl in 1..5
    }

    private fun isModelEmpty(m: SkillsModel): Boolean {
        return (m.skillName.isNullOrBlank() && (m.skillLevel == null || m.skillLevel == 0))
    }
}
