package com.example.cvmaker.fragments.profiledetail.adapters

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
import com.example.cvmaker.databinding.InterestItemBinding
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import com.example.cvmaker.model.workingmodels.InterestModel
import com.example.cvmaker.viewmodels.SharedViewModel

class InterestAdapter : ListAdapter<InterestModel, InterestAdapter.ViewHolder>(DiffCallback()) {

    private var onEditTextCompleteListener: OnEditTextCompleteListener? = null
    private lateinit var sharedViewModel: SharedViewModel
    private var lastAddedPosition = -1

    fun setSharedViewModel(viewModel: SharedViewModel) {
        sharedViewModel = viewModel
    }

    interface OnEditTextCompleteListener {
        fun onInterestTextChange(position: Int, text: String)
        fun requestFocusForNewItem(editText: EditText)
        fun onRemoveItem(position: Int)
    }

    fun setOnEditTextCompleteListener(listener: OnEditTextCompleteListener) {
        onEditTextCompleteListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = InterestItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    inner class ViewHolder(private val binding: InterestItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(currentItem: InterestModel) {
            binding.interestEdittext.setText(currentItem.interestName ?: "")

            ViewUtils.applyCapitalizeFilter(binding.interestEdittext)
            ViewUtils.setupEditTextofAdaptors(
                binding.interestEdittext,
                InputType.TYPE_CLASS_TEXT,
                KeyEvent.KEYCODE_ENTER
            )

            binding.viewInstitutedetailicn.setOnClickListener { toggleVisibility() }
            binding.removeItem.setOnClickListener {
                onEditTextCompleteListener?.onRemoveItem(adapterPosition)
            }

            if (currentItem.interestName.isNullOrEmpty()) {
                onEditTextCompleteListener?.requestFocusForNewItem(binding.interestEdittext)
            }

            binding.interestEdittext.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    s?.let { newText ->
                        val text = newText.toString()
                        onEditTextCompleteListener?.onInterestTextChange(adapterPosition, text)

                        if (text.length > 30) {
                            val trimmed = text.substring(0, 30)
                            binding.interestEdittext.setText(trimmed)
                            binding.interestEdittext.setSelection(trimmed.length)
                            binding.interestEdittext.error = "Max 30 characters allowed"
                        } else if (!ViewUtils.validateInput(text)) {
                            binding.interestEdittext.error = "Invalid input"
                        } else {
                            binding.interestEdittext.error = null
                        }
                    }
                }
            })
        }

        private fun toggleVisibility() {
            val visible = binding.dataConstraint.isVisible
            binding.dataConstraint.isVisible = !visible
            val title = currentList[adapterPosition].interestName
            binding.interest.text = if (title.isNullOrBlank()) "Interest" else title
            binding.viewInstitutedetailicn.rotation = if (visible) 180f else 0f
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<InterestModel>() {
        override fun areItemsTheSame(oldItem: InterestModel, newItem: InterestModel) =
            oldItem.hashCode() == newItem.hashCode()

        override fun areContentsTheSame(oldItem: InterestModel, newItem: InterestModel) =
            oldItem == newItem
    }

    fun setLastAddedPosition(position: Int) {
        lastAddedPosition = position
        notifyItemChanged(position)
    }
}
