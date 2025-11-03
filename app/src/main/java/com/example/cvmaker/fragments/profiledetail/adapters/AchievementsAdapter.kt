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
import com.example.cvmaker.databinding.AchievementsItemBinding
import com.example.cvmaker.model.workingmodels.AchievementModel

class AchievementsAdapter :
    ListAdapter<AchievementModel, AchievementsAdapter.ViewHolder>(DiffCallback()) {

    interface Listener {
        fun onTitleChanged(position: Int, value: String)
        fun onOrganizationChanged(position: Int, value: String)
        fun onYearChanged(position: Int, value: String)
        fun onDescriptionChanged(position: Int, value: String)
        fun onRemove(position: Int)
        fun requestFocusForNewItem(firstField: EditText)
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
            AchievementsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == expandedPosition)
    }

    inner class ViewHolder(private val binding: AchievementsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var twTitle: TextWatcher? = null
        private var twOrg: TextWatcher? = null
        private var twYear: TextWatcher? = null
        private var twDesc: TextWatcher? = null

        fun bind(item: AchievementModel, isExpanded: Boolean) {
            // Header text reflects title (fallback label)
            binding.tvTitle.text = item.title?.takeIf { it.isNotBlank() } ?: "Title"

            // Expanded state
            binding.dataConstraint.isVisible = isExpanded
            binding.viewInstitutedetailicn.rotation = if (isExpanded) 180f else 0f

            // Set field values
            binding.etTitle.setText(item.title.orEmpty())
            binding.etOrg.setText(item.organization.orEmpty())
            binding.etYearDate.setText(item.year.orEmpty())
            binding.etDesc.setText(item.description.orEmpty())

            // Detach any old watchers to avoid duplication on rebind
            twTitle?.let { binding.etTitle.removeTextChangedListener(it) }
            twOrg?.let { binding.etOrg.removeTextChangedListener(it) }
            twYear?.let { binding.etYearDate.removeTextChangedListener(it) }
            twDesc?.let { binding.etDesc.removeTextChangedListener(it) }

            // Watchers
            twTitle = watcher { text ->
                binding.tvTitle.text = if (text.isBlank()) "Title" else text
                listener?.onTitleChanged(adapterPosition, text)
            }.also { binding.etTitle.addTextChangedListener(it) }

            twOrg = watcher { text ->
                listener?.onOrganizationChanged(adapterPosition, text)
            }.also { binding.etOrg.addTextChangedListener(it) }

            twYear = watcher { text ->
                listener?.onYearChanged(adapterPosition, text)
            }.also { binding.etYearDate.addTextChangedListener(it) }

            twDesc = watcher { text ->
                listener?.onDescriptionChanged(adapterPosition, text)
            }.also { binding.etDesc.addTextChangedListener(it) }

            // Toggle expand/collapse
            binding.viewInstitutedetailicn.setOnClickListener {
                val target = if (isExpanded) RecyclerView.NO_POSITION else adapterPosition
                listener?.collapseAllExcept(target)
            }

            // Remove row
            binding.removeItem.setOnClickListener {
                listener?.onRemove(adapterPosition)
            }

            // Focus new row’s first field if empty and expanded
            if (isExpanded && item.title.isNullOrBlank()) {
                listener?.requestFocusForNewItem(binding.etTitle)
            }
        }

        private fun watcher(onAfter: (String) -> Unit) = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onAfter(s?.toString().orEmpty())
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AchievementModel>() {
        override fun areItemsTheSame(oldItem: AchievementModel, newItem: AchievementModel) =
            oldItem === newItem
        override fun areContentsTheSame(oldItem: AchievementModel, newItem: AchievementModel) =
            oldItem == newItem
    }
}
