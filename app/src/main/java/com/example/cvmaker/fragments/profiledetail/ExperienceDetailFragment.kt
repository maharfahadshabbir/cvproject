package com.example.cvmaker.fragments.profiledetail

import android.os.Bundle
import android.util.Log
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
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.previewCv
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.profilemodels.Experience
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExperienceDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureBackPress()

        initListener()

//        view.viewTreeObserver.addOnGlobalLayoutListener {
//            val rect = Rect()
//            view.getWindowVisibleDisplayFrame(rect)
//            val screenHeight = view.rootView.height
//            val keypadHeight = screenHeight - rect.bottom
//            if (keypadHeight > screenHeight * 0.15) {
//                onKeyboardShown(keypadHeight)
//            } else {
//                onKeyboardHidden()
//            }
//        }


    }

    override fun onDestroyView() {
        super.onDestroyView()

        removeIncompleteItems()

        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    private fun backPressed() {
        removeIncompleteItems()
        findNavController().popBackStack()
    }

    private fun configureBackPress() {
        tryCatch {
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }
            onBackPressedCallback?.let {
                getViewLifecycleOwnerOrNull()?.let { it1 ->
                    activity?.onBackPressedDispatcher?.addCallback(
                        it1,
                        it
                    )
                }
            }
        }
    }

    private fun initListener() {
        tryCatch {
            adapterListener()
            populateData()
            clickListener()
            binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
        }
    }

    private fun clickListener() {
        binding.backButton.setOnClickListener {
            backPressed()
        }
        binding.addMoreExperience.setOnClickListener { addNewEducationItem() }
        binding.expnextbtn.setOnClickListener {
            if (!validateFields()) {
                return@setOnClickListener
            }
            if (!validateDates()) {
                return@setOnClickListener
            }
            tryCatch {
                activity?.let {
                    if (it is MainActivity) {
                   /*     InterstitialHelper.showAndLoadInterstitial(
                            it,
                            RemoteConfig.CREATE_AI_CV_INTERSTITIAL_ID
                        ) {
                            navigateToFragment(
                                sharedNavVM = sharedNavigationViewModel,
                                targetDestinationId = R.id.customHomeFragment
                            )
                        }*/
                    }
                }
            }
        }

        binding.previewCv.setOnClickListener {
//            previewCv( sharedViewModel, R.id.fragmentPreviewApi)
        }
    }

    private fun populateData() {
        tryCatch {
            if (sharedViewModel.cvModel.experiences.isNotEmpty()) {
                adapter?.submitList(sharedViewModel.cvModel.experiences)
            } else {
                sharedViewModel.cvModel.apply {
                    this.experiences.add(Experience())
                }
                adapter?.submitList(sharedViewModel.cvModel.experiences)
            }
        }
    }

    private fun adapterListener() {
        tryCatch {
            context?.let { ctx ->
                binding.experienceRecyclerView.layoutManager = LinearLayoutManager(ctx)
                adapter = ExperienceAdapter()
                binding.experienceRecyclerView.adapter = adapter
                adapter?.setOnEditTextCompleteListener(object :
                    ExperienceAdapter.OnEditTextCompleteListener {
                    override fun onCompanyNameTextChangeChange(position: Int, text: String) {
                        tryCatch {
                            if (!companyNameEntryLogged && text.isNotEmpty()) {
                                activity?.let {
                                    if (it is MainActivity) {
                                        companyNameEntryLogged = true
                                    }
                                }
                            }
                            if (position in 0 until sharedViewModel.cvModel.experiences.size) {
                                sharedViewModel.cvModel.experiences[position]?.company_name = text
                                sharedViewModel.cvModel.apply {
                                    this.experiences[position]?.company_name = text
                                }
                            }
                        }
                    }

                    override fun onDetailTextChange(position: Int, text: String) {
                        tryCatch {
                            if (!companyDetailsLogged && text.isNotEmpty()) {
                                activity?.let {
                                    if (it is MainActivity) {
                                        companyDetailsLogged = true
                                    }
                                }
                            }
                            if (position in 0 until sharedViewModel.cvModel.experiences.size) {
                                sharedViewModel.cvModel.experiences[position]?.description = text

                                sharedViewModel.cvModel.apply {
                                    this.experiences[position]?.description = text

                                }
                            }
                        }
                    }

                    override fun onJobTextChange(position: Int, text: String) {
                        tryCatch {
                            if (!jobTitleEntryLogged && text.isNotEmpty()) {
                                activity?.let {
                                    if (it is MainActivity) {
                                        jobTitleEntryLogged = true
                                    }
                                }
                            }

                            if (position in 0 until sharedViewModel.cvModel.experiences.size) {
                                tryCatch {
                                    sharedViewModel.cvModel.apply {
                                        this.experiences[position]?.designation = text
                                        this.designation = text
                                    }
                                }
                            }
                        }
                    }

                    override fun onStartDateChange(position: Int, text: String) {
                        tryCatch {
                            if (!startCompanyDateLogged && text.isNotEmpty()) {
                                activity?.let {
                                    if (it is MainActivity) {
                                        startCompanyDateLogged = true
                                    }
                                }
                            }
                            if (position in 0 until sharedViewModel.cvModel.experiences.size) {
                                // Parse the input date
                                val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                                // Check if month is a single digit
                                val inputDateString = text.split("/")
                                val month = inputDateString[0]
                                val correctedMonth = if (month.length == 1) {
                                    "0$month"
                                } else if (month.length == 2 && inputDateString[0][0].isLetter()) {
                                    inputDateString[0].substring(1)
                                } else {
                                    month // Keep the month unchanged if it's already in the correct format
                                }
                                val parsedDate =
                                    inputFormat.parse(correctedMonth + "/" + inputDateString[1] + "/" + inputDateString[2])

                                // Format the parsed date to the desired format "YYYY-MM-DD"
                                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                                sharedViewModel.cvModel.apply {
                                    this.experiences[position]?.start_at =
                                        parsedDate?.let { outputFormat.format(it) }
                                }
                            }
                        }
                    }

                    override fun onEndDateChange(position: Int, text: String) {
                        tryCatch {
                            if (!endCompanyDateLogged && text.isNotEmpty()) {
                                activity?.let {
                                    if (it is MainActivity) {
                                        endCompanyDateLogged = true
                                    }
                                }
                            }
                            if (position in 0 until sharedViewModel.cvModel.experiences.size) {
                                if (text == (resources.getString(R.string.present))) {
                                    sharedViewModel.cvModel.apply {
                                        this.experiences[position]?.end_at = null
                                    }
                                } else {
                                    if (text.isNotEmpty()) {
                                        // Parse the input date
                                        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                                        // Check if month is a single digit
                                        val inputDateString = text.split("/")
                                        val month = inputDateString[0]
                                        val correctedMonth = if (month.length == 1) {
                                            "0$month"
                                        } else if (month.length == 2 && inputDateString[0][0].isLetter()) {
                                            inputDateString[0].substring(1)
                                        } else {
                                            month // Keep the month unchanged if it's already in the correct format
                                        }

                                        val parsedDate =
                                            inputFormat.parse(correctedMonth + "/" + inputDateString[1] + "/" + inputDateString[2])

                                        // Format the parsed date to the desired format "YYYY-MM-DD"
                                        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                                        sharedViewModel.cvModel.apply {
                                            this.experiences[position]?.end_at =
                                                parsedDate?.let { outputFormat.format(it) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onCheckBoxStateChanged(position: Int, isChecked: Boolean) {
                        tryCatch {

                            if (position in 0 until sharedViewModel.cvModel.educations.size) {
                                tryCatch {
                                    if (sharedViewModel.cvModel.educations[0].end_at == R.string.present.toString()) {
                                        sharedViewModel.cvModel.apply {
                                            this.experiences[position]?.present = true
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun requestFocusForNewItem(editText: EditText) {
                        editText.requestFocus()
                    }

                    override fun removeItem(position: Int) {
                        tryCatch {
                            showRemoveItemBottomSheet(position)
                        }
                    }
                })
            }
        }
    }

    private fun validateFields(): Boolean {
        val areAllItemsNotEmpty = sharedViewModel.cvModel.experiences.all {
            it?.company_name?.isNotEmpty() == true &&
                    it.designation.isNotEmpty() &&
                    !it.start_at.isNullOrEmpty() &&
                    (!it.end_at.isNullOrEmpty() || it.present) &&
                    it.description.isNotEmpty()
        }
        if (!areAllItemsNotEmpty) {
            context?.let {
                Toast.makeText(
                    it,
                    "Please fill all fields before proceeding.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return areAllItemsNotEmpty
    }

    private fun validateDates(): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (experience in sharedViewModel.cvModel.experiences) {
            if (experience != null && !experience.start_at.isNullOrEmpty() && !experience.end_at.isNullOrEmpty() && !experience.present) {
                try {
                    val startDate: Date? = dateFormat.parse(experience.start_at!!)
                    val endDate: Date? = dateFormat.parse(experience.end_at!!)
                    if (startDate != null && endDate != null && endDate.before(startDate)) {
                        context?.let {
                            Toast.makeText(
                                it,
                                "End date cannot be before the start date.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return false // Validation failed
                    }
                } catch (e: Exception) {
                    Log.e("ExperienceFragment", "Date parsing error", e)
                    // Optionally show a generic error toast for parsing issues
                    return false
                }
            }
        }
        return true // All dates are valid
    }

    private fun showRemoveItemBottomSheet(position: Int) {
        val removeItemBottomSheet = RemoveItemBottomSheet() {
            removeItemFromList(position)
        }
        removeItemBottomSheet.show(parentFragmentManager, "RemoveItemBottomSheet")
    }

    private fun removeItemFromList(position: Int) {
        try {
            if (position >= 0 && position < sharedViewModel.cvModel.experiences.size) {
                // Create a new copy (so DiffUtil sees a new list instance)
                val updatedList = sharedViewModel.cvModel.experiences.toMutableList()
                updatedList.removeAt(position)
                sharedViewModel.cvModel.experiences = updatedList
                if (updatedList.isEmpty()) {
                    findNavController().popBackStack()
                    return
                }

                binding.scrollExperience.isEnabled = false
                binding.scrollExperience.clearFocus()

                adapter?.submitList(updatedList.toList())

// Re-enable scrolling after a frame
                binding.scrollExperience.post {
                    binding.scrollExperience.isEnabled = true
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun addNewEducationItem() {
        tryCatch {
            if (validateFields() && validateDates()) {
                // Only add a new item if all existing items are not empty and dates are valid
                sharedViewModel.cvModel.apply { this.experiences.add(Experience()) }
                adapter?.submitList(sharedViewModel.cvModel.experiences)
                binding.experienceRecyclerView.scrollToPosition(sharedViewModel.cvModel.experiences.size - 1)
            } else {
                // Toast messages are already shown in the validation functions
            }
        }
    }

    private fun removeIncompleteItems() {
        tryCatch {
            val originalCount = sharedViewModel.cvModel.experiences.size
            // If there are no items, nothing to remove
            if (originalCount == 0) return@tryCatch

            val completeItems = sharedViewModel.cvModel.experiences.filter { experience ->
                experience != null && isExperienceComplete(experience)
            }.toMutableList()

            // Count different types of incomplete items for better logging
            val emptyItems = sharedViewModel.cvModel.experiences.count { experience ->
                experience != null && isExperienceEmpty(experience)
            }
            val partialItems = sharedViewModel.cvModel.experiences.count { experience ->
                experience != null && isExperiencePartiallyFilled(experience)
            }

            // Update the ViewModel with only complete items
            sharedViewModel.cvModel.experiences = completeItems

            // Log removed items count for debugging
            val removedCount = originalCount - completeItems.size
            if (removedCount > 0) {
                Log.d(
                    "ExperienceFragment",
                    "Removed $removedCount incomplete experience items ($emptyItems empty, $partialItems partial)"
                )

                // Only show toast for partially filled items (don't bother user about completely empty ones)
                if (partialItems > 0) {
                    context?.let {
                        val message = "Removed $partialItems partially filled experience item(s)"
                        Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun isExperienceComplete(experience: Experience): Boolean {
        return experience.company_name.trim().isNotEmpty() &&
                experience.designation.trim().isNotEmpty() &&
                !experience.start_at.isNullOrEmpty() &&
                (!experience.end_at.isNullOrEmpty() || experience.present) &&
                experience.description.trim().isNotEmpty()
    }

    private fun isExperienceEmpty(experience: Experience): Boolean {
        return experience.company_name.trim().isEmpty() &&
                experience.designation.trim().isEmpty() &&
                experience.start_at.isNullOrEmpty() &&
                experience.end_at.isNullOrEmpty() &&
                !experience.present &&
                experience.description.trim().isEmpty()
    }

    private fun isExperiencePartiallyFilled(experience: Experience): Boolean {
        return !isExperienceEmpty(experience) && !isExperienceComplete(experience)
    }

}