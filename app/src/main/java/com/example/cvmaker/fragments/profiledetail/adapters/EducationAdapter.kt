package com.example.cvmaker.fragments.profiledetail.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.CompoundButtonCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cvmaker.R
import com.example.cvmaker.databinding.EducationRowItemBinding
import com.example.cvmaker.fragments.profiledetail.util.DatePickerUtil
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.applyCapitalizeFilter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.formatDate
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.setupEditTextofAdaptors
import com.example.cvmaker.model.profilemodels.Education
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EducationAdapter :
    ListAdapter<Education, EducationAdapter.ViewHolder>(DiffCallback()) {

    private var onEditTextCompleteListener: OnEditTextCompleteListener? = null

    interface OnEditTextCompleteListener {
        fun onInstituteTextChange(position: Int, text: String)
        fun onGradeTextChange(position: Int, text: String)
        fun onStartDateChange(position: Int, text: String)
        fun onEndDateChange(position: Int, text: String)
        fun onCourseTextChange(position: Int, text: String)
        fun onCheckBoxStateChanged(position: Int, isChecked: Boolean)
        fun requestFocusForNewItem(editText: EditText)
        fun removeItem(position: Int)
    }

    fun setOnEditTextCompleteListener(listener: OnEditTextCompleteListener) {
        onEditTextCompleteListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            EducationRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    inner class ViewHolder(private val binding: EducationRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val datePickerUtil = DatePickerUtil()

        init {
            setupTextWatchers()
            setupDatePickers()
            setupImeOptions()
            applyCapitalization()
            binding.institutedetailicn.setOnClickListener {
                toggleVisibility()
            }
            binding.removeItem.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onEditTextCompleteListener?.removeItem(adapterPosition)
                }
            }

            binding.checkboxfordate.setOnCheckedChangeListener { _, isChecked ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onEditTextCompleteListener?.onCheckBoxStateChanged(adapterPosition, isChecked)
                    updateEndDateState(currentList[adapterPosition])
                }
            }
        }

        private fun toggleVisibility() {
            val isDataVisible = binding.dataConstraint.isVisible
            binding.dataConstraint.isVisible = !isDataVisible
            binding.institute.text = currentList[position].school.ifEmpty { "Institute" }
            binding.institutedetailicn.rotation = if (isDataVisible) 180f else 0f
        }

        @SuppressLint("SuspiciousIndentation")
        fun bindTo(currentItem: Education) {
            binding.instituteEdittext.setText(currentItem.school)
            binding.courseEdittext.setText(currentItem.name)
            binding.gradeEdittext.setText(currentItem.location)

            if (currentItem.name.isEmpty()) {
                onEditTextCompleteListener?.requestFocusForNewItem(binding.instituteEdittext)
            }
            binding.startDateEdittext.setText(currentItem.start_at?.let { formatDate(it) })
            binding.endDateEdittext.setText(currentItem.end_at?.let { formatDate(it) })
            binding.checkboxfordate.isChecked = currentItem.present
            updateEndDateState(currentItem)
            clearErrors()
        }

        private fun setupTextWatchers() {
            binding.instituteEdittext.addTextChangedListener(createTextWatcher { text ->
                limitLength(binding.instituteEdittext, text, 50)
                onEditTextCompleteListener?.onInstituteTextChange(adapterPosition, text)
            })
            binding.courseEdittext.addTextChangedListener(createTextWatcher { text ->
                limitLength(binding.courseEdittext, text, 50)
                onEditTextCompleteListener?.onCourseTextChange(adapterPosition, text)
            })
            binding.gradeEdittext.addTextChangedListener(createTextWatcher { text ->
                limitLength(binding.gradeEdittext, text, 30)
                onEditTextCompleteListener?.onGradeTextChange(adapterPosition, text)
            })
            binding.startDateEdittext.addTextChangedListener(createTextWatcher { text ->
                validateDates()
                onEditTextCompleteListener?.onStartDateChange(adapterPosition, text)
            })
            binding.endDateEdittext.addTextChangedListener(createTextWatcher { text ->
                validateDates()
                onEditTextCompleteListener?.onEndDateChange(adapterPosition, text)
            })
        }

        private fun createTextWatcher(afterTextChanged: (String) -> Unit): TextWatcher {
            return object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        afterTextChanged(s.toString())
                    }
                }
            }
        }

        private fun limitLength(editText: EditText, text: String, maxLength: Int) {
            if (text.length > maxLength) {
                val trimmedText = text.substring(0, maxLength)
                editText.setText(trimmedText)
                editText.setSelection(trimmedText.length)
                editText.error = "Character limit exceeded ($maxLength characters max)."
            } else {
                editText.error = null
            }
        }

        private fun setupDatePickers() {

            binding.startDateEdittext.apply {
                inputType = InputType.TYPE_NULL   // Keyboard disable
            }

            binding.endDateEdittext.apply {
                inputType = InputType.TYPE_NULL   // Keyboard disable
            }

            val dateSetListener = { editText: EditText ->
                showDatePicker(editText) { selectedDate ->
                    editText.setText(selectedDate)
                }
            }
            binding.startDateEdittext.setOnClickListener {
                hideKeyboard(it)
                dateSetListener(binding.startDateEdittext)
            }
            binding.startDateEdittext.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    hideKeyboard(v)
                    dateSetListener(binding.startDateEdittext)
                }
            }

            binding.endDateEdittext.setOnClickListener {
                hideKeyboard(it)
                dateSetListener(binding.endDateEdittext)
            }
            binding.endDateEdittext.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    hideKeyboard(v)
                    dateSetListener(binding.endDateEdittext)
                }
            }

        }

        private fun showDatePicker(editText: EditText, onDateSelected: (String) -> Unit) {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
            val previousDate = editText.text.toString()
            if (previousDate.isNotEmpty()) {
                try {
                    dateFormat.parse(previousDate)?.let { calendar.time = it }
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
            datePickerUtil.showDatePickerDialog(
                calendar,
                0,
                itemView.context,
                object : DatePickerUtil.DateSelectedListener {
                    override fun onDateSelected(formattedDate: String) {
                        onDateSelected(formattedDate)
                    }
                })
        }

        private fun validateDates() {
            val startDateStr = binding.startDateEdittext.text.toString()
            val endDateStr = binding.endDateEdittext.text.toString()
            if (startDateStr.length >= 8 && endDateStr.length >= 8) {
                val dateFormat = SimpleDateFormat("MM/dd/yy", Locale.US)
                try {
                    val startDate = dateFormat.parse(startDateStr)
                    val endDate = dateFormat.parse(endDateStr)
                    if (startDate != null && endDate != null && startDate.after(endDate)) {
                        binding.startDateEdittext.error = "Start date must be after end date"
                    } else {
                        binding.startDateEdittext.error = null
                        binding.endDateEdittext.error = null
                    }
                } catch (e: ParseException) {
                    binding.startDateEdittext.error = null
                }
            } else {
                binding.startDateEdittext.error = null
            }
        }

        private fun setupImeOptions() {
            setupEditTextofAdaptors(
                binding.instituteEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                binding.courseEdittext
            )
            setupEditTextofAdaptors(
                binding.courseEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                binding.gradeEdittext
            )
            setupEditTextofAdaptors(
                binding.gradeEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                binding.startDateEdittext
            )
        }

        private fun applyCapitalization() {
            applyCapitalizeFilter(binding.instituteEdittext)
            applyCapitalizeFilter(binding.courseEdittext)
        }

        private fun updateEndDateState(currentItem: Education) {

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

        private fun clearErrors() {
            binding.instituteEdittext.error = null
            binding.courseEdittext.error = null
            binding.gradeEdittext.error = null
            binding.startDateEdittext.error = null
            binding.endDateEdittext.error = null
        }

        private fun hideKeyboard(view: View) {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<Education>() {
        override fun areItemsTheSame(oldItem: Education, newItem: Education): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Education, newItem: Education): Boolean {
            return oldItem == newItem
        }
    }
}