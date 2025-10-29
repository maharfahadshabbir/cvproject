package com.example.cvmaker.fragments.profiledetail

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cvmaker.R
import com.example.cvmaker.activities.MainActivity
import com.example.cvmaker.databinding.FragmentEducationDetailBinding
import com.example.cvmaker.fragments.profiledetail.adapters.EducationAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.previewCv
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.profilemodels.Education
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.getValue


class EducationDetailFragment : Fragment() {

    private lateinit var binding: FragmentEducationDetailBinding

    private var adapter: EducationAdapter? = null

    private val sharedViewModel by activityViewModels<SharedViewModel>()


    private var isInstituteEntryAnalyticSent = false
    private var isGradeAnalyticSent = false
    private var isCourseAnalyticSent = false
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEducationDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        initListener()
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


    private fun onKeyboardShown(keyboardHeight: Int) {
        binding.scrollEducation.setPadding(
            binding.scrollEducation.paddingLeft,
            binding.scrollEducation.paddingTop,
            binding.scrollEducation.paddingRight,
            keyboardHeight
        )
    }

    private fun onKeyboardHidden() {}

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    private fun backPressed() {
        removeIncompleteEducationItems()
        findNavController().popBackStack()
//        navigateToFragment(
//            sharedNavVM = sharedNavigationViewModel,
//            targetDestinationId = R.id.customHomeFragment
//        )
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListener() {
        adaptorItemListener()
        populateData()
        clickListener()
        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun clickListener() {
        binding.addMore.setOnClickListener {

            addNewEducationItem()
        }


        binding.backButton.setOnClickListener {
            backPressed()
        }
        binding.expnextbtn.setOnClickListener {
            if (!areFieldsValid()) {

                return@setOnClickListener
            }

            if (!areDatesValid()) {
                return@setOnClickListener
            }

            tryCatch {

//                activity?.let {
//                    if (it is MainActivity) {
//                        InterstitialHelper.showAndLoadInterstitial(
//                            it,
//                            RemoteConfig.CREATE_AI_CV_INTERSTITIAL_ID
//                        ) {
//                            navigateToFragment(
//                                sharedNavVM = sharedNavigationViewModel,
//                                targetDestinationId = R.id.customHomeFragment
//                            )
//                        }
//                    }
//                }
            }
        }

        binding.previewCv.setOnClickListener {
//            previewCv()
        }


    }

    private fun populateData() {
        tryCatch {
            if (sharedViewModel.cvModel.educations.isNotEmpty()) {
                adapter?.submitList(sharedViewModel.cvModel.educations)

            } else {
                sharedViewModel.cvModel.apply {
                    this.educations.add(Education())
                }
                adapter?.submitList(sharedViewModel.cvModel.educations)
            }
        }
    }

    private fun adaptorItemListener() {
        tryCatch {
            context?.let { ctx ->
                binding.educationRecyclerView.layoutManager = LinearLayoutManager(ctx)
                adapter = EducationAdapter()
                binding.educationRecyclerView.adapter = adapter
                adapter?.setOnEditTextCompleteListener(object :
                    EducationAdapter.OnEditTextCompleteListener {
                    override fun onInstituteTextChange(position: Int, text: String) {
                        tryCatch {
                            if (text.isNotEmpty() && !isInstituteEntryAnalyticSent) {
                                activity?.let {
                                    if (it is MainActivity) {
                                        isInstituteEntryAnalyticSent = true
                                    }
                                }
                            }
                            // Update the sharedViewModel data directly
                            if (position in 0 until sharedViewModel.cvModel.educations.size) {
                                sharedViewModel.cvModel.apply {
                                    this.educations[position].school = text

                                }
                            }
                        }
                    }

                    override fun onGradeTextChange(position: Int, text: String) {
                        tryCatch {
                            if (text.isNotEmpty() && !isGradeAnalyticSent) {
                                activity?.let {
                                    if (it is MainActivity) {
                                        isGradeAnalyticSent = true
                                    }
                                }
                            }
                            if (position in 0 until sharedViewModel.cvModel.educations.size) {
                                sharedViewModel.cvModel.apply {
                                    this.educations[position].location = text

                                }
                            }
                        }
                    }

                    override fun onStartDateChange(position: Int, text: String) {

                        // Update the sharedViewModel data directly
                        if (position in 0 until sharedViewModel.cvModel.educations.size) {
                            tryCatch {
                                val parts = text.split("/")
                                if (parts.size == 3) { // Ensure there are three parts separated by "/"
                                    val month = parts[0]
                                    val day = parts[1]
                                    val yearPart = parts[2]
                                    // Check if month, day, and yearPart are integers
                                    if (month.toIntOrNull() != null && day.toIntOrNull() != null && yearPart.toIntOrNull() != null) {
                                        val year = yearPart.toInt()
                                        // Create a new date string in "MM/dd/yyyy" format
                                        val formattedDate = "$month/$day/$year"
                                        // Parse the formatted date string
                                        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                                        val parsedDate = inputFormat.parse(formattedDate)
                                        // Format the parsed date to the desired format "yyyy-MM-dd"
                                        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                        sharedViewModel.cvModel.apply {
                                            this.educations[position].start_at =
                                                outputFormat.format(parsedDate!!)

                                        }
                                        // Further processing with the formatted date
                                    } else {
                                        // Handle invalid integer values in the date parts
                                    }
                                } else {
                                    // Handle invalid date format
                                }
                            }
                        }
                    }

                    override fun onEndDateChange(position: Int, text: String) {
                        tryCatch {

                            // Update the sharedViewModel data directly
                            if (position in 0 until sharedViewModel.cvModel.educations.size) {
                                if (text == (resources.getString(R.string.present))) {
                                    sharedViewModel.cvModel.apply {
                                        this.educations[position].end_at = null
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
                                            this.educations[position].end_at =
                                                outputFormat.format(parsedDate!!)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onCourseTextChange(position: Int, text: String) {
                        tryCatch {
                            if (text.isNotEmpty() && !isCourseAnalyticSent) {
                                activity?.let {
                                    if (it is MainActivity) {
                                        isCourseAnalyticSent = true
                                    }
                                }
                            }
                            if (position in 0 until sharedViewModel.cvModel.educations.size) {
                                sharedViewModel.cvModel.apply {
                                    this.educations[position].name = text

                                }
                            }
                        }
                    }

                    override fun onCheckBoxStateChanged(position: Int, isChecked: Boolean) {
                        tryCatch {

                            if (position in 0 until sharedViewModel.cvModel.educations.size) {
                                if (sharedViewModel.cvModel.educations[position].end_at == resources.getString(R.string.present)) {
                                    sharedViewModel.cvModel.apply {
                                        this.educations[position].present = true
                                    }
                                } else {
                                    sharedViewModel.cvModel.apply {
                                        this.educations[position].present = false
                                    }
                                }
                            }
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
    }

    private fun showRemoveItemBottomSheet(position: Int) {
        RemoveItemBottomSheet {
            removeItemFromList(position)
        }.show(parentFragmentManager, "RemoveItemBottomSheet")
    }

    private fun removeItemFromList(position: Int) {
        try {
            if (position >= 0 && position < sharedViewModel.cvModel.educations.size) {
                val updatedList = sharedViewModel.cvModel.educations.toMutableList()
                updatedList.removeAt(position)
                sharedViewModel.cvModel.educations = updatedList
                if (updatedList.isEmpty()) {
                    findNavController().popBackStack()
                    return
                }
                binding.scrollEducation.isEnabled = false
                binding.scrollEducation.clearFocus()

                adapter?.submitList(updatedList.toList())

// Re-enable scrolling after a frame
                binding.scrollEducation.post {
                    binding.scrollEducation.isEnabled = true
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun areFieldsValid(): Boolean {
        val areAllFieldsFilled = sharedViewModel.cvModel.educations.all { edu ->
            edu.school.isNotEmpty() &&
                    edu.name.isNotEmpty() &&
                    edu.location.isNotEmpty() &&
                    !edu.start_at.isNullOrEmpty() &&
                    (edu.present || !edu.end_at.isNullOrEmpty())
        }
        if (!areAllFieldsFilled) {
            context?.let { ctx ->
                Toast.makeText(ctx, "Input fields are empty", Toast.LENGTH_SHORT).show()
            }
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun areDatesValid(): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val isDateValid = sharedViewModel.cvModel.educations.all { edu ->
            isDateOrderValid(edu.start_at, edu.end_at, edu.present, formatter)
        }
        if (!isDateValid) {
            context?.let { ctx ->
                Toast.makeText(ctx, "End date cannot be before start date", Toast.LENGTH_SHORT)
                    .show()
            }
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addNewEducationItem() {
        tryCatch {
            val fieldsValid = areFieldsValid()

            val datesValid = areDatesValid()

            if (fieldsValid && datesValid) {
                val newEducation = Education()
                sharedViewModel.cvModel.educations.add(newEducation)
                adapter?.submitList(sharedViewModel.cvModel.educations.toList())
                binding.educationRecyclerView.scrollToPosition(sharedViewModel.cvModel.educations.size - 1)
            } else if (!fieldsValid) {
                Toast.makeText(context, "Input fields are empty", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "End date cannot be before start date", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isDateOrderValid(
        startDate: String?,
        endDate: String?,
        isPresent: Boolean,
        formatter: DateTimeFormatter
    ): Boolean {
        return try {
            if (isPresent) return true

            val start = startDate?.let { LocalDate.parse(it, formatter) }
            val end = endDate?.let { LocalDate.parse(it, formatter) }

            if (start != null && end != null) {
                !end.isBefore(start)
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun removeIncompleteEducationItems() {
        tryCatch {
            val originalCount = sharedViewModel.cvModel.educations.size
            // If there are no items, nothing to remove
            if (originalCount == 0) return@tryCatch

            val completeItems = sharedViewModel.cvModel.educations.filter { education ->
                education != null && isEducationComplete(education)
            }.toMutableList()

            // Count different types of incomplete items for better logging
            val emptyItems = sharedViewModel.cvModel.educations.count { education ->
                education != null && isEducationEmpty(education)
            }
            val partialItems = sharedViewModel.cvModel.educations.count { education ->
                education != null && isEducationPartiallyFilled(education)
            }

            // Update the ViewModel with only complete items
            sharedViewModel.cvModel.educations = completeItems

            // Log removed items count for debugging
            val removedCount = originalCount - completeItems.size
            if (removedCount > 0) {
                Log.d(
                    "EducationFragment",
                    "Removed $removedCount incomplete educations items ($emptyItems empty, $partialItems partial)"
                )

                // Only show toast for partially filled items (don't bother user about completely empty ones)
                if (partialItems > 0) {
                    context?.let {
                        val message = "Removed $partialItems partially filled education item(s)"
                        Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Check if an educations item has all required fields filled
     */
    private fun isEducationComplete(education: Education): Boolean {
        return education.school.trim().isNotEmpty() &&
                education.name.trim().isNotEmpty() &&
                education.location.trim().isNotEmpty() &&
                !education.start_at.isNullOrEmpty() &&
                (!education.end_at.isNullOrEmpty() || education.present)
    }

    /**
     * Check if an educations item is completely empty (no data entered at all)
     */
    private fun isEducationEmpty(education: Education): Boolean {
        return education.school.trim().isEmpty() &&
                education.name.trim().isEmpty() &&
                education.location.trim().isEmpty() &&
                education.start_at.isNullOrEmpty() &&
                education.end_at.isNullOrEmpty() &&
                !education.present
    }

    /**
     * Check if an educations item is partially filled (has some data but not complete)
     */
    private fun isEducationPartiallyFilled(education: Education): Boolean {
        return !isEducationEmpty(education) && !isEducationComplete(education)
    }


}