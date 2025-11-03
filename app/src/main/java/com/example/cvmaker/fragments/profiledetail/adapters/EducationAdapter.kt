package com.example.cvmaker.fragments.profiledetail.adapters

import android.annotation.SuppressLint
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cvmaker.databinding.EducationRowItemBinding
import com.example.cvmaker.fragments.profiledetail.util.DatePickerUtil
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.applyCapitalizeFilter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.setupEditTextofAdaptors
import com.example.cvmaker.model.workingmodels.EducationModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EducationAdapter :
    ListAdapter<EducationModel, EducationAdapter.ViewHolder>(DiffCallback()) {

    interface OnEditTextCompleteListener {
        fun onInstituteTextChange(position: Int, text: String)
        fun onCourseTextChange(position: Int, text: String)
        fun onGradeTextChange(position: Int, text: String)
        fun onStartDateUiChange(position: Int, uiDate: String)   // "MM/dd/yyyy"
        fun onEndDateUiChange(position: Int, uiDate: String)     // "MM/dd/yyyy" or "Present"
        fun onCheckBoxStateChanged(position: Int, isChecked: Boolean)
        fun requestFocusForNewItem(editText: EditText)
        fun removeItem(position: Int)
    }

    private var listener: OnEditTextCompleteListener? = null
    private val expandedPositions = mutableSetOf<Int>() // only one open

    fun setOnEditTextCompleteListener(l: OnEditTextCompleteListener) {
        listener = l
    }

    /** Collapse all and expand only this */
    fun expandOnly(position: Int) {
        expandedPositions.clear()
        if (position in 0 until itemCount) expandedPositions.add(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            EducationRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position), expandedPositions.contains(position))
    }

    inner class ViewHolder(private val binding: EducationRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val datePickerUtil = DatePickerUtil()
        private val uiFmt = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        private val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        @SuppressLint("SetTextI18n")
        fun bindTo(item: EducationModel, isExpanded: Boolean) = with(binding) {
            // Header line
            institute.text = (item.institute ?: "").ifEmpty { "Institute" }
            institutedetailicn.rotation = if (isExpanded) 0f else 180f

            // Editor values
            instituteEdittext.setText(item.institute.orEmpty())
            courseEdittext.setText(item.course.orEmpty())
            gradeEdittext.setText(item.grade.orEmpty())

            // Dates (adapter shows UI format; model keeps ISO)
            startDateEdittext.setText(item.startDate?.let { isoToUi(it) }.orEmpty())
            endDateEdittext.setText(
                if (item.isCurrentStudent) "Present" else item.endDate?.let { isoToUi(it) }.orEmpty()
            )

            checkboxfordate.isChecked = item.isCurrentStudent
            endDateEdittext.isEnabled = !item.isCurrentStudent
            if (item.isCurrentStudent) endDateEdittext.setText("Present")

            // Accordion visibility
            dataConstraint.isVisible = isExpanded
            title.isVisible = !isExpanded
            titledetailicn.isVisible = !isExpanded
            institutedetailicn.rotation = if (isExpanded) 0f else 180f

            // Autofocus for brand-new row
            if (item.institute.isNullOrEmpty()) {
                listener?.requestFocusForNewItem(instituteEdittext)
            }

            // Clear old errors
            instituteEdittext.error = null
            courseEdittext.error = null
            gradeEdittext.error = null
            startDateEdittext.error = null
            endDateEdittext.error = null

            // Input config
            applyCapitalizeFilter(instituteEdittext)
            applyCapitalizeFilter(courseEdittext)
            setupEditTextofAdaptors(
                instituteEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                courseEdittext
            )
            setupEditTextofAdaptors(
                courseEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                gradeEdittext
            )
            setupEditTextofAdaptors(
                gradeEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                startDateEdittext
            )

            // Disable keyboard for date fields
            startDateEdittext.inputType = InputType.TYPE_NULL
            endDateEdittext.inputType = InputType.TYPE_NULL

            // Date pickers
            val showPicker: (EditText) -> Unit = { et ->
                val cal = Calendar.getInstance()
                val prev = et.text?.toString().orEmpty()
                if (prev.isNotEmpty() && prev != "Present") {
                    try { uiFmt.parse(prev)?.let { cal.time = it } } catch (_: ParseException) {}
                }
                datePickerUtil.showDatePickerDialog(
                    cal, 0, itemView.context,
                    object : DatePickerUtil.DateSelectedListener {
                        override fun onDateSelected(formattedDate: String) {
                            et.setText(formattedDate)
                            if (et === startDateEdittext) {
                                listener?.onStartDateUiChange(absoluteAdapterPosition, formattedDate)
                            } else {
                                listener?.onEndDateUiChange(absoluteAdapterPosition, formattedDate)
                            }
                            validateDates()
                        }
                    })
            }

            startDateEdittext.setOnClickListener { showPicker(startDateEdittext) }
            startDateEdittext.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showPicker(startDateEdittext) }
            endDateEdittext.setOnClickListener { if (!checkboxfordate.isChecked) showPicker(endDateEdittext) }
            endDateEdittext.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && !checkboxfordate.isChecked) showPicker(endDateEdittext)
            }

            // Watchers
            instituteEdittext.addTextChangedListener(watch { txt ->
                val t = limit(instituteEdittext, txt, 50)
                listener?.onInstituteTextChange(absoluteAdapterPosition, t)
                if (!dataConstraint.isVisible) institute.text = t.ifEmpty { "Institute" }
            })
            courseEdittext.addTextChangedListener(watch { txt ->
                listener?.onCourseTextChange(absoluteAdapterPosition, limit(courseEdittext, txt, 50))
            })
            gradeEdittext.addTextChangedListener(watch { txt ->
                listener?.onGradeTextChange(absoluteAdapterPosition, limit(gradeEdittext, txt, 30))
            })
            startDateEdittext.addTextChangedListener(watch { txt ->
                listener?.onStartDateUiChange(absoluteAdapterPosition, txt)
                validateDates()
            })
            endDateEdittext.addTextChangedListener(watch { txt ->
                listener?.onEndDateUiChange(absoluteAdapterPosition, txt)
                validateDates()
            })

            // Expand/collapse from either chevron or header row
            institutedetailicn.setOnClickListener { expandOnly(absoluteAdapterPosition) }
            institutedetailicn.setOnClickListener { expandOnly(absoluteAdapterPosition) }
            institute.setOnClickListener { expandOnly(absoluteAdapterPosition) }

            // Remove
            removeItem.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    listener?.removeItem(absoluteAdapterPosition)
                }
            }

            // Checkbox
            checkboxfordate.setOnCheckedChangeListener { _, checked ->
                endDateEdittext.isEnabled = !checked
                if (checked) {
                    endDateEdittext.setText("Present")
                } else if (endDateEdittext.text?.toString() == "Present") {
                    endDateEdittext.setText("")
                }
                listener?.onCheckBoxStateChanged(absoluteAdapterPosition, checked)
                validateDates()
            }
        }

        private fun watch(after: (String) -> Unit) = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { after(s?.toString().orEmpty()) }
        }

        private fun limit(et: EditText, txt: String, max: Int): String {
            return if (txt.length > max) {
                val t = txt.substring(0, max)
                et.setText(t); et.setSelection(t.length)
                et.error = "Max $max characters"
                t
            } else {
                et.error = null
                txt
            }
        }

        private fun validateDates() {
            val s = binding.startDateEdittext.text?.toString().orEmpty()
            val e = binding.endDateEdittext.text?.toString().orEmpty()
            if (s.length < 8) { clearDateErrors(); return }
            if (e == "Present") { clearDateErrors(); return }
            if (e.length < 8) { clearDateErrors(); return }

            val f = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            try {
                val sd = f.parse(s)
                val ed = f.parse(e)
                if (sd != null && ed != null && ed.before(sd)) {
                    binding.startDateEdittext.error = "Start must be ≤ End"
                    binding.endDateEdittext.error = "End must be ≥ Start"
                } else {
                    clearDateErrors()
                }
            } catch (_: Exception) { clearDateErrors() }
        }

        private fun clearDateErrors() {
            binding.startDateEdittext.error = null
            binding.endDateEdittext.error = null
        }

        private fun isoToUi(iso: String): String =
            try {
                val inF = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val outF = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                inF.parse(iso)?.let { outF.format(it) } ?: ""
            } catch (_: Exception) { "" }
    }

    class DiffCallback : DiffUtil.ItemCallback<EducationModel>() {
        override fun areItemsTheSame(old: EducationModel, new: EducationModel) = old === new
        override fun areContentsTheSame(old: EducationModel, new: EducationModel) = old == new
    }
}
