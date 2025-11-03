package com.example.cvmaker.fragments.profiledetail

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cvmaker.databinding.FragmentPortfolioDetailsBinding
import com.example.cvmaker.model.workingmodels.PortfolioModel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.showToastSafe
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel

class PortfolioDetailsFragment : Fragment() {

    private var _binding: FragmentPortfolioDetailsBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPortfolioDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        populateData()
        setupLiveSync()
        setupClicks()
        handleKeyboard(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
        _binding = null
    }

    private fun configureBackPress() {
        tryCatch {
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
            onBackPressedCallback?.let { cb ->
                getViewLifecycleOwnerOrNull()?.let { owner ->
                    activity?.onBackPressedDispatcher?.addCallback(owner, cb)
                }
            }
        }
    }

    /** Read existing portfolio (index 0 of portfolioList) and fill inputs */
    private fun populateData() {
        val list = sharedViewModel.cvModelRequestDb.portfolioList
        val current = list.firstOrNull() ?: PortfolioModel()

        binding.etGithub.setText(current.github.orEmpty())
        binding.etDribbble.setText(current.dribble.orEmpty())
        binding.etBehance.setText(current.behance.orEmpty())
        binding.etWebsiteName.setText(current.websiteName.orEmpty())
        binding.etWebsiteLink.setText(current.websiteLink.orEmpty())
    }

    /** Two-way live sync: update view-model as user types (still save on button too) */
    private fun setupLiveSync() {
        binding.etGithub.doAfterTextChanged { updateLocal { it.github = it.textOrNull(binding.etGithub) } }
        binding.etDribbble.doAfterTextChanged { updateLocal { it.dribble = it.textOrNull(binding.etDribbble) } }
        binding.etBehance.doAfterTextChanged { updateLocal { it.behance = it.textOrNull(binding.etBehance) } }
        binding.etWebsiteName.doAfterTextChanged { updateLocal { it.websiteName = it.textOrNull(binding.etWebsiteName) } }
        binding.etWebsiteLink.doAfterTextChanged { updateLocal { it.websiteLink = it.textOrNull(binding.etWebsiteLink) } }
    }

    private fun setupClicks() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }

        binding.btnSave.setOnClickListener {
            saveToDb()
        }
    }

    private fun saveToDb() {
        val github = binding.etGithub.text?.toString()?.trim().orEmpty()
        val dribbble = binding.etDribbble.text?.toString()?.trim().orEmpty()
        val behance = binding.etBehance.text?.toString()?.trim().orEmpty()
        val websiteName = binding.etWebsiteName.text?.toString()?.trim().orEmpty()
        val websiteLink = binding.etWebsiteLink.text?.toString()?.trim().orEmpty()

        // If absolutely everything is blank, allow but warn user
        val allBlank = github.isBlank() && dribbble.isBlank() && behance.isBlank() &&
                websiteName.isBlank() && websiteLink.isBlank()
        if (allBlank) {
            Toast.makeText(requireContext(), "No portfolio links added", Toast.LENGTH_SHORT).show()
        }

        val model = PortfolioModel(
            github = github.nullIfBlank(),
            dribble = dribbble.nullIfBlank(),
            behance = behance.nullIfBlank(),
            websiteName = websiteName.nullIfBlank(),
            websiteLink = websiteLink.nullIfBlank()
        )

        // Keep only one record at index 0
        val list = sharedViewModel.cvModelRequestDb.portfolioList
        if (list.isEmpty()) list.add(model) else list[0] = model

        showToastSafe("Portfolio saved")
        findNavController().popBackStack()
    }

    private fun handleKeyboard(root: View) {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            root.getWindowVisibleDisplayFrame(r)
            val screenHeight = root.rootView.height
            val keypadHeight = screenHeight - r.bottom

            binding.scrollPortfolio.setPadding(
                binding.scrollPortfolio.paddingLeft,
                binding.scrollPortfolio.paddingTop,
                binding.scrollPortfolio.paddingRight,
                if (keypadHeight > screenHeight * 0.15) keypadHeight else 0
            )
        }
    }

    /** Helper: mutate the single PortfolioModel at index 0 as user types */
    private fun updateLocal(mutate: (PortfolioModel) -> Unit) {
        val list = sharedViewModel.cvModelRequestDb.portfolioList
        if (list.isEmpty()) list.add(PortfolioModel())
        val current = list[0]
        mutate(current)
    }

    private fun PortfolioModel.textOrNull(input: android.widget.EditText): String? {
        val t = input.text?.toString()?.trim().orEmpty()
        return t.nullIfBlank()
    }

    private fun String.nullIfBlank(): String? = if (isBlank()) null else this
}
