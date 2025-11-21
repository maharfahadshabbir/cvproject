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
import com.example.cvmaker.databinding.FragmentAchievementsDetailsBinding
import com.example.cvmaker.fragments.profiledetail.adapters.AchievementsAdapter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.bottomsheets.RemoveItemBottomSheet
import com.example.cvmaker.model.workingmodels.AchievementModel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.showToastSafe
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel

class AchievementsDetailsFragment : Fragment() {

    private var _binding: FragmentAchievementsDetailsBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var adapter: AchievementsAdapter? = null
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        setupRecycler()
        populateData()          // 🔥 this will now show existing achievements for edit
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

    // -------------------------------------------------------------------------
    // 🔙 BACK PRESS
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // ♻ RECYCLER + ADAPTER
    // -------------------------------------------------------------------------
    private fun setupRecycler() {
        adapter = AchievementsAdapter()
        binding.achievementRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.achievementRecyclerView.adapter = adapter

        adapter?.setListener(object : AchievementsAdapter.Listener {
            override fun onTitleChanged(position: Int, value: String) {
                val list = sharedViewModel.cvModelRequestDb.achievementList
                if (position in list.indices) list[position].title = value
            }

            override fun onOrganizationChanged(position: Int, value: String) {
                val list = sharedViewModel.cvModelRequestDb.achievementList
                if (position in list.indices) list[position].organization = value
            }

            override fun onYearChanged(position: Int, value: String) {
                val list = sharedViewModel.cvModelRequestDb.achievementList
                if (position in list.indices) list[position].year = value
            }

            override fun onDescriptionChanged(position: Int, value: String) {
                val list = sharedViewModel.cvModelRequestDb.achievementList
                if (position in list.indices) list[position].description = value
            }

            override fun onRemove(position: Int) {
                showRemoveItemBottomSheet(position)
            }

            override fun requestFocusForNewItem(firstField: EditText) {
                firstField.requestFocus()
            }

            override fun collapseAllExcept(position: Int) {
                adapter?.expandOnly(position)
            }
        })
    }


    private fun populateData() {
        val list = sharedViewModel.cvModelRequestDb.achievementList

        if (list.isEmpty()) {
            // New profile or no achievements saved yet → add one empty row
            list.add(
                AchievementModel(
                    title = "",
                    organization = "",
                    year = "",
                    description = ""
                )
            )
        }
        // Make an immutable snapshot for the adapter
        adapter?.submitList(list.toList())
        adapter?.expandOnly(list.lastIndex)
    }

    // -------------------------------------------------------------------------
    // 🖱 CLICKS
    // -------------------------------------------------------------------------
    private fun setupClicks() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }

        binding.addMoreAchievement.setOnClickListener {
            val list = sharedViewModel.cvModelRequestDb.achievementList

            // Require current row minimally filled before adding a new one
            val allOk = list.all { !(it.title.isNullOrBlank()) }
            if (!allOk) {
                Toast.makeText(
                    requireContext(),
                    "Please fill the current achievement first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            list.add(
                AchievementModel(
                    title = "",
                    organization = "",
                    year = "",
                    description = ""
                )
            )
            adapter?.submitList(list.toList())
            val newIndex = list.lastIndex
            binding.achievementRecyclerView.scrollToPosition(newIndex)
            adapter?.expandOnly(newIndex)
        }

        binding.btnSave.setOnClickListener {
            saveAndExit()
        }
    }

    // -------------------------------------------------------------------------
    // 💾 SAVE + EXIT
    // -------------------------------------------------------------------------
    private fun saveAndExit() {
        val list = sharedViewModel.cvModelRequestDb.achievementList

        // Trim each row and drop fully blank ones
        val cleaned = list
            .map {
                it.copy(
                    title = it.title?.trim(),
                    organization = it.organization?.trim(),
                    year = it.year?.trim(),
                    description = it.description?.trim()
                )
            }
            .filter { !(it.title.isNullOrBlank()
                    && it.organization.isNullOrBlank()
                    && it.year.isNullOrBlank()
                    && it.description.isNullOrBlank()) }
            .toMutableList()

        if (cleaned.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please add at least one achievement",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 🔥 Save back into cvModelRequestDb so the full profile (including edit) persists
        sharedViewModel.cvModelRequestDb.achievementList = cleaned

        showToastSafe("Achievements saved successfully!")
        findNavController().popBackStack()
    }

    // -------------------------------------------------------------------------
    // ❌ REMOVE ITEM
    // -------------------------------------------------------------------------
    private fun showRemoveItemBottomSheet(position: Int) {
        val bottomSheet = RemoveItemBottomSheet {
            removeItem(position)
        }
        bottomSheet.show(parentFragmentManager, "RemoveAchievementBottomSheet")
    }

    private fun removeItem(position: Int) {
        try {
            val list = sharedViewModel.cvModelRequestDb.achievementList
            if (position in list.indices) {
                val updated = list.toMutableList()
                updated.removeAt(position)

                if (updated.isEmpty()) {
                    // Keep one empty row so user has something to edit
                    updated.add(
                        AchievementModel(
                            title = "",
                            organization = "",
                            year = "",
                            description = ""
                        )
                    )
                }

                sharedViewModel.cvModelRequestDb.achievementList = updated
                adapter?.submitList(updated.toList())
                adapter?.expandOnly(updated.lastIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // -------------------------------------------------------------------------
    // ⌨️ KEYBOARD HANDLING
    // -------------------------------------------------------------------------
    private fun handleKeyboard(root: View) {
        root.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            root.getWindowVisibleDisplayFrame(r)
            val screenHeight = root.rootView.height
            val keypadHeight = screenHeight - r.bottom

            binding.scrollAchievements.setPadding(
                binding.scrollAchievements.paddingLeft,
                binding.scrollAchievements.paddingTop,
                binding.scrollAchievements.paddingRight,
                if (keypadHeight > screenHeight * 0.15) keypadHeight else 0
            )
        }
    }
}
