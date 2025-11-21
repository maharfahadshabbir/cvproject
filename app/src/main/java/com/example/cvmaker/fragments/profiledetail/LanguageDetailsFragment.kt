package com.example.cvmaker.fragments.profiledetail

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cvmaker.databinding.FragmentLanguageDetailsBinding
import com.example.cvmaker.fragments.profiledetail.adapters.LanguageAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.workingmodels.LanguageModel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.showToastSafe
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel

class LanguageDetailsFragment : Fragment() {

    // nullable binding pattern
    private var _binding: FragmentLanguageDetailsBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var adapter: LanguageAdapter? = null
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        setupRecycler()
        populateData()
        setupClicks()
        handleKeyboard(view)

        binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
        adapter = null
        _binding = null
    }

    /* -------------------- Back handling -------------------- */

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
        cleanAndPersistLanguages()
        findNavController().popBackStack()
    }

    /* -------------------- Recycler / Adapter -------------------- */

    private fun setupRecycler() {
        adapter = LanguageAdapter()
        binding.languageRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.languageRecyclerView.adapter = adapter

        adapter?.setListener(object : LanguageAdapter.Listener {
            override fun onLanguageTextChange(position: Int, text: String) {
                val list = sharedViewModel.cvModelRequestDb.languageList
                if (position in list.indices) {
                    list[position].languageName = text.ifBlank { null }
                }
            }

            override fun onLevelChanged(position: Int, level: String) {
                val list = sharedViewModel.cvModelRequestDb.languageList
                if (position in list.indices) {
                    list[position].level = level
                }
            }

            override fun onRemove(position: Int) {
                showRemoveItemBottomSheet(position)
            }

            override fun requestFocusForNewItem(editText: EditText) {
                editText.requestFocus()
            }

            override fun collapseAllExcept(position: Int) {
                if (position == -1) {
                    // collapse all
                    adapter?.expandOnly(-1)
                    return
                }
                adapter?.expandOnly(position)
            }
        })
    }

    /**
     * Populate from SharedViewModel:
     * - Edit existing profile: use existing languageList.
     * - New profile: if empty, add a single blank row with default level.
     */
    private fun populateData() {
        val list = sharedViewModel.cvModelRequestDb.languageList
        if (list.isEmpty()) {
            list.add(LanguageModel(languageName = "", level = "Novice"))
        }
        adapter?.submitList(list.toList())
        adapter?.expandOnly(list.lastIndex)
    }

    /* -------------------- Clicks -------------------- */

    private fun setupClicks() {
        binding.backButton.setOnClickListener { backPressed() }

        binding.addMoreLanguage.setOnClickListener {
            val list = sharedViewModel.cvModelRequestDb.languageList

            // Allow add only if all existing rows have a name + level
            val allFilled = list.all {
                !it.languageName.isNullOrBlank() && !it.level.isNullOrBlank()
            }
            if (!allFilled) {
                Toast.makeText(
                    requireContext(),
                    "Please fill the current language first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Add new + accordion behavior: collapse previous, open new
            list.add(LanguageModel(languageName = "", level = "Novice"))
            adapter?.submitList(list.toList())
            val newIndex = list.lastIndex
            binding.languageRecyclerView.scrollToPosition(newIndex)
            adapter?.expandOnly(newIndex)
        }

        binding.btnSave.setOnClickListener {
            val list = sharedViewModel.cvModelRequestDb.languageList

            val cleaned = list
                .filter { !it.languageName.isNullOrBlank() }
                .map {
                    it.copy(
                        languageName = it.languageName!!.trim(),
                        level = (it.level ?: "Novice")
                    )
                }
                .toMutableList()

            if (cleaned.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please add at least one language",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Persist to in-memory DB
            sharedViewModel.cvModelRequestDb.languageList = cleaned
            showToastSafe("Languages saved successfully!")
            findNavController().popBackStack()
        }
    }

    /* -------------------- Remove item -------------------- */

    private fun showRemoveItemBottomSheet(position: Int) {
        val remove = RemoveItemBottomSheet {
            removeItem(position)
        }
        remove.show(parentFragmentManager, "RemoveLanguageBottomSheet")
    }

    private fun removeItem(position: Int) {
        try {
            val list = sharedViewModel.cvModelRequestDb.languageList
            if (position in list.indices) {
                val updated = list.toMutableList()
                updated.removeAt(position)

                if (updated.isEmpty()) {
                    // If user removed the last one, keep a blank row to edit
                    updated.add(LanguageModel(languageName = "", level = "Novice"))
                }

                sharedViewModel.cvModelRequestDb.languageList = updated
                adapter?.submitList(updated.toList())
                adapter?.expandOnly(updated.lastIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /* -------------------- Cleanup on back -------------------- */

    private fun cleanAndPersistLanguages() {
        tryCatch {
            val list = sharedViewModel.cvModelRequestDb.languageList

            val cleaned = list
                .map {
                    it.copy(
                        languageName = it.languageName?.trim(),
                        level = it.level ?: "Novice"
                    )
                }
                .filter { !it.languageName.isNullOrBlank() }
                .toMutableList()

            // If everything got removed, keep one empty row for when user returns
            if (cleaned.isEmpty()) {
                cleaned.add(LanguageModel(languageName = "", level = "Novice"))
            }

            sharedViewModel.cvModelRequestDb.languageList = cleaned
            adapter?.submitList(cleaned.toList())
            adapter?.expandOnly(cleaned.lastIndex)
        }
    }

    /* -------------------- Keyboard padding -------------------- */

    private fun handleKeyboard(root: View) {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            root.getWindowVisibleDisplayFrame(r)
            val screenHeight = root.rootView.height
            val keypadHeight = screenHeight - r.bottom

            binding.scrollLanguages.setPadding(
                binding.scrollLanguages.paddingLeft,
                binding.scrollLanguages.paddingTop,
                binding.scrollLanguages.paddingRight,
                if (keypadHeight > screenHeight * 0.15) keypadHeight else 0
            )
        }
    }
}
