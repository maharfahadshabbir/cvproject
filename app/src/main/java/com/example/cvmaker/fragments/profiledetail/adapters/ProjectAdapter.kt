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
import com.example.cvmaker.model.workingmodels.ProjectModel
import com.example.cvmaker.utils.tryCatch

class ProjectAdapter :
    ListAdapter<ProjectModel, ProjectAdapter.ViewHolder>(DiffCallback()) {

    interface OnEditTextCompleteListener {
        fun onProjectTitleTextChange(position: Int, text: String)
        fun onProjectDescriptionChange(position: Int, text: String)
        fun onProjectLinkChange(position: Int, text: String)
        fun requestNewFocus(editText: EditText)
        fun onRemoveItem(position: Int)
    }

    private var listener: OnEditTextCompleteListener? = null

    // one-open accordion behavior
    private val expandedPositions = mutableSetOf<Int>()
    fun expandOnly(position: Int) {
        expandedPositions.clear()
        if (position in 0 until itemCount) expandedPositions.add(position)
        notifyDataSetChanged()
    }

    fun setOnEditTextCompleteListener(l: OnEditTextCompleteListener) {
        listener = l
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProjectItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position), expandedPositions.contains(position))
    }

    private fun tw(after: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) { after(s?.toString().orEmpty()) }
    }

    inner class ViewHolder(private val binding: ProjectItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(item: ProjectModel, isExpanded: Boolean) = with(binding) {
            // header
            project.text = (item.projectTitle ?: "").ifEmpty { "Project Title" }
            viewInstitutedetailicn.rotation = if (isExpanded) 0f else 180f

            // details
            projectEdittext.setText(item.projectTitle.orEmpty())
            descriptionEditTxt.setText(item.description.orEmpty())
            etLink.setText(item.link.orEmpty())

            // accordion
            dataConstraint.isVisible = isExpanded

            // focus if new
            tryCatch {
                if (item.projectTitle.isNullOrEmpty()) {
                    listener?.requestNewFocus(projectEdittext)
                }
            }

            // inputs & IME
            ViewUtils.applyCapitalizeFilter(projectEdittext)
            ViewUtils.setupEditTextofAdaptors(
                projectEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                descriptionEditTxt
            )
            // description multi-line
            descriptionEditTxt.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
            descriptionEditTxt.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            // link as URI
            etLink.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI

            // watchers
            projectEdittext.addTextChangedListener(tw { text ->
                val trimmed = if (text.length > 30) {
                    val t = text.substring(0, 30)
                    projectEdittext.setText(t)
                    projectEdittext.setSelection(t.length)
                    projectEdittext.error = "Character limit exceeded (30 max)."
                    t
                } else {
                    projectEdittext.error = null
                    text
                }
                listener?.onProjectTitleTextChange(absoluteAdapterPosition, trimmed)
                if (!dataConstraint.isVisible) project.text =
                    trimmed.ifEmpty { "Project Title" }
            })

            descriptionEditTxt.addTextChangedListener(tw { text ->
                listener?.onProjectDescriptionChange(absoluteAdapterPosition, text)
            })

            etLink.addTextChangedListener(tw { text ->
                listener?.onProjectLinkChange(absoluteAdapterPosition, text)
            })

            // expand/collapse from chevron or header
            viewInstitutedetailicn.setOnClickListener { expandOnly(absoluteAdapterPosition) }
            project.setOnClickListener { expandOnly(absoluteAdapterPosition) }

            // remove
            removeItem.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    listener?.onRemoveItem(absoluteAdapterPosition)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ProjectModel>() {
        override fun areItemsTheSame(old: ProjectModel, new: ProjectModel) = old === new
        override fun areContentsTheSame(old: ProjectModel, new: ProjectModel) = old == new
    }
}
