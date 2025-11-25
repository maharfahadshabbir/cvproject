package com.example.cvmaker.fragments.profile

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cvmaker.R
import com.example.cvmaker.adaptor.NewProfileAdapter
import com.example.cvmaker.databinding.FragmentNewProfileBinding
import com.example.cvmaker.databinding.ProfileDetailBottomSheetBinding
import com.example.cvmaker.databinding.RenameBottomsheetLayoutBinding
import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import com.example.cvmaker.model.workingmodels.CvProfileItem
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.CvMakerViewModel
import com.example.cvmaker.viewmodels.MyViewModel
import com.example.cvmaker.viewmodels.NewProfileViewModel
import com.example.cvmaker.viewmodels.SharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewProfileFragment : Fragment() {

    private lateinit var binding: FragmentNewProfileBinding

    private val profileAdapter by lazy { NewProfileAdapter() }

    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private val cvMakerViewModel by activityViewModels<CvMakerViewModel>()
    private val myViewModel by activityViewModels<MyViewModel>()   // kept for future delete/rename if you use it
    private val newProfileViewModel by viewModels<NewProfileViewModel>()

    private var lastClickTime = 0L
    private val clickDelay = 500L

    private var onBackPressedCallback: OnBackPressedCallback? = null

    // Local list for items mapped from DB to UI model
    private val profileItems: MutableList<CvProfileItem> = mutableListOf()

    // Keyboard padding flags (kept in case you use them later)
    private var originalRootPaddingBottom: Int = 0
    private var isOriginalPaddingCaptured: Boolean = false


    companion object{
        val draftItems: MutableList<CvProfileItem> = mutableListOf()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()

        binding.profilesRecyclerview.setHasFixedSize(true)
        binding.profilesRecyclerview.isNestedScrollingEnabled = false
        binding.profilesRecyclerview.adapter = profileAdapter

        setupAddProfileAnimation()
        initListener()
        loadProfilesFromDb()
        observeProfiles()
    }

    override fun onResume() {
        super.onResume()
        // 🔁 Always refresh from DB when we return here (including after save)

    }

    private fun setupAddProfileAnimation() {
        val scaleX = ObjectAnimator.ofFloat(binding.addProfile, "scaleX", 1f, 1.1f).apply {
            duration = 600
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }
        val scaleY = ObjectAnimator.ofFloat(binding.addProfile, "scaleY", 1f, 1.1f).apply {
            duration = 600
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }
        AnimatorSet().apply { playTogether(scaleX, scaleY) }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
        isOriginalPaddingCaptured = false
    }

    // -------------------------------------------------------------------------
    // 🔙 BACK PRESS
    // -------------------------------------------------------------------------
    private fun backPressed() {
        when (sharedViewModel.noprofile) {
            "createAiCoverLetter" -> {
                sharedViewModel.profileCase = "createAiCoverLetter"
            }

            "createCv" -> {
                sharedViewModel.profileCase = "createCv"
            }

            else -> {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    private fun configureBackPress() {
        tryCatch {
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            }
            onBackPressedCallback?.let { callback ->
                getViewLifecycleOwnerOrNull()?.let { owner ->
                    activity?.onBackPressedDispatcher?.addCallback(owner, callback)
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 🔄 INIT & LOAD DATA
    // -------------------------------------------------------------------------
    private fun initListener() {
        clickListener()
    }

    /**
     * Trigger ViewModel to fetch from Room.
     * Actual list will come through StateFlow in observeProfiles().
     */
    private fun loadProfilesFromDb() {
        binding.btnProgressBar.isVisible = true
        Log.d("NewProfileFragment", "loadProfilesFromDb: calling VM.loadProfiles()")
        newProfileViewModel.loadProfiles()
    }

    private fun observeProfiles() {
        viewLifecycleOwner.lifecycleScope.launch {
            newProfileViewModel.profiles.collectLatest { entities ->
                Log.d("NewProfileFragment", "Received ${entities.size} profiles")

                val gson = Gson()

                // Filter entities before mapping
                val filteredEntities = entities.filter { it.profileOrDraft } // only true profiles
                var filteredEntitiesDrafts = entities.filter { !it.profileOrDraft } // only drafts

                val itemsDraft = filteredEntitiesDrafts.mapNotNull { entity ->
                    try {
                        val cv = gson.fromJson(entity.json, CvModelRequestDb::class.java)
                        CvProfileItem(
                            id = entity.id,
                            data = cv,
                            updatedAt = entity.updateDate
                        )
                    } catch (e: Exception) {
                        Log.e("NewProfileFragment", "Parse error id=${entity.id}", e)
                        null
                    }
                }
                val items = filteredEntities.mapNotNull { entity ->
                    try {
                        val cv = gson.fromJson(entity.json, CvModelRequestDb::class.java)
                        CvProfileItem(
                            id = entity.id,
                            data = cv,
                            updatedAt = entity.updateDate
                        )
                    } catch (e: Exception) {
                        Log.e("NewProfileFragment", "Parse error id=${entity.id}", e)
                        null
                    }
                }

                draftItems.clear()
                draftItems.addAll(itemsDraft)
                profileItems.clear()
                profileItems.addAll(items)

                // ALWAYS hide progress bar
                binding.btnProgressBar.isVisible = false

                adapterListener()
            }
        }
    }


    /*
        private fun observeProfiles() {
            viewLifecycleOwner.lifecycleScope.launch {
                newProfileViewModel.profiles.collectLatest { entities ->
                    Log.d("NewProfileFragment", "Received ${entities.size} profiles")

                    val gson = Gson()
                    val items = entities.mapNotNull { entity ->
                        try {
                            val cv = gson.fromJson(entity.json, CvModelRequestDb::class.java)
                            CvProfileItem(
                                id = entity.id,
                                data = cv,
                                updatedAt = entity.updateDate
                            )
                        } catch (e: Exception) {
                            Log.e("NewProfileFragment", "Parse error id=${entity.id}", e)
                            null
                        }
                    }

                    profileItems.clear()
                    profileItems.addAll(items)

                    // ALWAYS hide progress bar
                    binding.btnProgressBar.isVisible = false

                    adapterListener()
                }
            }
        }
    */


    private fun showCreateOptionsDialog(cvData: CvModelRequestDb) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_options, null)
        val dialog = BottomSheetDialog(requireContext(), R.style.MyBottomSheetDialog)
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)

 /*       dialogView.findViewById<View>(R.id.cardCv).setOnClickListener {
            dialog.dismiss()
            // User chose CV → Generate with your desired template
            Toast.makeText(requireContext(), "Generating your CV...", Toast.LENGTH_SHORT).show()
            cvMakerViewModel.generate(cvData, templateName = "modern_blue") // Change template as needed
            findNavController().navigate(R.id.cvTemplateFragment)

        }*/


        dialogView.findViewById<View>(R.id.cardCv).setOnClickListener {
            dialog.dismiss()

            // Save data in SharedViewModel
            sharedViewModel.setCvData(cvData) // cvData is your CvModelRequestDb instance
            // Navigate to CV Template fragment
            findNavController().navigate(R.id.cvTemplateFragment)
        }




        dialogView.findViewById<View>(R.id.cardCoverLetter).setOnClickListener {
            dialog.dismiss()
            // Navigate to Cover Letter screen
            sharedViewModel.cvModelRequestDb = cvData
            sharedViewModel.profileCase = "createAiCoverLetter"
//            findNavController().navigate(R.id.action_newProfileFragment_to_coverLetterFragment) // Update destination
        }

        dialog.show()
    }

    // -------------------------------------------------------------------------
    // ♻ ADAPTER + UI STATE
    // -------------------------------------------------------------------------
    private fun adapterListener() {
        tryCatch {
            Log.d("NewProfileFragment", "adapterListener: called, size=${profileItems.size}")


            if (profileItems.isNotEmpty()) {
                binding.profilesRecyclerview.isVisible = true
                binding.emptyDataConst.isVisible = false
                binding.addProfile.isVisible = true
                profileAdapter.setData(profileItems)
            } else {
                binding.profilesRecyclerview.isVisible = false
                binding.emptyDataConst.isVisible = true
                binding.addProfile.isVisible = true
            }

            if (profileAdapter.listener == null) {
                profileAdapter.setOnClickListener(object : NewProfileAdapter.OnClickListener {
                    override fun onPopMenu(position: Int, item: CvProfileItem) {
                        showProfileBottomSheet(position, item)
                    }

                    override fun onItemClick(position: Int, item: CvProfileItem) {
                        showCreateOptionsDialog(item.data)
//                        showEditProfile(item)
                    }

                    override fun onClick(v: View?) {
                        // not used
                    }
                })
            }
        }
    }

    // -------------------------------------------------------------------------
    // 🖱 CLICKS
    // -------------------------------------------------------------------------
    private fun clickListener() {
        binding.backButton.setOnClickListener {
            backPressed()
        }

        binding.addProfile.setOnClickListener {
            tryCatch {
                val now = System.currentTimeMillis()
                if (now - lastClickTime < clickDelay) return@tryCatch
                lastClickTime = now

                // Reset VM for creating new profile
                sharedViewModel.selectedimageasFile = null
                sharedViewModel.selectedimageUri = null
                sharedViewModel.cvModelRequestDb = CvModelRequestDb()
                sharedViewModel.profileCase = "createProfile"

                // Navigate to Create Profile screen
                findNavController().navigate(R.id.createProfileFragment)
            }
        }
    }

    // -------------------------------------------------------------------------
    // ⬇ BOTTOM SHEETS
    // -------------------------------------------------------------------------
    @SuppressLint("SuspiciousIndentation")
    private fun showProfileBottomSheet(position: Int, item: CvProfileItem) {
        tryCatch {
            context?.let { ctx ->
                val sheetBinding =
                    ProfileDetailBottomSheetBinding.inflate(layoutInflater)
                val dialog = BottomSheetDialog(ctx, R.style.MyBottomSheetDialog)
                dialog.setContentView(sheetBinding.root)

                val name = item.data.personalDetails?.name ?: "Untitled Profile"
                sheetBinding.text.text = name

                sheetBinding.renameTxt.setOnClickListener {
                    dialog.dismiss()
                    showRenameBottomSheet()
                }

                sheetBinding.editTxt.setOnClickListener {
                    dialog.dismiss()
                    showEditProfile(item)
                }

                sheetBinding.deleteTxt.setOnClickListener {
                    dialog.dismiss()
                    deleteProfile(item)
                }

                dialog.show()
            }
        }
    }

    private fun deleteProfile(item: CvProfileItem) {
        lifecycleScope.launch {
            try {
                newProfileViewModel.deleteProfileById(item.id) // Now works!
                Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfile(item: CvProfileItem) {
        sharedViewModel.cvModelRequestDb = item.data
        sharedViewModel.editingProfileId = item.id  // ← CRITICAL
        sharedViewModel.profileCase = "updateProfile"
        findNavController().navigate(R.id.createProfileFragment)
    }

    private fun showRenameBottomSheet() {
        tryCatch {
            context?.let { ctx ->
                val bsBinding =
                    RenameBottomsheetLayoutBinding.inflate(layoutInflater)
                val dialog = BottomSheetDialog(ctx, R.style.MyBottomSheetDialog)
                dialog.setContentView(bsBinding.root)

                bsBinding.renameButton.setOnClickListener {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Rename saved", Toast.LENGTH_SHORT).show()
                }

                bsBinding.cancelButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        }
    }
}
