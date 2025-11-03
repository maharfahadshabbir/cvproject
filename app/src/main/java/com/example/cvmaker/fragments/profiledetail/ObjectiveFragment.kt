package com.example.cvmaker.fragments.profiledetail

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cvmaker.R
import com.example.cvmaker.databinding.FragmentObjectiveBinding
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.model.workingmodels.ObjectiveModel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.showToastSafe
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import java.util.regex.Pattern

class ObjectiveFragment : Fragment() {

    private lateinit var binding: FragmentObjectiveBinding
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var isBoldClicked = false
    private var isItalicClicked = false
    private var isUnderlineClicked = false
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentObjectiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun backPressed() {
        findNavController().popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        setupUi()
        populateData()
        setupEditorListeners()
        setupClicks()
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
                getViewLifecycleOwnerOrNull()?.let { owner ->
                    activity?.onBackPressedDispatcher?.addCallback(owner, it)
                }
            }
        }
    }

    private fun setupUi() {
        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
        binding.etObjective.setPadding(resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp))
        binding.etObjective.setEditorFontColor(Color.BLACK)
        binding.etObjective.setEditorFontSize(13)
        resetButtonStates()
    }

    private fun populateData() {
        val html = sharedViewModel.cvModelRequestDb.objective?.objective.orEmpty()
        if (html.isNotBlank()) {
            binding.etObjective.html = html
            binding.textviewforhint.isVisible = false
        } else {
            binding.textviewforhint.isVisible = true
        }
    }

    private fun setupEditorListeners() {
        binding.etObjective.setOnTouchListener { _, _ ->
            binding.scrollObjective.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.etObjective.setOnTextChangeListener { html ->
            val text = html ?: ""
            binding.textviewforhint.isVisible = text.isBlank()
            if (sharedViewModel.cvModelRequestDb.objective == null) {
                sharedViewModel.cvModelRequestDb.objective = ObjectiveModel()
            }
            sharedViewModel.cvModelRequestDb.objective?.objective = text
        }
    }

    private fun setupClicks() = with(binding) {
        backButton.setOnClickListener { backPressed() }

        btnBold.setOnClickListener {
            tryCatch {
                etObjective.setBold()
                isBoldClicked = !isBoldClicked
                updateButtonBackground(btnBold, isBoldClicked)
            }
        }

        btnItalic.setOnClickListener {
            tryCatch {
                etObjective.setItalic()
                isItalicClicked = !isItalicClicked
                updateButtonBackground(btnItalic, isItalicClicked)
            }
        }

        btnUnderline.setOnClickListener {
            tryCatch {
                etObjective.setUnderline()
                isUnderlineClicked = !isUnderlineClicked
                updateButtonBackground(btnUnderline, isUnderlineClicked)
            }
        }

        btnUndo.setOnClickListener {
            tryCatch {
                etObjective.undo()
                resetButtonStates()
            }
        }

        btnRedo.setOnClickListener {
            tryCatch {
                etObjective.redo()
            }
        }

        btnBullets.setOnClickListener {
            tryCatch { etObjective.setBullets() }
        }

        btnNumbers.setOnClickListener {
            tryCatch { etObjective.setNumbers() }
        }

        // Save objective locally in cvModelRequestDb
        btnSave.setOnClickListener {
            val html = etObjective.html ?: ""
            if (isObjectiveValid(html)) {
                sharedViewModel.cvModelRequestDb.objective =
                    ObjectiveModel(objective = html.trim())
                showToastSafe("Objective saved successfully!")
                backPressed()
            }
        }

        // Optional preview (kept for reference)
        // previewCv.setOnClickListener { previewCv(sharedViewModel, R.id.fragmentPreviewApi) }
    }

    private fun updateButtonBackground(view: View, selected: Boolean) {
        val color = ContextCompat.getColor(
            requireContext(),
            if (selected) R.color.white else R.color.white
        )
        view.setBackgroundColor(color)
    }

    private fun resetButtonStates() {
        isBoldClicked = false
        isItalicClicked = false
        isUnderlineClicked = false
        updateButtonBackground(binding.btnBold, false)
        updateButtonBackground(binding.btnItalic, false)
        updateButtonBackground(binding.btnUnderline, false)
    }

    private fun isObjectiveValid(html: String): Boolean {
        val plain = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        return if (plain.isEmpty()) {
            showToastSafe("Please enter your objective.")
            false
        } else true
    }
}
