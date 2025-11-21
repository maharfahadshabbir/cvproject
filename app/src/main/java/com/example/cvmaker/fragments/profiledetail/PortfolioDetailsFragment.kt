package com.example.cvmaker.fragments.profiledetail

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cvmaker.databinding.FragmentPortfolioDetailsBinding
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
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

        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
        _binding = null
    }

    // -------------------- Back handling --------------------

    private fun configureBackPress() {
        tryCatch {
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }
            onBackPressedCallback?.let { cb ->
                getViewLifecycleOwnerOrNull()?.let { owner ->
                    activity?.onBackPressedDispatcher?.addCallback(owner, cb)
                }
            }
        }
    }

    private fun backPressed() {
        persistPortfolio(showToast = false)
        findNavController().popBackStack()
    }

    // -------------------- Populate for edit --------------------

    private fun populateData() {
        val list = sharedViewModel.cvModelRequestDb.portfolioList
        val current = list.firstOrNull() ?: PortfolioModel()

        binding.etGithub.setText(current.github.orEmpty())
        binding.etDribbble.setText(current.dribble.orEmpty())
        binding.etBehance.setText(current.behance.orEmpty())
        binding.etWebsiteName.setText(current.websiteName.orEmpty())
        binding.etWebsiteLink.setText(current.websiteLink.orEmpty())
    }

    // -------------------- Live sync while typing --------------------

    private fun setupLiveSync() {
        binding.etGithub.doAfterTextChanged {
            updateLocal { m -> m.github = binding.etGithub.textOrNull() }
        }
        binding.etDribbble.doAfterTextChanged {
            updateLocal { m -> m.dribble = binding.etDribbble.textOrNull() }
        }
        binding.etBehance.doAfterTextChanged {
            updateLocal { m -> m.behance = binding.etBehance.textOrNull() }
        }
        binding.etWebsiteName.doAfterTextChanged {
            updateLocal { m -> m.websiteName = binding.etWebsiteName.textOrNull() }
        }
        binding.etWebsiteLink.doAfterTextChanged {
            updateLocal { m -> m.websiteLink = binding.etWebsiteLink.textOrNull() }
        }
    }

    private fun setupClicks() {
        binding.backButton.setOnClickListener { backPressed() }

        binding.btnSave.setOnClickListener {
            persistPortfolio(showToast = true)
        }
    }

    // -------------------- Persist helpers --------------------

    private fun persistPortfolio(showToast: Boolean) {
        val github = binding.etGithub.text?.toString()?.trim().orEmpty()
        val dribbble = binding.etDribbble.text?.toString()?.trim().orEmpty()
        val behance = binding.etBehance.text?.toString()?.trim().orEmpty()
        val websiteName = binding.etWebsiteName.text?.toString()?.trim().orEmpty()
        val websiteLink = binding.etWebsiteLink.text?.toString()?.trim().orEmpty()

        val allBlank = github.isBlank() &&
                dribbble.isBlank() &&
                behance.isBlank() &&
                websiteName.isBlank() &&
                websiteLink.isBlank()

        val list = sharedViewModel.cvModelRequestDb.portfolioList

        if (allBlank) {
            list.clear()
            if (showToast) {
                Toast.makeText(requireContext(), "No portfolio links added", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val model = PortfolioModel(
            github = github.nullIfBlank(),
            dribble = dribbble.nullIfBlank(),
            behance = behance.nullIfBlank(),
            websiteName = websiteName.nullIfBlank(),
            websiteLink = websiteLink.nullIfBlank()
        )

        if (list.isEmpty()) list.add(model) else list[0] = model

        if (showToast) {
            showToastSafe("Portfolio saved")
            findNavController().popBackStack()
        }
    }

    // -------------------- Keyboard padding (FIXED) --------------------

    private fun handleKeyboard(root: View) {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            // ❗ Use _binding safely, avoid calling `binding` when view is destroyed
            val b = _binding ?: return@addOnGlobalLayoutListener

            val r = Rect()
            root.getWindowVisibleDisplayFrame(r)
            val screenHeight = root.rootView.height
            val keypadHeight = screenHeight - r.bottom

            val bottomPadding =
                if (keypadHeight > screenHeight * 0.15) keypadHeight else 0

            b.scrollPortfolio.setPadding(
                b.scrollPortfolio.paddingLeft,
                b.scrollPortfolio.paddingTop,
                b.scrollPortfolio.paddingRight,
                bottomPadding
            )
        }
    }

    // -------------------- Local update helpers --------------------

    private fun updateLocal(mutate: (PortfolioModel) -> Unit) {
        val list = sharedViewModel.cvModelRequestDb.portfolioList
        if (list.isEmpty()) list.add(PortfolioModel())
        val current = list[0]
        mutate(current)
    }

    private fun android.widget.EditText.textOrNull(): String? {
        val t = text?.toString()?.trim().orEmpty()
        return t.nullIfBlank()
    }

    private fun String.nullIfBlank(): String? = ifBlank { null }
}
