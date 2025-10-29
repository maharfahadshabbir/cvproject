package com.example.cvmaker.fragments.profiledetail

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cvmaker.R
import com.example.cvmaker.activities.MainActivity
import com.example.cvmaker.databinding.FragmentObjectiveBinding
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.previewCv
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.showToastSafe
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import java.util.regex.Pattern
import kotlin.getValue


class ObjectiveFragment : Fragment() {


    private lateinit var binding: FragmentObjectiveBinding
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var isBoldClicked = false
    private var isItalicClicked = false
    private var isUnderlineClicked = false


    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var coroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentObjectiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun backPressed() {
      findNavController().popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        initListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    private fun configureBackPress() {
        tryCatch {
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }

            onBackPressedCallback?.let {
                getViewLifecycleOwnerOrNull()?.let { it1 ->
                    activity?.onBackPressedDispatcher?.addCallback(
                        it1,
                        it
                    )
                }
            }
        }
    }

    private fun initListener() {
        populateData()
        clickListner()
        changeListener()
        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    private fun populateData() {
        if (sharedViewModel.cvModel.objective.isNotEmpty()) {
//            binding.editText.setBold()
//            binding.editText.setItalic()
//            binding.editText.setUnderline()
            // reset state

            Log.i("richedites", "populateData:${sharedViewModel.cvModel.objective} ")
            binding.etObjective.html = sharedViewModel.cvModel.objective +"<br>"
            binding.etObjective.loadUrl("javascript:document.execCommand('removeFormat', false, null);")
            binding.textviewforhint.isVisible = false
        } else {
            binding.textviewforhint.isVisible = true
            sharedViewModel.cvModel.objective = ""
        }

        // Ensure formatting states are reset when populating data
        context?.let { currentContext ->
            resetButtonStates(currentContext)
        }
    }

    private fun changeListener() {
        binding.backButton.setOnClickListener {
            backPressed()
        }

        binding.etObjective.setOnTouchListener { _, event ->
            binding.scrollObjective.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.etObjective.setOnTextChangeListener {
            activity?.let {
                if (it is MainActivity) {
                }
            }

            tryCatch {
                binding.textviewforhint.isVisible = it.isNullOrEmpty()

                sharedViewModel.cvModel.apply {
                    this?.objective = it ?: ""
                }
            }

            tryCatch {
                val editTextHtml = binding.etObjective.html

                if (editTextHtml != null) {
                    val formatting = checkTextFormatting(editTextHtml)

                    when (formatting) {
                        "Bold" -> {
                            showToastSafe("Text is Bold")
                        }

                        "Italic" -> {
                            showToastSafe("Text is Italic")
                        }

                        "Underline" -> {
                            showToastSafe("Text is Underlined")
                        }

                        "List" -> {
                            showToastSafe("Text is in a List")
                        }

                        "BoldItalic" -> {
                            showToastSafe("Text is Bold and Italic")
                        }

                        "BoldUnderline" -> {
                            showToastSafe("Text is Bold and Underlined")
                        }

                        "ItalicUnderline" -> {
                            showToastSafe("Text is Italic and Underlined")
                        }

                        "UnderlineNumberedList" -> {
                            showToastSafe("Text is Underlined and in a Numbered List")
                        }

                        "UnderlineBulletedList" -> {
                            showToastSafe("Text is Underlined and in a Bulleted List")
                        }

                        "BoldNumberedList" -> {
                            showToastSafe("Text is Bold and in a Numbered List")
                        }

                        "ItalicNumberedList" -> {
                            showToastSafe("Text is Italic and in a Numbered List")
                        }

                        "BoldBulletedList" -> {
                            showToastSafe("Text is Bold and in a Bulleted List")
                        }

                        "ItalicBulletedList" -> {
                            showToastSafe("Text is Italic and in a Bulleted List")
                        }

                        "BoldItalicUnderline" -> {
                            showToastSafe("Text is Bold, Italic, and Underlined")
                        }

                        "Normal" -> {
                            showToastSafe("Text is Normal")
                        }

                        else -> {
                        }
                    }
                } else {
                    // Handle the case when editText.html is null
                    showToastSafe("Text is null")
                }
            }
        }
    }

    private fun clickListner() {
        context?.let { currentContext ->
            tryCatch {
                binding.etObjective.setPadding(resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp))
                binding.etObjective.setEditorFontColor(Color.BLACK)
                binding.etObjective.setEditorFontSize(13)

                // Initialize button states
                initializeButtonStates(currentContext)

                // Bold button
                binding.btnBold.setOnClickListener {
                    tryCatch {
                        binding.etObjective.setBold()

                        // Toggle the state AFTER applying formatting
                        isBoldClicked = !isBoldClicked

                        // Update background color based on new state
                        updateButtonBackground(binding.btnBold, isBoldClicked, currentContext)
                    }
                }

                // Italic button
                binding.btnItalic.setOnClickListener {
                    tryCatch {
                        binding.etObjective.setItalic()

                        // Toggle the state AFTER applying formatting
                        isItalicClicked = !isItalicClicked

                        // Update background color based on new state
                        updateButtonBackground(binding.btnItalic, isItalicClicked, currentContext)
                    }
                }

                // Underline button
                binding.btnUnderline.setOnClickListener {
                    tryCatch {
                        binding.etObjective.setUnderline()

                        // Toggle the state AFTER applying formatting
                        isUnderlineClicked = !isUnderlineClicked

                        // Update background color based on new state
                        updateButtonBackground(binding.btnUnderline, isUnderlineClicked, currentContext)
                    }
                }

                // Undo button
                binding.btnUndo.setOnClickListener {
                    tryCatch {
                        binding.etObjective.undo()
                        // Reset button states after undo
                        resetButtonStates(currentContext)
                    }
                }

                // Redo button
                binding.btnRedo.setOnClickListener {
                    tryCatch {
                        binding.etObjective.redo()
                        // You might want to sync button states after redo as well
                    }
                }

                // Bullet list button
                binding.btnBullets.setOnClickListener {
                    tryCatch {
                        binding.etObjective.setBullets()
                    }
                }

                // Numbered list button
                binding.btnNumbers.setOnClickListener {
                    tryCatch {
                        binding.etObjective.setNumbers()
                    }
                }

                // Preview CV button
                binding.previewCv.setOnClickListener {
//                    previewCv( sharedViewModel, R.id.fragmentPreviewApi)
                }

            }
        }
    }


    private fun initializeButtonStates(context: android.content.Context) {
        tryCatch {
            // Set initial button backgrounds to unselected state
            updateButtonBackground(binding.btnBold, isBoldClicked, context)
            updateButtonBackground(binding.btnItalic, isItalicClicked, context)
            updateButtonBackground(binding.btnUnderline, isUnderlineClicked, context)
        }
    }

    /**
     * Update button background based on its state
     */
    private fun updateButtonBackground(button: View, isSelected: Boolean, context: android.content.Context) {
        val backgroundColor = if (isSelected) {
            ContextCompat.getColor(context, R.color.white)
        } else {
            ContextCompat.getColor(context, R.color.white)
        }
        button.setBackgroundColor(backgroundColor)
    }

    /**
     * Reset all button states (useful after undo operations)
     */
    private fun resetButtonStates(context: android.content.Context) {
        tryCatch {
            isBoldClicked = false
            isItalicClicked = false
            isUnderlineClicked = false

            updateButtonBackground(binding.btnBold, isBoldClicked, context)
            updateButtonBackground(binding.btnItalic, isItalicClicked, context)
            updateButtonBackground(binding.btnUnderline, isUnderlineClicked, context)
        }
    }

    private fun checkTextFormatting(htmlContent: String?): String {
        if (htmlContent == null) {
            return "Null"
        }

        var result = ""

        tryCatch {
            val boldPattern = Pattern.compile("<b>|<strong>")
            val italicPattern = Pattern.compile("<i>|<em>")
            val underlinePattern = Pattern.compile("<u>")
            val listPattern = Pattern.compile("<ol>|<ul>")

            val formattedText = StringBuilder()

            var isInBold = false
            var isInItalic = false
            var isUnderlined = false
            var isInList = false

            for (char in htmlContent) {
                when {
                    boldPattern.matcher(char.toString()).find() -> isInBold = !isInBold
                    italicPattern.matcher(char.toString()).find() -> isInItalic = !isInItalic
                    underlinePattern.matcher(char.toString()).find() -> isUnderlined = !isUnderlined
                    listPattern.matcher(char.toString()).find() -> isInList = true
                    char == '<' || char == '>' -> { /* Ignore HTML tags */
                    }

                    else -> {
                        val formatting = buildString {
                            if (isInBold) append("B")
                            if (isInItalic) append("I")
                            if (isUnderlined) append("U")
                            if (isInList) append("L")
                        }

                        formattedText.append("$formatting$char")

                        // Reset list state after processing a character
                        isInList = false
                    }
                }
            }

            result = formattedText.toString()
            println("Formatted Text: $result") // Debug log
        }

        return result
    }


}