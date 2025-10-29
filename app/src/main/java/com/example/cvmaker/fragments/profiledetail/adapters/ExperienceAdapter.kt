package com.example.cvmaker.fragments.profiledetail.adapters


import android.annotation.SuppressLint
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.CompoundButtonCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cvmaker.R
import com.example.cvmaker.databinding.ExperienceRowItemBinding
import com.example.cvmaker.fragments.profiledetail.util.DatePickerUtil
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import com.example.cvmaker.model.profilemodels.Experience

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.text.ifEmpty

class ExperienceAdapter() :
    ListAdapter<Experience, ExperienceAdapter.ViewHolder>(
        DiffCallback()
    ) {



    private val datePickerUtil = DatePickerUtil()
    private var onEditTextCompleteListener: OnEditTextCompleteListener? = null
    var msg = "issue"

    interface OnEditTextCompleteListener {
        fun onCompanyNameTextChangeChange(position: Int, text: String)
        fun onDetailTextChange(position: Int, text: String)
        fun onStartDateChange(position: Int, text: String)
        fun onEndDateChange(position: Int, text: String)
        fun onJobTextChange(position: Int, text: String)
        fun onCheckBoxStateChanged(position: Int, isChecked: Boolean)
        fun requestFocusForNewItem(editText: EditText)

        fun removeItem(position: Int)

    }

    fun setOnEditTextCompleteListener(listener: OnEditTextCompleteListener) {
        onEditTextCompleteListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val binding =
            ExperienceRowItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bindTo(currentItem)
    }


    private inner class GenericTextWatcher(val binding: ExperienceRowItemBinding,private val fieldUpdater: (String) -> Unit) :
        TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Not used in this example
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Not used in this example
        }

        override fun afterTextChanged(s: Editable?) {
            // Notify the listener when EditText changes are completed
            s?.let { fieldUpdater.invoke(it.toString()) }

            if (binding.startDateEdittext.text?.length!! >= 8 && binding.endDateEdittext.text?.length!! >= 8) {
                val check = validateStartandEnddate(
                    binding.startDateEdittext.text.toString(),
                    binding.endDateEdittext.text.toString()
                )
                if (check) {
                    binding.startDateEdittext.error = null
                } else {
                    if (msg == "") {
                        binding.startDateEdittext.error = null
                    } else {
                        binding.startDateEdittext.error = msg
                    }
                }
                if (msg == "") {
                    binding.startDateEdittext.error = null
                } else {
                    binding.startDateEdittext.error = msg
                }
            }
        }
    }


    inner class ViewHolder(val binding: ExperienceRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SuspiciousIndentation")
        fun bindTo(currentItem: Experience) {
            // Check if the item is not null
            // Bind data to the views here
            try {
                if (currentItem.company_name.isEmpty()) {
                    onEditTextCompleteListener?.requestFocusForNewItem(binding.companyEdittext)

                }
            } catch (e: Exception) {
                TODO("Not yet implemented")
            }
            binding.companyEdittext.setText(currentItem?.company_name ?: "")
            binding.designationEdittext.setText(currentItem?.designation ?: "")
            binding.detailEditTxt.setText(currentItem.description)
            // Check if start_at and end_at are not null before proceeding
            if (currentItem.start_at != null) {
                binding.startDateEdittext.setText(ViewUtils.formatDate(currentItem.start_at!!))
            } else {
                Log.e(
                    "DateParsingError",
                    "Failed to parse date. start_at: ${currentItem.start_at}, end_at: ${currentItem.end_at}"
                )
            }
            if (currentItem.end_at != null) {
                binding.endDateEdittext.setText(ViewUtils.formatDate(currentItem.end_at!!))
            } else {
                if (currentItem.present) {
                    Log.e(
                        "DateParsingError",
                        "Failed to parse date. end date is ${currentItem.end_at} and present is ${currentItem.present}"
                    )
                    binding.checkboxfordate.isChecked = true
                    CompoundButtonCompat.setButtonTintList(
                        binding.checkboxfordate,
                        ContextCompat.getColorStateList(itemView.context, R.color.blue)
                    )
                    updateCheckBoxState(currentItem)
                }
            }

            binding.companyEdittext.error = null
            binding.designationEdittext.error = null
            binding.detailEditTxt.error = null
            binding.startDateEdittext.error = null
            binding.endDateEdittext.error = null
            // Example usage:
            ViewUtils.setupEditTextofAdaptors(
                binding.companyEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                binding.designationEdittext
            )
            ViewUtils.setupEditTextofAdaptors(
                binding.designationEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                binding.startDateEdittext
            )

            // Apply the InputFilter to the name field
            ViewUtils.applyCapitalizeFilter(binding.companyEdittext)
            ViewUtils.applyCapitalizeFilter(binding.designationEdittext)

            binding.endDateEdittext.inputType = InputType.TYPE_NULL
            binding.startDateEdittext.inputType =
                InputType.TYPE_NULL // or set in XML: android:inputType="none"

            val showDatePicker: (EditText, (String) -> Unit) -> Unit = { editText, onDateSelected ->
                itemView.context?.let { context ->
                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

                    // Check if the EditText already has a selected date
                    val previousDate = editText.text.toString()
                    if (previousDate.isNotEmpty()) {
                        try {
                            val parsedDate = dateFormat.parse(previousDate)
                            parsedDate?.let { calendar.time = it }
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                    }

                    datePickerUtil.showDatePickerDialog(
                        calendar,
                        0,
                        context,
                        object : DatePickerUtil.DateSelectedListener {
                            override fun onDateSelected(formattedDate: String) {
                                onDateSelected(formattedDate)
                            }
                        })
                }
            }



            binding.startDateEdittext.setOnClickListener {
                showDatePicker(binding.startDateEdittext) { selectedDate ->
                    binding.startDateEdittext.setText(selectedDate)
                }
            }
            binding.startDateEdittext.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showDatePicker(binding.startDateEdittext) { selectedDate ->

                        binding.startDateEdittext.setText(selectedDate)
                    }
                }
            }

            binding.endDateEdittext.setOnClickListener {
                showDatePicker(binding.endDateEdittext) { selectedDate ->
                    binding.endDateEdittext.setText(selectedDate)
                }
            }

            binding.endDateEdittext.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showDatePicker(binding.endDateEdittext) { selectedDate ->
                        binding.endDateEdittext.setText(selectedDate)
                    }
                }
            }

            // Set listeners for EditText changes
            binding.companyEdittext.addTextChangedListener(GenericTextWatcher(binding) { newText ->
                if (newText.isNotEmpty()) {
                    if (newText.length <= 50) {
                    } else {
                        // Trim the input to 30 characters
                        val trimmedText = newText.substring(0, 50)
                        binding.companyEdittext.setText(trimmedText)
                        // Set the cursor to the end of the trimmed text
                        binding.companyEdittext.setSelection(trimmedText.length)
                        binding.companyEdittext.error =
                            "Character limit exceeded (50 characters max)."
                    }
                }

                onEditTextCompleteListener?.onCompanyNameTextChangeChange(adapterPosition, newText)
            })



            binding.designationEdittext.addTextChangedListener(GenericTextWatcher (binding){ newText ->
                if (newText.isNotEmpty()) {
                    if (newText.length <= 50) {

                    } else {

                        // Trim the input to 30 characters
                        val trimmedText = newText.substring(0, 50)
                        binding.designationEdittext.setText(trimmedText)
                        // Set the cursor to the end of the trimmed text
                        binding.designationEdittext.setSelection(trimmedText.length)
                        binding.designationEdittext.error =
                            "Character limit exceeded (30 characters max)."
                    }
                }
                onEditTextCompleteListener?.onJobTextChange(adapterPosition, newText)
            })

            binding.detailEditTxt.addTextChangedListener(GenericTextWatcher (binding){ newText ->
//                if (newText.isNotEmpty()) {
//                    if (newText.length <= 30) {
//
//                    } else {
//
//                        // Trim the input to 30 characters
//                        val trimmedText = newText.substring(0, 30)
//                        binding.detailEditTxt.setText(trimmedText)
//                        // Set the cursor to the end of the trimmed text
//                        binding.detailEditTxt.setSelection(trimmedText.length)
//                        binding.detailEditTxt.error = "Character limit exceeded (30 characters max)."
//                    }
//                }
                onEditTextCompleteListener?.onDetailTextChange(adapterPosition, newText)
            })
            binding.startDateEdittext.addTextChangedListener(GenericTextWatcher(binding) { newText ->

                val check = validateStartandEnddate(
                    binding.startDateEdittext.text.toString(),
                    binding.endDateEdittext.text.toString()
                )
                if (check) {
                    binding.startDateEdittext.error = null
                }
                onEditTextCompleteListener?.onStartDateChange(adapterPosition, newText)
            })

            binding.endDateEdittext.addTextChangedListener(GenericTextWatcher(binding) { newText ->
                onEditTextCompleteListener?.onEndDateChange(adapterPosition, newText)
            })

            binding.viewInstitutedetailicn.setOnClickListener {
                toggleVisibility()
            }


            binding.removeItem.setOnClickListener {
                if(absoluteAdapterPosition!= RecyclerView.NO_POSITION) {
                    onEditTextCompleteListener?.removeItem(absoluteAdapterPosition)
                }
            }

            // Set listener for CheckBox changes
            binding.checkboxfordate.setOnCheckedChangeListener { _, isChecked ->
                // currentItem.present = true
                updateCheckBoxState(currentItem)
                onEditTextCompleteListener?.onCheckBoxStateChanged(adapterPosition, isChecked)
            }
        }

        private fun toggleVisibility() {
            val isDataVisible = binding.dataConstraint.isVisible
            binding.dataConstraint.isVisible = !isDataVisible
            binding.company.text = currentList[position].company_name.ifEmpty { "Institute" }
            binding.viewInstitutedetailicn.rotation = if (isDataVisible) 180f else 0f
        }

        private fun updateCheckBoxState(currentItem: Experience) {
            val isChecked = binding.checkboxfordate.isChecked

            // Change the check color to blue when checked
            val blueColor = ContextCompat.getColorStateList(itemView.context, R.color.blue)
            val gray = ContextCompat.getColorStateList(itemView.context, R.color.gray_tx_color)

            // Disable the end date EditText if CheckBox is checked
            if (isChecked) {
                currentItem.present = true
                CompoundButtonCompat.setButtonTintList(binding.checkboxfordate, blueColor)
                binding.endDateEdittext.setText(R.string.present)
                binding.endDateEdittext.isEnabled = !isChecked
            } else {
                currentItem.present = false
                CompoundButtonCompat.setButtonTintList(binding.checkboxfordate, gray)

                binding.endDateEdittext.text?.clear()
                binding.endDateEdittext.hint = "MM/DD/YYYY"
                binding.endDateEdittext.isEnabled = !isChecked

            }
        }
    }

    fun validateStartandEnddate(startDateText: String, endDateText: String): Boolean {
        var check = false
        val currentCalendar = Calendar.getInstance()
        val currentYear = currentCalendar.get(Calendar.YEAR)
        val currentday = currentCalendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth =
            currentCalendar.get(Calendar.MONTH) + 1 // Adding 1 because months are zero-based

        if (startDateText.isNotEmpty() && endDateText.isNotEmpty()) {
            val startParts = startDateText.split("/")
            val endParts = endDateText.split("/")

            if (startParts.size == 3 && endParts.size == 3) {

                val startMonth = startParts[0].toIntOrNull()
                val startDay = startParts[1].toIntOrNull()
                val startYear = startParts[2].toIntOrNull()
                val endMonth = endParts[0].toIntOrNull()
                val endDay = endParts[1].toIntOrNull()
                val endYear = endParts[2].toIntOrNull()
                Log.d(
                    "validateStartandEnddate",
                    "validateStartandEnddate: startmonth is $currentYear and startyear is $startYear , endmonth is $endMonth and endyear is $endYear"
                )
                if (startMonth != null && startYear != null && endMonth != null && endYear != null && endDay != null && startDay != null) {
                    if (((startYear < (currentYear)) || (startYear == currentYear && startMonth < currentMonth) || (startYear == currentYear && startMonth == currentMonth && startDay <= currentday)) && ((endYear < (currentYear)) || (endYear == currentYear && endMonth < currentMonth) || (endYear == currentYear && endMonth == currentMonth && endDay <= currentday))) {
                        if (startYear < endYear || (startYear == endYear && startMonth < endMonth) || (startYear == endYear && startMonth == endMonth && startDay <= endDay)) {
                            msg = ""
                            check = true // Start date is earlier than end date
                        } else {
                            msg = "End Date should be greater than Start Date"
                            check = false
                        }
                    } else {
                        msg = "Start and End Years should be less than the current year"
                        check = false
                    }
                } else {
                    msg = "Invalid date parameters"
                    check = false
                }
            } else if (endDateText == "Present") {


                val calendar = Calendar.getInstance()

                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)

                val endParts = formattedDate.split("/")

                val startMonth = startParts[0].toIntOrNull()
                val startDay = startParts[1].toIntOrNull()
                val startYear = startParts[2].toIntOrNull()
                val endMonth = endParts[0].toIntOrNull()
                val endDay = endParts[1].toIntOrNull()
                val endYear = endParts[2].toIntOrNull()
                ///
                Log.d(
                    "validateStartandEnddate",
                    "validateStartandEnddate: startmonth is $startMonth and startyear is $startYear , endmonth is $endMonth and endyear is $endYear"
                )
                if (startMonth != null && startYear != null && endMonth != null && endYear != null && endDay != null && startDay != null) {
                    if (((startYear < (currentYear)) || (startYear == currentYear && startMonth < currentMonth) || (startYear == currentYear && startMonth == currentMonth && startDay <= currentday)) && ((endYear < (currentYear)) || (endYear == currentYear && endMonth < currentMonth) || (endYear == currentYear && endMonth == currentMonth && endDay <= currentday))) {
                        if (startYear < endYear || (startYear == endYear && startMonth < endMonth) || (startYear == endYear && startMonth == endMonth && startDay <= endDay)) {
                            msg = ""
                            check = true // Start date is earlier than end date
                        } else {
                            msg = "End Date should be greater than Start Date"
                            check = false
                        }
                    } else {
                        msg = "Start and End Years should be less than the current year"
                        check = false
                    }
                } else {
                    msg = "Invalid date parameters"
                    check = false
                }
            }
        } else {
            Log.d("validateStartandEndddfffffate", "validateStartandEnddate: ")
        }

        return check // Invalid date or end date is not later than the start date
    }

    class DiffCallback : DiffUtil.ItemCallback<Experience>() {
        override fun areItemsTheSame(oldItem: Experience, newItem: Experience) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Experience, newItem: Experience) =
            oldItem == newItem
    }

    private fun hideData(binding: ExperienceRowItemBinding, currentItem: Experience) {

        if (currentItem.company_name.isNotEmpty()) {

            binding.title.isVisible = true

            binding.title.text = binding.companyEdittext.text.toString()

            binding.titledetailicn.setBackgroundResource(R.drawable.hide_icon)

            binding.titledetailicn.isVisible = true

            binding.dataConstraint.isVisible = false

        } else {

            binding.title.text = binding.root.context.getString(R.string.title);

            binding.title.isVisible = true

            binding.titledetailicn.setBackgroundResource(R.drawable.hide_icon)

            binding.titledetailicn.isVisible = true

            binding.dataConstraint.isVisible = false

        }
    }

    // Function to update visibility of views
    private fun showData(binding: ExperienceRowItemBinding, currentItem: Experience) {
        // Show company-related views and hide title-related views
        binding.title.isVisible = false
        binding.titledetailicn.isVisible = false

        binding.dataConstraint.isVisible = true

    }
}

