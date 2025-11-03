package com.example.cvmaker.fragments.profiledetail.adapters

import android.annotation.SuppressLint
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.CompoundButtonCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cvmaker.R
import com.example.cvmaker.databinding.ExperienceRowItemBinding
import com.example.cvmaker.fragments.profiledetail.util.DatePickerUtil
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import com.example.cvmaker.model.workingmodels.ExperienceModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExperienceAdapter :
    ListAdapter<ExperienceModel, ExperienceAdapter.ViewHolder>(DiffCallback()) {

    private val datePickerUtil = DatePickerUtil()
    private var onEditTextCompleteListener: OnEditTextCompleteListener? = null
    private var msg = "issue"

    // Only one expanded at a time
    private val expandedPositions = mutableSetOf<Int>()

    interface OnEditTextCompleteListener {
        fun onCompanyNameTextChangeChange(position: Int, text: String)
        fun onDetailTextChange(position: Int, text: String)
        fun onStartDateChange(position: Int, text: String)   // MM/dd/yyyy
        fun onEndDateChange(position: Int, text: String)     // MM/dd/yyyy or "Present"
        fun onJobTextChange(position: Int, text: String)
        fun onCheckBoxStateChanged(position: Int, isChecked: Boolean)
        fun requestFocusForNewItem(editText: EditText)
        fun removeItem(position: Int)
    }

    fun setOnEditTextCompleteListener(listener: OnEditTextCompleteListener) {
        onEditTextCompleteListener = listener
    }

    /** Collapse all and expand only this position */
    fun expandOnly(position: Int) {
        expandedPositions.clear()
        if (position in 0 until itemCount) expandedPositions.add(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ExperienceRowItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position), expandedPositions.contains(position))
    }

    inner class ViewHolder(private val binding: ExperienceRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var companyWatcher: TextWatcher? = null
        private var designationWatcher: TextWatcher? = null
        private var detailWatcher: TextWatcher? = null
        private var startWatcher: TextWatcher? = null
        private var endWatcher: TextWatcher? = null

        private fun detachWatchers() {
            companyWatcher?.let { binding.companyEdittext.removeTextChangedListener(it) }
            designationWatcher?.let { binding.designationEdittext.removeTextChangedListener(it) }
            detailWatcher?.let { binding.detailEditTxt.removeTextChangedListener(it) }
            startWatcher?.let { binding.startDateEdittext.removeTextChangedListener(it) }
            endWatcher?.let { binding.endDateEdittext.removeTextChangedListener(it) }
            companyWatcher = null; designationWatcher = null; detailWatcher = null
            startWatcher = null; endWatcher = null
        }

        @SuppressLint("SetTextI18n")
        fun bindTo(item: ExperienceModel, isExpanded: Boolean) {
            // 1) Reset listeners to avoid duplicates on rebind
            detachWatchers()

            // 2) Prefill edit fields from model
            binding.companyEdittext.setText(item.companyName.orEmpty())
            binding.designationEdittext.setText(item.designation.orEmpty())
            binding.detailEditTxt.setText(item.detail.orEmpty())

            val startUi = item.startDate?.let { isoToUi(it) }
            val endUi = item.endDate?.let { isoToUi(it) }

            if (startUi != null) binding.startDateEdittext.setText(startUi)
            else binding.startDateEdittext.text = null

            if (item.isCurrentWorking) binding.endDateEdittext.setText(R.string.present)
            else if (endUi != null) binding.endDateEdittext.setText(endUi)
            else binding.endDateEdittext.text = null

            // 3) Checkbox / end-date enable
            binding.checkboxfordate.setOnCheckedChangeListener(null)
            binding.checkboxfordate.isChecked = item.isCurrentWorking
            CompoundButtonCompat.setButtonTintList(
                binding.checkboxfordate,
                ContextCompat.getColorStateList(
                    itemView.context,
                    if (item.isCurrentWorking) R.color.blue else R.color.gray_tx_color
                )
            )
            binding.endDateEdittext.isEnabled = !item.isCurrentWorking

            // 4) Header / accordion visibility (NO overlay):
            // We use ONLY the `company` TextView as the header.
            val header = item.companyName?.takeIf { it.isNotBlank() }
                ?: itemView.context.getString(R.string.institute)
            binding.company.text = header

            // Title row must be hidden entirely to avoid overlap
            binding.title.isVisible = false
            binding.titledetailicn.isVisible = false

            // Show header row only when collapsed; show editor only when expanded
            binding.company.isVisible = !isExpanded
            binding.viewInstitutedetailicn.isVisible = !isExpanded
            binding.dataConstraint.isVisible = isExpanded
            binding.viewInstitutedetailicn.rotation = if (isExpanded) 0f else 180f

            // Clear errors
            binding.companyEdittext.error = null
            binding.designationEdittext.error = null
            binding.detailEditTxt.error = null
            binding.startDateEdittext.error = null
            binding.endDateEdittext.error = null

            // 5) Inputs
            ViewUtils.setupEditTextofAdaptors(
                binding.companyEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                binding.designationEdittext
            )
            ViewUtils.setupEditTextofAdaptors(
                binding.designationEdittext,
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_NEXT,
                binding.startDateEdittext
            )
            ViewUtils.applyCapitalizeFilter(binding.companyEdittext)
            ViewUtils.applyCapitalizeFilter(binding.designationEdittext)

            // 6) Date pickers
            binding.startDateEdittext.inputType = InputType.TYPE_NULL
            binding.endDateEdittext.inputType = InputType.TYPE_NULL

            val showDatePicker: (EditText, (String) -> Unit) -> Unit = { editText, onDateSelected ->
                itemView.context?.let { context ->
                    val cal = Calendar.getInstance()
                    val fmt = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    val prev = editText.text?.toString().orEmpty()
                    if (prev.isNotEmpty() && prev != context.getString(R.string.present)) {
                        try { fmt.parse(prev)?.let { cal.time = it } } catch (_: ParseException) {}
                    }
                    datePickerUtil.showDatePickerDialog(
                        cal, 0, context,
                        object : DatePickerUtil.DateSelectedListener {
                            override fun onDateSelected(formattedDate: String) {
                                onDateSelected(formattedDate)
                            }
                        }
                    )
                }
            }

            binding.startDateEdittext.setOnClickListener {
                showDatePicker(binding.startDateEdittext) { d -> binding.startDateEdittext.setText(d) }
            }
            binding.startDateEdittext.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) showDatePicker(binding.startDateEdittext) { d ->
                    binding.startDateEdittext.setText(d)
                }
            }

            binding.endDateEdittext.setOnClickListener {
                if (!binding.checkboxfordate.isChecked) {
                    showDatePicker(binding.endDateEdittext) { d -> binding.endDateEdittext.setText(d) }
                }
            }
            binding.endDateEdittext.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && !binding.checkboxfordate.isChecked) {
                    showDatePicker(binding.endDateEdittext) { d -> binding.endDateEdittext.setText(d) }
                }
            }

            // 7) Text watchers (attach AFTER prefill)
            companyWatcher = watcher { txt ->
                val t = if (txt.length > 50) txt.take(50) else txt
                if (t.length != txt.length) {
                    binding.companyEdittext.setText(t)
                    binding.companyEdittext.setSelection(t.length)
                    binding.companyEdittext.error = "Character limit exceeded (50 max)."
                }
                onEditTextCompleteListener?.onCompanyNameTextChangeChange(absoluteAdapterPosition, t)
                // Update header live if collapsed
                if (!binding.dataConstraint.isVisible) binding.company.text =
                    t.ifBlank { itemView.context.getString(R.string.institute) }
            }
            binding.companyEdittext.addTextChangedListener(companyWatcher)

            designationWatcher = watcher { txt ->
                val t = if (txt.length > 50) txt.take(50) else txt
                if (t.length != txt.length) {
                    binding.designationEdittext.setText(t)
                    binding.designationEdittext.setSelection(t.length)
                    binding.designationEdittext.error = "Character limit exceeded (50 max)."
                }
                onEditTextCompleteListener?.onJobTextChange(absoluteAdapterPosition, t)
            }
            binding.designationEdittext.addTextChangedListener(designationWatcher)

            detailWatcher = watcher { txt ->
                onEditTextCompleteListener?.onDetailTextChange(absoluteAdapterPosition, txt)
            }
            binding.detailEditTxt.addTextChangedListener(detailWatcher)

            startWatcher = watcher {
                val ok = validateStartandEnddate(
                    binding.startDateEdittext.text?.toString().orEmpty(),
                    binding.endDateEdittext.text?.toString().orEmpty()
                )
                binding.startDateEdittext.error = if (ok || msg.isEmpty()) null else msg
                onEditTextCompleteListener?.onStartDateChange(
                    absoluteAdapterPosition,
                    binding.startDateEdittext.text?.toString().orEmpty()
                )
            }
            binding.startDateEdittext.addTextChangedListener(startWatcher)

            endWatcher = watcher {
                onEditTextCompleteListener?.onEndDateChange(
                    absoluteAdapterPosition,
                    binding.endDateEdittext.text?.toString().orEmpty()
                )
            }
            binding.endDateEdittext.addTextChangedListener(endWatcher)

            // 8) Toggle collapse/expand via header row
            binding.viewInstitutedetailicn.setOnClickListener {
                expandOnly(absoluteAdapterPosition)
            }
            binding.company.setOnClickListener {
                expandOnly(absoluteAdapterPosition)
            }

            // 9) Remove
            binding.removeItem.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    onEditTextCompleteListener?.removeItem(absoluteAdapterPosition)
                }
            }

            // 10) Checkbox logic (don’t blank dates on collapse)
            binding.checkboxfordate.setOnCheckedChangeListener { _, checked ->
                val tint = ContextCompat.getColorStateList(
                    itemView.context,
                    if (checked) R.color.blue else R.color.gray_tx_color
                )
                CompoundButtonCompat.setButtonTintList(binding.checkboxfordate, tint)
                if (checked) {
                    binding.endDateEdittext.setText(R.string.present)
                    binding.endDateEdittext.isEnabled = false
                } else {
                    if (binding.endDateEdittext.text?.toString() ==
                        itemView.context.getString(R.string.present)
                    ) {
                        binding.endDateEdittext.text = null // keep previous date if user set later
                    }
                    binding.endDateEdittext.hint = "MM/DD/YYYY"
                    binding.endDateEdittext.isEnabled = true
                }
                onEditTextCompleteListener?.onCheckBoxStateChanged(absoluteAdapterPosition, checked)
            }

            // Focus company on brand-new row
            if ((item.companyName ?: "").isBlank()) {
                onEditTextCompleteListener?.requestFocusForNewItem(binding.companyEdittext)
            }
        }

        private fun watcher(after: (String) -> Unit) = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { after(s?.toString().orEmpty()) }
        }
    }

    // MM/dd/yyyy validation; allows "Present"
    private fun validateStartandEnddate(startDateText: String, endDateText: String): Boolean {
        var ok = false
        val cal = Calendar.getInstance()
        val cy = cal.get(Calendar.YEAR)
        val cm = cal.get(Calendar.MONTH) + 1
        val cd = cal.get(Calendar.DAY_OF_MONTH)

        if (startDateText.isEmpty()) return false
        if (endDateText == "Present") {
            val p = startDateText.split("/")
            val sm = p.getOrNull(0)?.toIntOrNull()
            val sd = p.getOrNull(1)?.toIntOrNull()
            val sy = p.getOrNull(2)?.toIntOrNull()
            ok = sm != null && sd != null && sy != null &&
                    (sy < cy || sy == cy && (sm < cm || sm == cm && sd <= cd))
            msg = if (ok) "" else "Start date cannot be in the future"
            return ok
        }

        if (endDateText.isEmpty()) return false
        val sp = startDateText.split("/")
        val ep = endDateText.split("/")
        val sm = sp.getOrNull(0)?.toIntOrNull()
        val sd = sp.getOrNull(1)?.toIntOrNull()
        val sy = sp.getOrNull(2)?.toIntOrNull()
        val em = ep.getOrNull(0)?.toIntOrNull()
        val ed = ep.getOrNull(1)?.toIntOrNull()
        val ey = ep.getOrNull(2)?.toIntOrNull()
        if (sm == null || sd == null || sy == null || em == null || ed == null || ey == null) {
            msg = "Invalid date parameters"; return false
        }

        val startNotFuture = sy < cy || sy == cy && (sm < cm || sm == cm && sd <= cd)
        val endNotFuture = ey < cy || ey == cy && (em < cm || em == cm && ed <= cd)
        if (!startNotFuture || !endNotFuture) { msg = "Dates must not be in the future"; return false }

        ok = sy < ey || sy == ey && (sm < em || sm == em && sd <= ed)
        msg = if (ok) "" else "End Date should be greater than Start Date"
        return ok
    }

    private fun isoToUi(iso: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val output = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            input.parse(iso)?.let { output.format(it) } ?: ""
        } catch (_: Exception) { "" }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExperienceModel>() {
        override fun areItemsTheSame(oldItem: ExperienceModel, newItem: ExperienceModel) =
            oldItem === newItem
        override fun areContentsTheSame(oldItem: ExperienceModel, newItem: ExperienceModel) =
            oldItem == newItem
    }
}
