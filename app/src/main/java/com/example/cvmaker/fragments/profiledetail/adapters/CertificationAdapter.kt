package com.example.cvmaker.fragments.profiledetail.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cvmaker.databinding.CertificationItemBinding
import com.example.cvmaker.model.workingmodels.CertificationModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class CertificationAdapter :
    ListAdapter<CertificationModel, CertificationAdapter.ViewHolder>(DiffCallback()) {

    interface Listener {
        fun onCourseChanged(position: Int, value: String)
        fun onInstituteChanged(position: Int, value: String)
        fun onGradeChanged(position: Int, value: String)
        fun onStartDateChanged(position: Int, value: String)
        fun onEndDateChanged(position: Int, value: String)
        fun onCurrentStudentToggled(position: Int, isChecked: Boolean)
        fun onRemove(position: Int)
        fun requestFocusForNewItem(editText: EditText)
        fun collapseAllExcept(position: Int) // -1 to collapse all
    }

    private var listener: Listener? = null
    fun setListener(l: Listener) { listener = l }

    private var expandedPosition: Int = RecyclerView.NO_POSITION
    fun expandOnly(position: Int) {
        val previous = expandedPosition
        expandedPosition = position
        if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
        if (position != RecyclerView.NO_POSITION) notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CertificationItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == expandedPosition)
    }

    inner class ViewHolder(private val binding: CertificationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var twCourse: TextWatcher? = null
        private var twInstitute: TextWatcher? = null
        private var twGrade: TextWatcher? = null

        private val dateFormat = SimpleDateFormat("MM/yy", Locale.getDefault())

        fun bind(item: CertificationModel, isExpanded: Boolean) {
            // Header text
            val header = if (item.course.isNullOrBlank()) "Course / Degree" else item.course
            binding.tvCourse.text = header

            // Expand/collapse
            binding.dataConstraint.isVisible = isExpanded
            binding.viewInstitutedetailicn.rotation = if (isExpanded) 180f else 0f

            // Set values
            binding.etCourse.setText(item.course.orEmpty())
            binding.etInstitute.setText(item.institute.orEmpty())
            binding.etGrades.setText(item.grade.orEmpty())
            binding.etStartDate.setText(item.startDate.orEmpty())
            binding.etEndDate.setText(item.endDate.orEmpty())
            binding.cbCurrentStudent.isChecked = item.isCurrentStudent

            // End Date visibility & enabled state
            binding.etEndDate.isEnabled = !item.isCurrentStudent
            binding.etEndDate.alpha = if (item.isCurrentStudent) 0.5f else 1f

            // Remove old watchers
            twCourse?.let { binding.etCourse.removeTextChangedListener(it) }
            twInstitute?.let { binding.etInstitute.removeTextChangedListener(it) }
            twGrade?.let { binding.etGrades.removeTextChangedListener(it) }

            // Text watchers
            twCourse = makeWatcher { text ->
                binding.tvCourse.text = text.ifBlank { "Course / Degree" }
                listener?.onCourseChanged(adapterPosition, text)
            }.also { binding.etCourse.addTextChangedListener(it) }

            twInstitute = makeWatcher { text ->
                listener?.onInstituteChanged(adapterPosition, text)
            }.also { binding.etInstitute.addTextChangedListener(it) }

            twGrade = makeWatcher { text ->
                listener?.onGradeChanged(adapterPosition, text)
            }.also { binding.etGrades.addTextChangedListener(it) }

            // Date Pickers
            setupDatePicker(binding.etStartDate) { millis ->
                val formatted = dateFormat.format(Date(millis))
                binding.etStartDate.setText(formatted)
                listener?.onStartDateChanged(adapterPosition, formatted)
            }

            setupDatePicker(binding.etEndDate) { millis ->
                if (!binding.etEndDate.isEnabled) return@setupDatePicker
                val formatted = dateFormat.format(Date(millis))
                binding.etEndDate.setText(formatted)
                listener?.onEndDateChanged(adapterPosition, formatted)
            }

            // Toggle expand/collapse
            binding.viewInstitutedetailicn.setOnClickListener {
                val target = if (isExpanded) RecyclerView.NO_POSITION else adapterPosition
                listener?.collapseAllExcept(target)
            }

            // Remove item
            binding.removeItem.setOnClickListener {
                listener?.onRemove(adapterPosition)
            }

            // Auto-focus first field when new empty item is expanded
            if (isExpanded && item.course.isNullOrBlank()) {
                listener?.requestFocusForNewItem(binding.etCourse)
            }

            // Current student checkbox
            binding.cbCurrentStudent.setOnCheckedChangeListener(null)
            binding.cbCurrentStudent.setOnCheckedChangeListener { _, isChecked ->
                binding.etEndDate.isEnabled = !isChecked
                binding.etEndDate.alpha = if (isChecked) 0.5f else 1f
                if (isChecked) {
                    binding.etEndDate.setText("")
                    listener?.onEndDateChanged(adapterPosition, "")
                }
                listener?.onCurrentStudentToggled(adapterPosition, isChecked)
            }
        }

        private fun setupDatePicker(editText: EditText, onDateSelected: (Long) -> Unit) {
            val clickListener = {
                val picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

                picker.addOnPositiveButtonClickListener { selection ->
                    onDateSelected(selection)
                }

                // Safely get FragmentManager
                val activity = editText.context as? AppCompatActivity
                activity?.supportFragmentManager?.let {
                    picker.show(it, "DATE_PICKER_${adapterPosition}")
                }
            }

            editText.setOnClickListener { clickListener() }
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && editText.isEnabled) {
                    clickListener()
                }
            }
        }

        private fun makeWatcher(onAfter: (String) -> Unit) = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onAfter(s?.toString().orEmpty())
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CertificationModel>() {
        override fun areItemsTheSame(old: CertificationModel, new: CertificationModel) =
            old === new

        override fun areContentsTheSame(old: CertificationModel, new: CertificationModel) =
            old == new
    }
}