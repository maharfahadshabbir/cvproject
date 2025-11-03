package com.example.cvmaker.fragments.profiledetail.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cvmaker.databinding.CertificationItemBinding
import com.example.cvmaker.model.workingmodels.CertificationModel

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
        fun collapseAllExcept(position: Int) // pass -1 to collapse all
    }

    private var listener: Listener? = null
    fun setListener(l: Listener) { listener = l }

    // Single-expanded accordion behavior
    private var expandedPosition: Int = RecyclerView.NO_POSITION
    fun expandOnly(position: Int) {
        val previous = expandedPosition
        expandedPosition = position
        if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
        if (position != RecyclerView.NO_POSITION) notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            CertificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        private var twStart: TextWatcher? = null
        private var twEnd: TextWatcher? = null

        fun bind(item: CertificationModel, isExpanded: Boolean) {
            // Header label
            val header = when {
                !item.course.isNullOrBlank() -> item.course
                else -> "Course / Degree"
            }
            binding.tvCourse.text = header

            // Expand / collapse block
            binding.dataConstraint.isVisible = isExpanded
            binding.viewInstitutedetailicn.rotation = if (isExpanded) 180f else 0f

            // Set field values
            binding.etCourse.setText(item.course.orEmpty())
            binding.etInstitute.setText(item.institute.orEmpty())
            binding.etGrades.setText(item.grade.orEmpty())
            binding.etStartDate.setText(item.startDate.orEmpty())
            binding.etEndDate.setText(item.endDate.orEmpty())
            binding.cbCurrentStudent.isChecked = item.isCurrentStudent

            // If current student is checked, visually disable End Date
            binding.etEndDate.isEnabled = !item.isCurrentStudent
            binding.etEndDate.alpha = if (item.isCurrentStudent) 0.5f else 1f

            // Clear existing watchers to avoid duplicates on rebind
            twCourse?.let { binding.etCourse.removeTextChangedListener(it) }
            twInstitute?.let { binding.etInstitute.removeTextChangedListener(it) }
            twGrade?.let { binding.etGrades.removeTextChangedListener(it) }
            twStart?.let { binding.etStartDate.removeTextChangedListener(it) }
            twEnd?.let { binding.etEndDate.removeTextChangedListener(it) }

            // Watchers
            twCourse = makeWatcher { text ->
                binding.tvCourse.text = if (text.isBlank()) "Course / Degree" else text
                listener?.onCourseChanged(adapterPosition, text)
            }.also { binding.etCourse.addTextChangedListener(it) }

            twInstitute = makeWatcher { text ->
                listener?.onInstituteChanged(adapterPosition, text)
            }.also { binding.etInstitute.addTextChangedListener(it) }

            twGrade = makeWatcher { text ->
                listener?.onGradeChanged(adapterPosition, text)
            }.also { binding.etGrades.addTextChangedListener(it) }

            twStart = makeWatcher { text ->
                listener?.onStartDateChanged(adapterPosition, text)
            }.also { binding.etStartDate.addTextChangedListener(it) }

            twEnd = makeWatcher { text ->
                listener?.onEndDateChanged(adapterPosition, text)
            }.also { binding.etEndDate.addTextChangedListener(it) }

            // Toggle expanded
            binding.viewInstitutedetailicn.setOnClickListener {
                val target = if (isExpanded) RecyclerView.NO_POSITION else adapterPosition
                listener?.collapseAllExcept(target)
            }

            // Remove row
            binding.removeItem.setOnClickListener {
                listener?.onRemove(adapterPosition)
            }

            // Focus the new item’s first field if it’s empty and expanded
            if (isExpanded && item.course.isNullOrBlank()) {
                listener?.requestFocusForNewItem(binding.etCourse)
            }

            // Current student checkbox
            binding.cbCurrentStudent.setOnCheckedChangeListener(null)
            binding.cbCurrentStudent.setOnCheckedChangeListener { _, checked ->
                // If checked, clear/disable end date
                if (checked) {
                    binding.etEndDate.setText("")
                }
                binding.etEndDate.isEnabled = !checked
                binding.etEndDate.alpha = if (checked) 0.5f else 1f
                listener?.onCurrentStudentToggled(adapterPosition, checked)
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
        override fun areItemsTheSame(oldItem: CertificationModel, newItem: CertificationModel): Boolean =
            oldItem === newItem

        override fun areContentsTheSame(oldItem: CertificationModel, newItem: CertificationModel): Boolean =
            oldItem == newItem
    }
}
