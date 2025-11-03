package com.example.cvmaker.fragments.profiledetail

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
import com.example.cvmaker.databinding.FragmentExperienceDetailBinding
import com.example.cvmaker.fragments.profiledetail.adapters.ExperienceAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.workingmodels.ExperienceModel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExperienceDetailFragment : Fragment() {

    private lateinit var binding: FragmentExperienceDetailBinding
    private var adapter: ExperienceAdapter? = null
    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var companyNameEntryLogged = false
    private var companyDetailsLogged = false
    private var jobTitleEntryLogged = false
    private var startCompanyDateLogged = false
    private var endCompanyDateLogged = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExperienceDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        initUi()
        initAdapter()
        populateFromDb()
        initClicks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeIncompleteItems()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    private fun initUi() = tryCatch {
        binding.experienceRecyclerView.visibility = View.VISIBLE
        binding.experienceContainer.visibility = View.GONE
        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    private fun initAdapter() = tryCatch {
        context?.let { ctx ->
            binding.experienceRecyclerView.layoutManager = LinearLayoutManager(ctx)
            adapter = ExperienceAdapter().also { binding.experienceRecyclerView.adapter = it }

            adapter?.setOnEditTextCompleteListener(object :
                ExperienceAdapter.OnEditTextCompleteListener {

                override fun onCompanyNameTextChangeChange(position: Int, text: String) {
                    tryCatch {
                        if (!companyNameEntryLogged && text.isNotEmpty()) {
                            (activity as? MainActivity)?.let { companyNameEntryLogged = true }
                        }
                        updateModel(position) { m -> m.companyName = text.ifBlank { null } }
                    }
                }

                override fun onDetailTextChange(position: Int, text: String) {
                    tryCatch {
                        if (!companyDetailsLogged && text.isNotEmpty()) {
                            (activity as? MainActivity)?.let { companyDetailsLogged = true }
                        }
                        updateModel(position) { m -> m.detail = text.ifBlank { null } }
                    }
                }

                override fun onJobTextChange(position: Int, text: String) {
                    tryCatch {
                        if (!jobTitleEntryLogged && text.isNotEmpty()) {
                            (activity as? MainActivity)?.let { jobTitleEntryLogged = true }
                        }
                        updateModel(position) { m -> m.designation = text.ifBlank { null } }
                    }
                }

                override fun onStartDateChange(position: Int, text: String) {
                    tryCatch {
                        if (!startCompanyDateLogged && text.isNotEmpty()) {
                            (activity as? MainActivity)?.let { startCompanyDateLogged = true }
                        }
                        updateModel(position) { m -> m.startDate = parseUiDateToIso(text) }
                    }
                }

                override fun onEndDateChange(position: Int, text: String) {
                    tryCatch {
                        if (!endCompanyDateLogged && text.isNotEmpty()) {
                            (activity as? MainActivity)?.let { endCompanyDateLogged = true }
                        }
                        updateModel(position) { m ->
                            if (text == getString(R.string.present)) {
                                m.isCurrentWorking = true
                                m.endDate = null
                            } else {
                                m.isCurrentWorking = false
                                m.endDate = parseUiDateToIso(text)
                            }
                        }
                    }
                }

                override fun onCheckBoxStateChanged(position: Int, isChecked: Boolean) {
                    updateModel(position) { m ->
                        m.isCurrentWorking = isChecked
                        if (isChecked) m.endDate = null
                    }
                }

                override fun requestFocusForNewItem(editText: EditText) {
                    editText.requestFocus()
                }

                override fun removeItem(position: Int) {
                    showRemoveItemBottomSheet(position)
                }
            })
        }
    }

    private fun populateFromDb() = tryCatch {
        val dbList = sharedViewModel.cvModelRequestDb.experienceList
        if (dbList.isEmpty()) dbList.add(ExperienceModel())
        adapter?.submitList(dbList.toList())
        adapter?.expandOnly(dbList.lastIndex.coerceAtLeast(0))
    }

    private fun initClicks() = tryCatch {
        binding.backButton.setOnClickListener { backPressed() }
        binding.addMoreExperience.setOnClickListener { addMoreExperienceItem() }
        binding.previewCv.setOnClickListener { /* preview */ }
        binding.btnSave.setOnClickListener {
            if (!validateFields()) return@setOnClickListener
            if (!validateDates()) return@setOnClickListener
            findNavController().navigateUp()
            Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMoreExperienceItem() = tryCatch {
        if (validateFields() && validateDates()) {
            val db = sharedViewModel.cvModelRequestDb.experienceList
            db.add(ExperienceModel())
            adapter?.submitList(db.toList())
            val newIndex = db.lastIndex
            binding.experienceRecyclerView.scrollToPosition(newIndex)
            adapter?.expandOnly(newIndex) // close previous, open NEW one only
        }
    }

    private fun showRemoveItemBottomSheet(position: Int) {
        val sheet = RemoveItemBottomSheet { removeItemFromDb(position) }
        sheet.show(parentFragmentManager, "RemoveItemBottomSheet")
    }

    private fun removeItemFromDb(position: Int) = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.experienceList
        if (position !in db.indices) return@tryCatch
        db.removeAt(position)
        if (db.isEmpty()) { findNavController().popBackStack(); return@tryCatch }

        binding.scrollExperience.isEnabled = false
        binding.scrollExperience.clearFocus()

        adapter?.submitList(db.toList())
        adapter?.expandOnly(position.coerceAtMost(db.lastIndex))

        binding.scrollExperience.post { binding.scrollExperience.isEnabled = true }
    }

    private fun validateFields(): Boolean {
        val list = sharedViewModel.cvModelRequestDb.experienceList
        val ok = list.all { m ->
            val company = m.companyName?.trim().orEmpty()
            val desig = m.designation?.trim().orEmpty()
            val start = m.startDate?.trim()
            val end = m.endDate?.trim()
            company.isNotEmpty() &&
                    desig.isNotEmpty() &&
                    !start.isNullOrEmpty() &&
                    (!end.isNullOrEmpty() || m.isCurrentWorking) &&
                    (m.detail?.trim().orEmpty().isNotEmpty())
        }
        if (!ok) {
            Toast.makeText(requireContext(), "Please fill all fields before proceeding.", Toast.LENGTH_SHORT).show()
        }
        return ok
    }

    private fun validateDates(): Boolean {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (m in sharedViewModel.cvModelRequestDb.experienceList) {
            val start = m.startDate; val end = m.endDate
            if (!start.isNullOrEmpty() && !end.isNullOrEmpty() && !m.isCurrentWorking) {
                try {
                    val s: Date? = fmt.parse(start); val e: Date? = fmt.parse(end)
                    if (s != null && e != null && e.before(s)) {
                        Toast.makeText(requireContext(),"End date cannot be before the start date.", Toast.LENGTH_SHORT).show()
                        return false
                    }
                } catch (_: Exception) { return false }
            }
        }
        return true
    }

    private fun removeIncompleteItems() = tryCatch {
        val db = sharedViewModel.cvModelRequestDb.experienceList
        val original = db.size
        if (original == 0) return@tryCatch

        val complete = db.filter { isModelComplete(it) }.toMutableList()
        val partialCount = db.count { !isModelEmpty(it) && !isModelComplete(it) }
        sharedViewModel.cvModelRequestDb.experienceList = complete

        if (partialCount > 0) {
            Toast.makeText(requireContext(),"Removed $partialCount partially filled experience item(s)", Toast.LENGTH_SHORT).show()
        }
        adapter?.submitList(complete.toList())
        if (complete.isNotEmpty()) adapter?.expandOnly(complete.lastIndex)
    }

    private fun isModelComplete(m: ExperienceModel): Boolean {
        val company = m.companyName?.trim().orEmpty()
        val desig = m.designation?.trim().orEmpty()
        return company.isNotEmpty() &&
                desig.isNotEmpty() &&
                !m.startDate.isNullOrEmpty() &&
                (!m.endDate.isNullOrEmpty() || m.isCurrentWorking) &&
                (m.detail?.trim().orEmpty().isNotEmpty())
    }

    private fun isModelEmpty(m: ExperienceModel): Boolean {
        return (m.companyName.isNullOrBlank()
                && m.designation.isNullOrBlank()
                && m.startDate.isNullOrBlank()
                && m.endDate.isNullOrBlank()
                && !m.isCurrentWorking
                && m.detail.isNullOrBlank())
    }

    private inline fun updateModel(index: Int, update: (ExperienceModel) -> Unit) {
        val db = sharedViewModel.cvModelRequestDb.experienceList
        if (index in db.indices) update(db[index])
    }

    private fun backPressed() {
        removeIncompleteItems()
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

    /** "MM/dd/yyyy" -> "yyyy-MM-dd" (DB) */
    private fun parseUiDateToIso(text: String?): String? {
        if (text.isNullOrBlank() || text.equals(getString(R.string.present), true)) return null
        return try {
            val input = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val output = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val p = text.split("/")
            val mm = p.getOrNull(0)?.padStart(2, '0') ?: return null
            val dd = p.getOrNull(1)?.padStart(2, '0') ?: return null
            val yy = p.getOrNull(2) ?: return null
            input.parse("$mm/$dd/$yy")?.let { output.format(it) }
        } catch (_: Exception) { null }
    }
}
