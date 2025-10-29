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
import com.example.cvmaker.model.profilemodels.Interest
import com.example.cvmaker.viewmodels.SharedViewModel

class InterestAdapter() :
    ListAdapter<Interest, InterestAdapter.ViewHolder>(DiffCallback()) {

    lateinit var binding: InterestItemBinding
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InterestAdapter.ViewHolder {
        binding = InterestItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterestAdapter.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bindTo(currentItem)
    }

    inner class ViewHolder(val binding: InterestItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(currentItem: Interest) {

            binding.interestEdittext.setText(currentItem?.name ?: "")





            ViewUtils.applyCapitalizeFilter(binding.interestEdittext)


            ViewUtils.setupEditTextofAdaptors(
                binding.interestEdittext,
                InputType.TYPE_CLASS_TEXT,
                KeyEvent.KEYCODE_ENTER
            )


            binding.viewInstitutedetailicn.setOnClickListener {
                //hide data
                toggleVisibility()
            }
            binding.removeItem.setOnClickListener {
                onEditTextCompleteListener?.onRemoveItem(adapterPosition)
            }


            try {
                if (currentItem.name.isEmpty()) {
                    onEditTextCompleteListener?.requestFocusForNewItem(binding.interestEdittext)
                }
            } catch (e: Exception) {

            }



            //
            binding.interestEdittext.addTextChangedListener(object : TextWatcher {
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
                        onEditTextCompleteListener?.onInterestTextChange(
                            adapterPosition,
                            newText.toString()
                        )
                        if (newText.length <= 30) {
                            if (!(ViewUtils.validateInput(newText.toString()))) {
                                binding.interestEdittext.error = "Invalid input"
                            } else {
                                binding.interestEdittext.error = null
                            }
                        } else {

                            // Trim the input to 30 characters
                            val trimmedText = newText.substring(0, 30)
                            binding.interestEdittext.setText(trimmedText)
                            // Set the cursor to the end of the trimmed text
                            binding.interestEdittext.setSelection(trimmedText.length)
                            binding.interestEdittext.error =
                                "Character limit exceeded (30 characters max)."
                        }
                    }
                }
            })

        }
        private fun toggleVisibility() {
            val isDataVisible = binding.dataConstraint.isVisible
            binding.dataConstraint.isVisible = !isDataVisible
            binding.interest.text = currentList[position].name.ifEmpty { "Interest" }
            binding.viewInstitutedetailicn.rotation = if (isDataVisible) 180f else 0f
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Interest>() {
        override fun areItemsTheSame(oldItem: Interest, newItem: Interest) =
            oldItem.hashCode() == newItem.hashCode()

        override fun areContentsTheSame(oldItem: Interest, newItem: Interest) =
            oldItem == newItem
    }



    fun setLastAddedPosition(position: Int) {
        lastAddedPosition = position
        notifyItemChanged(position)
    }
}