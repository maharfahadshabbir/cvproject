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
import com.example.cvmaker.model.profilemodels.OtherSkill
import com.example.cvmaker.utils.tryCatch


class SkillAdapter() : ListAdapter<OtherSkill, SkillAdapter.ViewHolder>(DiffCallback()) {

    // Declare the binding property
    private lateinit var binding: SkillItemBinding

    private var onEditTextCompleteListener: OnEditTextCompleteListener? = null

    interface OnEditTextCompleteListener {
        fun requestFocusForNewItem(editText: EditText)
        fun onSkillNameTextChange(position: Int, text: String)
        fun onRatingChanged(position: Int, rating: Float)

        fun onItemRemoved(position: Int)

    }


    fun setOnEditTextCompleteListener(listener: OnEditTextCompleteListener) {
        onEditTextCompleteListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = SkillItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bindTo(currentItem)


    }

    inner class ViewHolder(val binding: SkillItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(currentItem: OtherSkill) {
            binding.apply {
                skillEdittext.setText(currentItem.name)
                skillEdittext.error = null
                binding.ratingBar.rating = currentItem.rating.toFloat()

                // If the item is newly added (e.g., name is empty), request focus.


                tryCatch {
                    if (currentItem.name.isEmpty()) {
                        onEditTextCompleteListener?.requestFocusForNewItem(skillEdittext)
                    }
                }


                // Apply the InputFilter to the name field
                applyCapitalizeFilter(skillEdittext)


                setupEditTextofAdaptors(
                    binding.skillEdittext,
                    InputType.TYPE_CLASS_TEXT,
                    EditorInfo.IME_ACTION_NEXT
                )



                ratingBar.setOnRatingBarChangeListener { _, rating, _ ->

                    // onRatingBarChangeListener?.onRatingChanged(adapterPosition, rating)

                    onEditTextCompleteListener?.onRatingChanged(
                        adapterPosition,
                        rating
                    )


                }
                skillEdittext.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        s?.let { newText ->
                            onEditTextCompleteListener?.onSkillNameTextChange(
                                adapterPosition,
                                newText.toString()
                            )


                            if (newText.isNotEmpty()) {
                                // Enable radio buttons when the skill name is not empty

                                //enableRadioButtons()

                                if (newText.length <= 30) {

                                } else {

                                    // Trim the input to 30 characters
                                    val trimmedText = newText.substring(0, 30)
                                    binding.skillEdittext.setText(trimmedText)
                                    // Set the cursor to the end of the trimmed text
                                    binding.skillEdittext.setSelection(trimmedText.length)
                                    binding.skillEdittext.error =
                                        "Character limit exceeded (30 characters max)."
                                }

                            } else {

                            }
                        }
                    }
                })
                skilldetailicn.setOnClickListener {
                    toggleVisibility()
                }
                removeItem.setOnClickListener {
                    onEditTextCompleteListener?.onItemRemoved(adapterPosition)
                }

            }
        }

        private fun toggleVisibility() {
            val isDataVisible = binding.dataConstraint.isVisible
            binding.dataConstraint.isVisible = !isDataVisible
            binding.company.text = currentList[position].name.ifEmpty { "Skill Name" }
            binding.skilldetailicn.rotation = if (isDataVisible) 180f else 0f
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<OtherSkill>() {
        override fun areItemsTheSame(oldItem: OtherSkill, newItem: OtherSkill) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OtherSkill, newItem: OtherSkill) =
            oldItem == newItem
    }


}

