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
import com.example.cvmaker.viewmodels.MyViewModel
import com.example.cvmaker.viewmodels.SharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineExceptionHandler

class NewProfileFragment : Fragment() {

    private lateinit var binding: FragmentNewProfileBinding

    private val profileAdapter by lazy { NewProfileAdapter() }

    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private val myViewModel by activityViewModels<MyViewModel>()

    private var lastClickTime = 0L
    private val clickDelay = 500L

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("NewProfileFragment", "Coroutine error: ${throwable.localizedMessage}", throwable)
    }

    // Local list for items from DB
    private val profileItems: MutableList<CvProfileItem> = mutableListOf()

    // Keyboard padding (you already had these flags; kept for consistency)
    private var originalRootPaddingBottom: Int = 0
    private var isOriginalPaddingCaptured: Boolean = false

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
                // default behaviour, you can navigate back if you want:
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
        loadProfilesFromDb()
        clickListener()
    }

    /**
     * Fetch data from Room using MyViewModel and convert to CvProfileItem list.
     */
    private fun loadProfilesFromDb() {
        lifecycleScope.launch(coroutineExceptionHandler) {
            try {
                binding.btnProgressBar.isVisible = true

                val entities = withContext(Dispatchers.IO) {
                    myViewModel.getCvModelRequest()
                }

                Log.d("NewProfileFragment", "Fetched ${entities.size} entities from DB")

                val gson = Gson()
                val items = entities.mapNotNull { entity ->
                    try {
                        val cv = gson.fromJson(entity?.json, CvModelRequestDb::class.java)
                        CvProfileItem(
                            id = entity?.id,
                            data = cv,
                            updatedAt = entity?.updateDate
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "NewProfileFragment",
                            "Failed to deserialize CV JSON (id=${entity?.id}): ${e.localizedMessage}"
                        )
                        null
                    }
                }

                profileItems.clear()
                profileItems.addAll(items)

                Log.d("NewProfileFragment", "Deserialized ${profileItems.size} profiles")

                withContext(Dispatchers.Main) {
                    binding.btnProgressBar.isVisible = false
                    adapterListener()
                }
            } catch (e: Exception) {
                Log.e("NewProfileFragment", "Error loading profiles from DB", e)
                withContext(Dispatchers.Main) {
                    binding.btnProgressBar.isVisible = false
                    adapterListener() // still update UI as "empty" on error
                    Toast.makeText(requireContext(), "Failed to load profiles", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // ♻ ADAPTER + UI STATE
    // -------------------------------------------------------------------------
    private fun adapterListener() {
        tryCatch {
            Log.d("NewProfileFragment", "adapterListener: called")

            if (profileItems.isNotEmpty()) {
                Log.d(
                    "NewProfileFragment",
                    "adapterListener: profileItems not empty, size=${profileItems.size}"
                )
                binding.profilesRecyclerview.isVisible = true
                binding.emptyDataConst.isVisible = false
                binding.addProfile.isVisible = true
                profileAdapter.setData(profileItems)
            } else {
                Log.d("NewProfileFragment", "adapterListener: profileItems empty")
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
                        // You can directly open edit here if you want
                        showEditProfile(item)
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
    // ⬇ BOTTOM SHEETS (EDIT / RENAME – DB WIRED LATER IF YOU WANT)
    // -------------------------------------------------------------------------
    @SuppressLint("SuspiciousIndentation")
    private fun showProfileBottomSheet(position: Int, item: CvProfileItem) {
        tryCatch {
            context?.let { ctx ->
                val sheetBinding =
                    ProfileDetailBottomSheetBinding.inflate(layoutInflater)
                val dialog = BottomSheetDialog(ctx, R.style.MyBottomSheetDialog)
                dialog.setContentView(sheetBinding.root)

                val name =
                    item.data.personalDetails?.name ?: "Untitled Profile"
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
                    // TODO: hook up delete from DB using myViewModel when ready
                    Toast.makeText(requireContext(), "Delete logic to be implemented", Toast.LENGTH_SHORT).show()
                }

                dialog.show()
            }
        }
    }

    private fun showEditProfile(item: CvProfileItem) {
        tryCatch {
            // Put full CV data back into shared VM
            sharedViewModel.cvModelRequestDb = item.data
            // if you track id in VM:
            // sharedViewModel.setCvIdForEdit(item.id)  // create helper if needed
            sharedViewModel.profileCase = "updateProfile"

            findNavController().navigate(R.id.createProfileFragment)
        }
    }

    private fun showRenameBottomSheet() {
        tryCatch {
            context?.let { ctx ->
                val binding =
                    RenameBottomsheetLayoutBinding.inflate(layoutInflater)
                val dialog = BottomSheetDialog(ctx, R.style.MyBottomSheetDialog)
                dialog.setContentView(binding.root)

                binding.renameButton.setOnClickListener {
                    // TODO: handle rename if you store a name in DB
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Rename saved", Toast.LENGTH_SHORT).show()
                }

                binding.cancelButton.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        }
    }
}
