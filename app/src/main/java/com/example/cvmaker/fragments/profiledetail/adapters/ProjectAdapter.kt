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
import com.example.cvmaker.databinding.ProjectItemBinding
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import com.example.cvmaker.model.profilemodels.Project
import com.example.cvmaker.utils.tryCatch


class ProjectAdapter() :
    ListAdapter<Project, ProjectAdapter.ViewHolder>(DiffCallback()) {


    lateinit var binding: ProjectItemBinding

    private var isBoldClicked = true
    private var isItalicClicked = true
    private var isUnderlineClicked = true

    private var onEditTextCompleteListener: OnEditTextCompleteListener? = null




    interface OnEditTextCompleteListener {
        fun onProjectTitleTextChange(position: Int, text: String)
        fun onProjectDescriptionChange(position: Int, text: String)

        fun requestNewFocus(editText: EditText)

        fun OnRemoveItem(position: Int)
    }

    fun setOnEditTextCompleteListener(listener: OnEditTextCompleteListener) {
        onEditTextCompleteListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProjectItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
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


    inner class ViewHolder(private val binding: ProjectItemBinding) :
        RecyclerView.ViewHolder(binding.root) {



        fun bindTo(currentItem: Project) {

            // Bind your data to the views using data binding
            binding.projectEdittext.setText(currentItem.title)
            binding.descriptionEditTxt.setText(currentItem.description)
            binding.projectEdittext.error=null

            tryCatch {
                if (currentItem.title.isEmpty()) {
                    onEditTextCompleteListener?.requestNewFocus(binding.projectEdittext)

                }
            }


            ViewUtils.applyCapitalizeFilter(binding.projectEdittext)

            ViewUtils.setupEditTextofAdaptors(
                binding.projectEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                binding.descriptionEditTxt
            )

            ViewUtils.hideKeyboard_c(binding.descriptionEditTxt)








            // Set listeners for EditText changes
            binding.projectEdittext.addTextChangedListener(GenericTextWatcher { newText ->
                if (newText.length <= 30) {
//                    if (ViewUtils.validateInput(newText)) {
//                        binding.projectEdittext.error = null
//                    } else {
//                        binding.projectEdittext.error = "Invalid input"
//                    }
                } else {

                    // Trim the input to 30 characters
                    val trimmedText = newText.substring(0, 30)
                    binding.projectEdittext.setText(trimmedText)
                    // Set the cursor to the end of the trimmed text
                    binding.projectEdittext.setSelection(trimmedText.length)
                    binding.projectEdittext.error = "Character limit exceeded (30 characters max)."
                }

                onEditTextCompleteListener?.onProjectTitleTextChange(adapterPosition, newText)
            })

            // Set listeners for EditText changes
            binding.descriptionEditTxt.addTextChangedListener(GenericTextWatcher { newText ->

                onEditTextCompleteListener?.onProjectDescriptionChange(adapterPosition, newText)
            })




            binding.viewInstitutedetailicn.setOnClickListener {
                toggleVisibility()
            }

            // Set a click listener on the "titledetailicn" icon
            binding.removeItem.setOnClickListener {
                onEditTextCompleteListener?.OnRemoveItem(adapterPosition)
            }



        }

        private fun toggleVisibility() {
            val isDataVisible = binding.dataConstraint.isVisible
            binding.dataConstraint.isVisible = !isDataVisible
            binding.project.text = currentList[position].title.ifEmpty { "Project Title" }
            binding.viewInstitutedetailicn.rotation = if (isDataVisible) 180f else 0f
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldItem: Project, newItem: Project) =
            oldItem.hashCode() == newItem.hashCode()

        override fun areContentsTheSame(oldItem: Project, newItem: Project) =
            oldItem == newItem
    }



}
