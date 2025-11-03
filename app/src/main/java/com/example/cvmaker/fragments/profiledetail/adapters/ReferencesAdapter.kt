package com.example.cvmaker.fragments.profiledetail.adapters

import android.content.Context
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
import com.example.cvmaker.databinding.ReferencesRowItemBinding
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import com.example.cvmaker.model.workingmodels.ReferenceModel

class ReferencesAdapter :
    ListAdapter<ReferenceModel, ReferencesAdapter.ViewHolder>(DiffCallback()) {

    private var onEditTextCompleteListener: OnEditTextCompleteListener? = null
    var context: Context? = null

    interface OnEditTextCompleteListener {
        fun onReferenceNameTextChange(position: Int, text: String)
        fun onJobTitleTextChange(position: Int, text: String)
        fun onCompanyNameTextChange(position: Int, text: String)
        fun onEmailTextChange(position: Int, text: String)
        fun onPhoneTextChange(position: Int, text: String)
        fun requestFocus(editText: EditText)
        fun onRemoveItem(position: Int, itemRemoved: () -> Unit)
    }

    fun setOnEditTextCompleteListener(listener: OnEditTextCompleteListener) {
        onEditTextCompleteListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ReferencesRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    private inner class GenericTextWatcher(private val fieldUpdater: (String) -> Unit) :
        TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            fieldUpdater.invoke(s?.toString().orEmpty())
        }
    }

    inner class ViewHolder(private val binding: ReferencesRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(item: ReferenceModel) {
            binding.nameEdittext.setText(item.name.orEmpty())
            binding.designationEdittext.setText(item.designation.orEmpty())
            binding.companyEdittext.setText(item.companyName.orEmpty())
            binding.emailEdittext.setText(item.email.orEmpty())
            binding.phoneEdittext.setText(item.phone.orEmpty())

            // Input setup
            ViewUtils.setupEditTextofAdaptors(
                binding.nameEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                binding.phoneEdittext
            )
            ViewUtils.setupEditTextofAdaptors(
                binding.phoneEdittext,
                InputType.TYPE_CLASS_PHONE,
                EditorInfo.IME_ACTION_NEXT,
                binding.emailEdittext
            )
            ViewUtils.setupEditTextofAdaptors(
                binding.emailEdittext,
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                EditorInfo.IME_ACTION_NEXT,
                binding.designationEdittext
            )
            ViewUtils.setupEditTextofAdaptors(
                binding.designationEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                binding.companyEdittext
            )
            ViewUtils.setupEditTextofAdaptors(
                binding.companyEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_DONE
            )
            ViewUtils.hideKeyboard_c(binding.phone)

            ViewUtils.applyCapitalizeFilter(binding.nameEdittext)
            ViewUtils.applyCapitalizeFilter(binding.designationEdittext)
            ViewUtils.applyCapitalizeFilter(binding.companyEdittext)

            // watchers
            binding.nameEdittext.addTextChangedListener(GenericTextWatcher { text ->
                // <=30, not starting with digit
                if (text.isNotEmpty() && text.first().isDigit()) {
                    binding.nameEdittext.setText("")
                    binding.nameEdittext.error = "Name cannot start with a number"
                    return@GenericTextWatcher
                }
                if (text.length > 30) {
                    val t = text.substring(0, 30)
                    binding.nameEdittext.setText(t)
                    binding.nameEdittext.setSelection(t.length)
                    binding.nameEdittext.error = "Character limit exceeded (30 characters max)."
                } else binding.nameEdittext.error = null

                onEditTextCompleteListener?.onReferenceNameTextChange(absoluteAdapterPosition, binding.nameEdittext.text.toString())
            })

            binding.designationEdittext.addTextChangedListener(GenericTextWatcher { text ->
                if (text.length > 30) {
                    val t = text.substring(0, 30)
                    binding.designationEdittext.setText(t)
                    binding.designationEdittext.setSelection(t.length)
                    binding.designationEdittext.error = "Character limit exceeded (30 characters max)."
                } else binding.designationEdittext.error = null

                onEditTextCompleteListener?.onJobTitleTextChange(absoluteAdapterPosition, binding.designationEdittext.text.toString())
            })

            binding.companyEdittext.addTextChangedListener(GenericTextWatcher { text ->
                if (text.length > 30) {
                    val t = text.substring(0, 30)
                    binding.companyEdittext.setText(t)
                    binding.companyEdittext.setSelection(t.length)
                    binding.companyEdittext.error = "Character limit exceeded (30 characters max)."
                } else binding.companyEdittext.error = null

                onEditTextCompleteListener?.onCompanyNameTextChange(absoluteAdapterPosition, binding.companyEdittext.text.toString())
            })

            binding.emailEdittext.addTextChangedListener(GenericTextWatcher { text ->
                if (text.isEmpty()) {
                    binding.emailErrorText.text = null
                } else if (text.length <= 34) {
                    binding.emailErrorText.text =
                        if (!text.matches(ViewUtils.emailPattern.toRegex())) "Invalid Email" else null
                } else {
                    val t = text.substring(0, 34)
                    binding.emailEdittext.setText(t)
                    binding.emailEdittext.setSelection(t.length)
                    binding.emailErrorText.text = "Character limit exceeded (30 characters max)."
                }
                onEditTextCompleteListener?.onEmailTextChange(absoluteAdapterPosition, binding.emailEdittext.text.toString())
            })

            binding.phoneEdittext.addTextChangedListener(GenericTextWatcher { text ->
                val phone = text.trim()
                if (phone.isEmpty()) {
                    binding.phoneErrorText.text = "Phone number is required"
                } else if (phone.length > 14) {
                    val t = phone.substring(0, 14)
                    binding.phoneEdittext.setText(t)
                    binding.phoneEdittext.setSelection(t.length)
                    binding.phoneErrorText.text = "Phone number should be at most 14 digits"
                } else {
                    val valid = "^[+]?[0-9]{1,14}$".toRegex().matches(phone)
                    binding.phoneErrorText.text = if (valid) null else "Invalid phone number"
                }
                onEditTextCompleteListener?.onPhoneTextChange(absoluteAdapterPosition, binding.phoneEdittext.text.toString())
            })

            // Expand/collapse and remove (if you show list items)
            binding.viewInstitutedetailicn.setOnClickListener { toggle(binding) }
            binding.removeItem.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    onEditTextCompleteListener?.onRemoveItem(absoluteAdapterPosition) {
                        binding.root.isVisible = false
                        binding.root.layoutParams.height = 0
                        binding.root.layoutParams.width = 0
                        binding.parent.layoutParams.height = 0
                    }
                }
            }
        }

        private fun toggle(binding: ReferencesRowItemBinding) {
            val isVisible = binding.dataConstraint.isVisible
            binding.dataConstraint.isVisible = !isVisible
            binding.viewInstitutedetailicn.rotation = if (isVisible) 180f else 0f
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReferenceModel>() {
        override fun areItemsTheSame(oldItem: ReferenceModel, newItem: ReferenceModel) =
            oldItem === newItem
        override fun areContentsTheSame(oldItem: ReferenceModel, newItem: ReferenceModel) =
            oldItem == newItem
    }
}
