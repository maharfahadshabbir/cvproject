package com.example.cvmaker.fragments.profiledetail.adapters

import android.annotation.SuppressLint
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cvmaker.databinding.LanguageModelItemBinding
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import com.example.cvmaker.model.workingmodels.LanguageModel

class LanguageAdapter :
    ListAdapter<LanguageModel, LanguageAdapter.ViewHolder>(DiffCallback()) {

    interface Listener {
        fun onLanguageTextChange(position: Int, text: String)
        fun onLevelChanged(position: Int, level: String)
        fun onRemove(position: Int)
        fun requestFocusForNewItem(editText: EditText)
        fun collapseAllExcept(position: Int)
    }

    private var listener: Listener? = null
    fun setListener(l: Listener) { listener = l }

    // Which adapter position is currently expanded (accordion style)
    private var expandedPosition: Int = RecyclerView.NO_POSITION

    fun expandOnly(position: Int) {
        val previous = expandedPosition
        expandedPosition = position
        if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LanguageModelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position, position == expandedPosition)
    }

    inner class ViewHolder(private val binding: LanguageModelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var textWatcher: TextWatcher? = null

        fun bind(item: LanguageModel, position: Int, isExpanded: Boolean) {
            // Header title
            binding.tvLanguage.text = item.languageName?.takeIf { it.isNotBlank() } ?: "Language"

            // Expand/Collapse block (accordion)
            binding.dataConstraint.isVisible = isExpanded
            binding.viewInstitutedetailicn.rotation = if (isExpanded) 180f else 0f

            // Language EditText
            binding.etLanguage.setText(item.languageName ?: "")

            // Helpers like your other fragments
            ViewUtils.applyCapitalizeFilter(binding.etLanguage)
            ViewUtils.setupEditTextofAdaptors(
                binding.etLanguage,
                InputType.TYPE_CLASS_TEXT,
                KeyEvent.KEYCODE_ENTER
            )

            // Avoid duplicate watchers
            textWatcher?.let { binding.etLanguage.removeTextChangedListener(it) }
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString().orEmpty()
                    listener?.onLanguageTextChange(adapterPosition, text)
                    binding.tvLanguage.text = if (text.isBlank()) "Language" else text
                }
            }
            binding.etLanguage.addTextChangedListener(textWatcher)

            // Level UI
            val level = (item.level ?: "Novice")
            setLevelTitle(level)
            setLevelChecks(level)

            // Toggle level list
            binding.levelHeader.setOnClickListener {
                val newExpanded = adapterPosition
                listener?.collapseAllExcept(newExpanded)
            }
            binding.ivLevelToggle.setOnClickListener {
                val newExpanded = adapterPosition
                listener?.collapseAllExcept(newExpanded)
            }

            // Exclusive checks (radio behavior using CheckBoxes)
            binding.cbNovice.setOnClickListener { selectLevel("Novice") }
            binding.cbBeginner.setOnClickListener { selectLevel("Beginner") }
            binding.cbProficient.setOnClickListener { selectLevel("Proficient") }
            binding.cbExpert.setOnClickListener { selectLevel("Expert") }

            // Row collapse/expand from header chevron on top row
            binding.viewInstitutedetailicn.setOnClickListener {
                val newExpanded = if (isExpanded) RecyclerView.NO_POSITION else adapterPosition
                listener?.collapseAllExcept(newExpanded)
            }

            // Remove
            binding.removeItem.setOnClickListener { listener?.onRemove(adapterPosition) }

            // Focus new item
            if (item.languageName.isNullOrBlank() && isExpanded) {
                listener?.requestFocusForNewItem(binding.etLanguage)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setLevelTitle(level: String) {
            binding.tvLevelTitle.text = "Level [$level]"
        }

        private fun setLevelChecks(level: String) {
            val pairs = listOf(
                "Novice" to binding.cbNovice,
                "Beginner" to binding.cbBeginner,
                "Proficient" to binding.cbProficient,
                "Expert" to binding.cbExpert
            )
            pairs.forEach { (name, cb) -> cb.isChecked = name == level }
        }

        private fun selectLevel(level: String) {
            setLevelTitle(level)
            setLevelChecks(level)
            listener?.onLevelChanged(adapterPosition, level)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<LanguageModel>() {
        override fun areItemsTheSame(oldItem: LanguageModel, newItem: LanguageModel): Boolean =
            oldItem === newItem || (oldItem.languageName == newItem.languageName && oldItem.level == newItem.level)

        override fun areContentsTheSame(oldItem: LanguageModel, newItem: LanguageModel): Boolean =
            oldItem == newItem
    }
}
