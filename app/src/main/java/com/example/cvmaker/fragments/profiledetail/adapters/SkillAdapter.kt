package com.example.cvmaker.fragments.profiledetail.adapters

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
import com.example.cvmaker.databinding.SkillItemBinding
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.applyCapitalizeFilter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.setupEditTextofAdaptors
import com.example.cvmaker.model.workingmodels.SkillsModel

class SkillAdapter :
    ListAdapter<SkillsModel, SkillAdapter.ViewHolder>(DiffCallback()) {

    interface OnEditTextCompleteListener {
        fun requestFocusForNewItem(editText: EditText)
        fun onSkillNameTextChange(position: Int, text: String)
        fun onRatingChanged(position: Int, rating: Int)
        fun onItemRemoved(position: Int)
    }

    private var onEditTextCompleteListener: OnEditTextCompleteListener? = null
    private val expandedPositions = mutableSetOf<Int>() // only one open

    fun setOnEditTextCompleteListener(listener: OnEditTextCompleteListener) {
        onEditTextCompleteListener = listener
    }

    fun expandOnly(position: Int) {
        expandedPositions.clear()
        if (position in 0 until itemCount) expandedPositions.add(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SkillItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position), expandedPositions.contains(position))
    }

    inner class ViewHolder(private val binding: SkillItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(item: SkillsModel, isExpanded: Boolean) = with(binding) {
            // Header text
            company.text = (item.skillName ?: "").ifEmpty { "Skill Name" }

            // Editor values
            skillEdittext.setText(item.skillName ?: "")
            skillEdittext.error = null
            ratingBar.rating = (item.skillLevel ?: 0).toFloat()

            // Accordion
            dataConstraint.isVisible = isExpanded
            company.isVisible = !isExpanded
            skilldetailicn.rotation = if (isExpanded) 0f else 180f

            if (item.skillName.isNullOrEmpty()) {
                onEditTextCompleteListener?.requestFocusForNewItem(skillEdittext)
            }

            // Input config
            applyCapitalizeFilter(skillEdittext)
            setupEditTextofAdaptors(
                skillEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT
            )

            // Name watcher (30 char cap)
            skillEdittext.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val txt = s?.toString().orEmpty()
                    if (txt.length > 30) {
                        val trimmed = txt.substring(0, 30)
                        skillEdittext.setText(trimmed)
                        skillEdittext.setSelection(trimmed.length)
                        skillEdittext.error = "Character limit exceeded (30 max)."
                        onEditTextCompleteListener?.onSkillNameTextChange(absoluteAdapterPosition, trimmed)
                        // Update header if collapsed
                        if (!dataConstraint.isVisible) company.text = "Skill Name"
                    } else {
                        onEditTextCompleteListener?.onSkillNameTextChange(absoluteAdapterPosition, txt)
                        if (!dataConstraint.isVisible) {
                            company.text = if (txt.isBlank()) "Skill Name" else txt
                        }
                    }
                }
            })

            // Rating -> INT 0..5 (you enforce 1..5 at save)
            ratingBar.setOnRatingBarChangeListener { _, rf, _ ->
                onEditTextCompleteListener?.onRatingChanged(absoluteAdapterPosition, rf.toInt().coerceIn(0, 5))
            }

            // Expand/collapse
            skilldetailicn.setOnClickListener { expandOnly(absoluteAdapterPosition) }
            company.setOnClickListener { expandOnly(absoluteAdapterPosition) }

            // Remove
            removeItem.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    onEditTextCompleteListener?.onItemRemoved(absoluteAdapterPosition)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SkillsModel>() {
        override fun areItemsTheSame(oldItem: SkillsModel, newItem: SkillsModel) = oldItem === newItem
        override fun areContentsTheSame(oldItem: SkillsModel, newItem: SkillsModel) = oldItem == newItem
    }
}
