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
import com.example.cvmaker.databinding.FragmentEducationDetailBinding
import com.example.cvmaker.fragments.profiledetail.adapters.EducationAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.workingmodels.EducationModel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class EducationDetailFragment : Fragment() {

    private lateinit var binding: FragmentEducationDetailBinding
    private var adapter: EducationAdapter? = null
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var onBackPressedCallback: OnBackPressedCallback? = null
    private var isInstituteEntryAnalyticSent = false
    private var isCourseAnalyticSent = false
    private var isGradeAnalyticSent = false

    private val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val uiFmt = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEducationDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        initUi()
        initAdapter()
        populateFromDb()
        initClicks()

        // keyboard padding
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screen = view.rootView.height
            val keyboard = screen - rect.bottom
            if (keyboard > screen * 0.15) {
                binding.scrollEducation.setPadding(
                    binding.scrollEducation.paddingLeft,
                    binding.scrollEducation.paddingTop,
                    binding.scrollEducation.paddingRight,
                    keyboard
                )
            } else {
                binding.scrollEducation.setPadding(
                    binding.scrollEducation.paddingLeft,
                    binding.scrollEducation.paddingTop,
                    binding.scrollEducation.paddingRight,
                    0
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    /* -------------------- UI -------------------- */

    private fun initUi() = tryCatch {
        // Use RecyclerView version; hide the static single block if present in XML
        binding.educationRecyclerView.visibility = View.VISIBLE
        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    /* -------------------- Adapter -------------------- */

    private fun initAdapter() = tryCatch {
        context?.let { ctx ->
            binding.educationRecyclerView.layoutManager = LinearLayoutManager(ctx)
            adapter = EducationAdapter().also { binding.educationRecyclerView.adapter = it }

            adapter?.setOnEditTextCompleteListener(object :
                EducationAdapter.OnEditTextCompleteListener {

                override fun requestFocusForNewItem(editText: EditText) {
                    editText.requestFocus()
                }

                override fun removeItem(position: Int) {
                    showRemoveItemBottomSheet(position)
                }

                override fun onInstituteTextChange(position: Int, text: String) {
                    if (text.isNotEmpty() && !isInstituteEntryAnalyticSent) {
                        (activity as? MainActivity)?.let { isInstituteEntryAnalyticSent = true }
                    }
                    updateModel(position) { it.institute = text.ifBlank { null } }
                }

                override fun onCourseTextChange(position: Int, text: String) {
                    if (text.isNotEmpty() && !isCourseAnalyticSent) {
                        (activity as? MainActivity)?.let { isCourseAnalyticSent = true }
                    }
                    updateModel(position) { it.course = text.ifBlank { null } }
                }

                override fun onGradeTextChange(position: Int, text: String) {
                    if (text.isNotEmpty() && !isGradeAnalyticSent) {
                        (activity as? MainActivity)?.let { isGradeAnalyticSent = true }
                    }
                    updateModel(position) { it.grade = text.ifBlank { null } }
                }

                override fun onStartDateUiChange(position: Int, uiDate: String) {
                    updateModel(position) { m -> m.startDate = uiToIso(uiDate) }
                }

                override fun onEndDateUiChange(position: Int, uiDate: String) {
                    updateModel(position) { m ->
                        if (uiDate.equals(getString(R.string.present), ignoreCase = true)) {
                            m.isCurrentStudent = true
                            m.endDate = null
                        } else {
                            m.isCurrentStudent = false
                            m.endDate = uiToIso(uiDate)
                        }
                    }
                }

                override fun onCheckBoxStateChanged(position: Int, isChecked: Boolean) {
                    updateModel(position) { m ->
                        m.isCurrentStudent = isChecked
                        if (isChecked) m.endDate = null
                    }
                }
            })
        }
    }

    /* -------------------- DB -> UI -------------------- */

    private fun populateFromDb() = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.educationList
        if (db.isEmpty()) db.add(EducationModel())

        // Submit copy so DiffUtil can work properly
        adapter?.submitList(db.toList())
        // expand last one
        adapter?.expandOnly(db.lastIndex.coerceAtLeast(0))
    }

    /* -------------------- Clicks -------------------- */

    private fun initClicks() = with(binding) {
        backButton.setOnClickListener { backPressed() }

        addMore.setOnClickListener { addNewEducationItem() }

        previewCv.setOnClickListener {
            // hook your preview here if needed
        }

        addEducation.setOnClickListener {
            if (!validateFields()) return@setOnClickListener
            if (!validateDates()) return@setOnClickListener
            findNavController().navigateUp()
            Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
        }
    }

    /* -------------------- Add / Remove -------------------- */

    private fun addNewEducationItem() = tryCatch {
        if (validateFields() && validateDates()) {
            val db = sharedViewModel.cvModelRequestDb.educationList
            db.add(EducationModel())
            adapter?.submitList(db.toList())
            val newIndex = db.lastIndex
            binding.educationRecyclerView.scrollToPosition(newIndex)
            adapter?.expandOnly(newIndex) // close previous, open new
        }
    }

    private fun showRemoveItemBottomSheet(position: Int) {
        val sheet = RemoveItemBottomSheet { removeItem(position) }
        sheet.show(parentFragmentManager, "RemoveEducationItem")
    }

    private fun removeItem(position: Int) = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.educationList
        if (position !in db.indices) return@tryCatch

        db.removeAt(position)
        if (db.isEmpty()) {
            findNavController().popBackStack()
            return@tryCatch
        }

        binding.scrollEducation.isEnabled = false
        binding.scrollEducation.clearFocus()

        adapter?.submitList(db.toList())
        val expandIndex = position.coerceAtMost(db.lastIndex)
        adapter?.expandOnly(expandIndex)

        binding.scrollEducation.post { binding.scrollEducation.isEnabled = true }
    }

    /* -------------------- Validation -------------------- */

    private fun validateFields(): Boolean {
        val list = sharedViewModel.cvModelRequestDb.educationList
        val ok = list.all { m ->
            val inst = m.institute?.trim().orEmpty()
            val course = m.course?.trim().orEmpty()
            val grade = m.grade?.trim().orEmpty()
            val start = m.startDate?.trim()
            val end = m.endDate?.trim()
            inst.isNotEmpty() &&
                    course.isNotEmpty() &&
                    grade.isNotEmpty() &&
                    !start.isNullOrEmpty() &&
                    (!end.isNullOrEmpty() || m.isCurrentStudent)
        }
        if (!ok) {
            Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show()
        }
        return ok
    }

    private fun validateDates(): Boolean {
        val list = sharedViewModel.cvModelRequestDb.educationList
        for (m in list) {
            val s = m.startDate
            val e = m.endDate
            if (!m.isCurrentStudent && !s.isNullOrEmpty() && !e.isNullOrEmpty()) {
                try {
                    val sd = isoFmt.parse(s)
                    val ed = isoFmt.parse(e)
                    if (sd != null && ed != null && ed.before(sd)) {
                        Toast.makeText(
                            requireContext(),
                            "End date cannot be before the start date.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return false
                    }
                } catch (_: Exception) {
                    return false
                }
            }
        }
        return true
    }

    /* -------------------- Helpers -------------------- */

    private inline fun updateModel(index: Int, update: (EducationModel) -> Unit) {
        val db = sharedViewModel.cvModelRequestDb.educationList
        if (index in db.indices) update(db[index])
    }

    private fun backPressed() {
        removeIncompleteEducationItems()
        findNavController().popBackStack()
    }

    private fun configureBackPress() = tryCatch {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = backPressed()
        }
        onBackPressedCallback?.let { cb ->
            getViewLifecycleOwnerOrNull()?.let { owner ->
                activity?.onBackPressedDispatcher?.addCallback(owner, cb)
            }
        }
    }

    private fun uiToIso(ui: String?): String? {
        if (ui.isNullOrBlank() || ui.equals(getString(R.string.present), true)) return null
        return try {
            // normalize single-digit month/day
            val parts = ui.split("/")
            val mm = parts.getOrNull(0)?.padStart(2, '0') ?: return null
            val dd = parts.getOrNull(1)?.padStart(2, '0') ?: return null
            val yy = parts.getOrNull(2) ?: return null
            val normalized = "$mm/$dd/$yy"
            val date = uiFmt.parse(normalized) ?: return null
            isoFmt.format(date)
        } catch (_: Exception) { null }
    }

    private fun removeIncompleteEducationItems() = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.educationList
        if (db.isEmpty()) return@tryCatch

        val complete = db.filter { isComplete(it) }.toMutableList()
        val partialCount = db.count { !isEmpty(it) && !isComplete(it) }

        sharedViewModel.cvModelRequestDb.educationList = complete

        adapter?.submitList(complete.toList())
        if (partialCount > 0) {
            Toast.makeText(
                requireContext(),
                "Removed $partialCount partially filled education item(s).",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (complete.isNotEmpty()) adapter?.expandOnly(complete.lastIndex)
    }

    private fun isComplete(m: EducationModel): Boolean {
        val inst = m.institute?.trim().orEmpty()
        val course = m.course?.trim().orEmpty()
        val grade = m.grade?.trim().orEmpty()
        return inst.isNotEmpty() &&
                course.isNotEmpty() &&
                grade.isNotEmpty() &&
                !m.startDate.isNullOrEmpty() &&
                (!m.endDate.isNullOrEmpty() || m.isCurrentStudent)
    }

    private fun isEmpty(m: EducationModel): Boolean {
        return (m.institute.isNullOrBlank()
                && m.course.isNullOrBlank()
                && m.grade.isNullOrBlank()
                && m.startDate.isNullOrBlank()
                && m.endDate.isNullOrBlank()
                && !m.isCurrentStudent)
    }
}
