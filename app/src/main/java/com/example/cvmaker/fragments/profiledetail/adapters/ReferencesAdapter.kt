package com.example.cvmaker.fragments.profiledetail.adapters


import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cvmaker.R
import com.example.cvmaker.databinding.ReferencesRowItemBinding
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import com.example.cvmaker.model.profilemodels.Reference
import com.example.cvmaker.utils.tryCatch

class ReferencesAdapter() :
    ListAdapter<Reference, ReferencesAdapter.ViewHolder>(DiffCallback()) {


    lateinit var binding: ReferencesRowItemBinding
    var context: Context? = null

    val msg = "Invalid Input"

    private var onEditTextCompleteListener: OnEditTextCompleteListener? = null


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
            ReferencesRowItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bindTo(currentItem)
    }


    private inner class GenericTextWatcher(private val fieldUpdater: (String) -> Unit) :
        TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Not used in this example
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Not used in this example
        }

        override fun afterTextChanged(s: Editable?) {
            // Notify the listener when EditText changes are completed
            s?.let { fieldUpdater.invoke(it.toString()) }
        }
    }


    inner class ViewHolder(private val binding: ReferencesRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindTo(currentItem: Reference) {
            Log.i("currentList", "bindTo:called ")
            // Check if the item is not null
            currentItem?.let {
                // Bind data to the views here
                binding.nameEdittext.setText(it.name)
                binding.designationEdittext.setText(it.designation)
                binding.companyEdittext.setText(it.company_name)
                binding.emailEdittext.setText(it.email)
                binding.phoneEdittext.setText(it.phone)
                binding.nameEdittext.error = null
                binding.designationEdittext.error = null
                binding.companyEdittext.error = null
                binding.emailEdittext.error = null
                binding.phoneEdittext.error = null
                tryCatch {
                    if (currentItem.name.isEmpty()) {
                        onEditTextCompleteListener?.requestFocus(binding.nameEdittext)
                    }
                }

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
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    EditorInfo.IME_ACTION_NEXT,
                    binding.companyEdittext
                )

                ViewUtils.setupEditTextofAdaptors(
                    binding.companyEdittext,
                    InputType.TYPE_CLASS_TEXT,
                    EditorInfo.IME_ACTION_DONE
                )
                ViewUtils.hideKeyboard_c(binding.phone)
// Apply the InputFilter to the name field
                ViewUtils.applyCapitalizeFilter(binding.nameEdittext)
                ViewUtils.applyCapitalizeFilter(binding.designationEdittext)
                ViewUtils.applyCapitalizeFilter(binding.companyEdittext)


                // Set listeners for EditText changes
                binding.nameEdittext.addTextChangedListener(GenericTextWatcher { newText ->
                    if (newText.length <= 30) {
                        if (newText.isNotEmpty()) {
                            if (newText[0] == '1' || newText[0] == '2' || newText[0] == '3' || newText[0] == '4' || newText[0] == '5'
                                || newText[0] == '6' || newText[0] == '7' || newText[0] == '8' || newText[0] == '9' || newText[0] == '0'
                            ) {
                                binding.nameEdittext.setText("")
                                binding.nameEdittext.error = "Name cannot start with a number"
                                return@GenericTextWatcher
                            }
                        }

                    } else {
                        // Trim the input to 30 characters
                        val trimmedText = newText.substring(0, 30)

                        if (trimmedText[0] == '1' || trimmedText[0] == '2' || trimmedText[0] == '3' || trimmedText[0] == '4' || trimmedText[0] == '5'
                            || trimmedText[0] == '6' || trimmedText[0] == '7' || trimmedText[0] == '8' || trimmedText[0] == '9' || trimmedText[0] == '0'
                        ) {
                            binding.nameEdittext.error = "Name cannot start with a number"
                            return@GenericTextWatcher
                        }
                        binding.nameEdittext.setText(trimmedText)

                        // Set the cursor to the end of the trimmed text
                        binding.nameEdittext.setSelection(trimmedText.length)
                        binding.nameEdittext.error = "Character limit exceeded (30 characters max)."
                    }
                    onEditTextCompleteListener?.onReferenceNameTextChange(adapterPosition, newText)
                })

                binding.designationEdittext.addTextChangedListener(GenericTextWatcher { newText ->
                    if (newText.length <= 30) {
//                        if (ViewUtils.validateInput(newText)) {
//                            binding.designationEdittext.error = null
//                        } else {
//                            binding.designationEdittext.error = "Invalid input"
//                        }
                    } else {

                        // Trim the input to 30 characters
                        val trimmedText = newText.substring(0, 30)
                        binding.designationEdittext.setText(trimmedText)
                        // Set the cursor to the end of the trimmed text
                        binding.designationEdittext.setSelection(trimmedText.length)
                        binding.designationEdittext.error =
                            "Character limit exceeded (30 characters max)."
                    }

                    onEditTextCompleteListener?.onJobTitleTextChange(
                        adapterPosition,
                        binding.designationEdittext.text.toString()
                    )
                })


                binding.companyEdittext.addTextChangedListener(GenericTextWatcher { newText ->
                    if (newText.length <= 30) {
//                        if (ViewUtils.validateInput(newText)) {
//                            binding.companyEdittext.error = null
//                        } else {
//                            binding.companyEdittext.error = "Invalid input"
//                        }
                    } else {

                        // Trim the input to 30 characters
                        val trimmedText = newText.substring(0, 30)
                        binding.companyEdittext.setText(trimmedText)
                        // Set the cursor to the end of the trimmed text
                        binding.companyEdittext.setSelection(trimmedText.length)
                        binding.companyEdittext.error =
                            "Character limit exceeded (30 characters max)."
                    }
                    onEditTextCompleteListener?.onCompanyNameTextChange(adapterPosition, newText)
                })
                binding.emailEdittext.addTextChangedListener(GenericTextWatcher { newText ->

//
//                    if (newText.length <= 40) {
//
//                        if (newText.matches(ViewUtils.emailPattern.toRegex())) {
//
//                            binding.emailEdittext.error = null
//                        } else {
//
//                            // Email format is invalid, show an error
//                            binding.emailEdittext.error = "Invalid email"
//                        }
//                        if (ViewUtils.error.isNotEmpty()) {
//
//                            binding.emailEdittext.error = ViewUtils.error
//
//                        }else{
//                            binding.emailEdittext.error = null
//                        }
//                    } else {
//
//                        // Trim the input to 30 characters
//                        val trimmedText = newText.substring(0, 30)
//                        binding.emailEdittext.setText(trimmedText)
//                        // Set the cursor to the end of the trimmed text
//                        binding.emailEdittext.setSelection(trimmedText.length)
//                        binding.emailEdittext.error = "Character limit exceeded (40 characters max)."
//                    }

                    if (newText.isEmpty()) {
                        binding.emailErrorText.text = null
                    }

                    if (newText.isNotEmpty()) {

                        if (binding.emailEdittext.text.length < 35) {

                            if (!newText.matches(ViewUtils.emailPattern.toRegex())) {
                                // Phone number format is invalid, show an error
                                binding.emailErrorText.text = "Invalid Email"
                            } else {
                                binding.emailErrorText.text = null

                            }

                        } else {

                            // Trim the input to 30 characters
                            val trimmedText = binding.emailEdittext.text.substring(0, 34)
                            binding.emailEdittext.setText(trimmedText)
                            // Set the cursor to the end of the trimmed text
                            binding.emailEdittext.setSelection(trimmedText.length)
                            binding.emailErrorText.text =
                                "Character limit exceeded (30 characters max)."
                        }

                    } else {
                        // binding!!.emailEdittext.error = "Email is Required"
                    }






                    onEditTextCompleteListener?.onEmailTextChange(adapterPosition, newText)
                })

                binding.phoneEdittext.addTextChangedListener(GenericTextWatcher { newText ->
                    val phoneText = binding.phoneEdittext.text.toString().trim()

                    if (phoneText.isNotEmpty()) {
                        if (phoneText.length < 15) {
                            // Check if the phone number contains only digits or starts with '+'
                            if (phoneText.firstOrNull() == '+' || phoneText.firstOrNull() == '0' || phoneText.all { it.isDigit() }) {
                                binding.phoneErrorText.text = null
                            } else {
                                binding.phoneErrorText.text =
                                    "Phone number should contain only digits"
                            }
                        } else {


                            if ((phoneText.all { it.isDigit() })) {
                                binding.phoneErrorText.text =
                                    "Phone number should be at most 14 digits"
                            }
                            // Check if the phone number is valid (contains only digits and optionally starts with +)
                            val validPhoneNumberRegex = "^[+]?[0-9]{1,14}$".toRegex()

                            if (validPhoneNumberRegex.matches(phoneText)) {
                                binding.phoneErrorText.text = null // Valid phone number
                            } else {
                                binding.phoneErrorText.text = "Invalid phone number"
                            }
                            // Trim the input to the maximum allowed length
                            val trimmedText =
                                phoneText.substring(0, 14) // Adjust the maximum length as needed
                            binding.phoneEdittext.setText(trimmedText)
                            // Set the cursor to the end of the trimmed text
                            binding.phoneEdittext.setSelection(trimmedText.length)
                            binding.phoneErrorText.text = "Phone number should be at most 14 digits"

                        }
                    } else {
                        // Phone number is required
                        binding.phoneErrorText.text = "Phone number is required"
                    }
                    onEditTextCompleteListener?.onPhoneTextChange(adapterPosition, newText)
                })

                context = itemView.context
                // Set a click listener on the "comapnydetailicn" icon
                binding.viewInstitutedetailicn.setOnClickListener {
                    toggleVisibility()
                }
                binding.removeItem.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onEditTextCompleteListener?.onRemoveItem(adapterPosition) {
                            Log.i("currentList", "bindTo:${currentList.size} ")
                            binding.root.isVisible = false
                            binding.root.layoutParams.height = 0
                            binding.root.layoutParams.width = 0
                            binding.parent.layoutParams.height = 0
                        }
                    }
                }
            }
        }

        private fun toggleVisibility() {
            val isDataVisible = binding.dataConstraint.isVisible
            binding.dataConstraint.isVisible = !isDataVisible
            binding.name.text = currentList[position].name.ifEmpty { "Name" }
            binding.viewInstitutedetailicn.rotation = if (isDataVisible) 180f else 0f
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Reference>() {
        override fun areItemsTheSame(oldItem: Reference, newItem: Reference) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Reference, newItem: Reference) =
            oldItem == newItem
    }


    // Function to update visibility of views
    private fun hideData(
        binding: ReferencesRowItemBinding, currentItem: Reference
    ) {

        if (binding.nameEdittext.text.isNotEmpty()) {
            binding.title.isVisible = true
            binding.title.text = binding.nameEdittext.text.toString()
            binding.titledetailicn.setBackgroundResource(R.drawable.hide_icon)
            binding.titledetailicn.isVisible = true
            binding.dataConstraint.isVisible = false
        } else {
            binding.title.text = context?.getString(R.string.title)
            binding.title.isVisible = true
            binding.titledetailicn.setBackgroundResource(R.drawable.hide_icon)
            binding.titledetailicn.isVisible = true
            binding.dataConstraint.isVisible = false
        }

    }

    // Function to update visibility of views
    private fun showData(
        binding: ReferencesRowItemBinding, currentItem: Reference
    ) {
        // Show company-related views and hide title-related views
        binding.title.isVisible = false
        binding.titledetailicn.isVisible = false
        binding.dataConstraint.isVisible = true
    }
}