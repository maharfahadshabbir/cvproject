package com.example.cvmaker.fragments.profile

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.example.cvmaker.viewmodels.MyViewModel
import com.example.cvmaker.viewmodels.SharedViewModel
import com.example.cvmaker.R
import com.example.cvmaker.adaptor.NewProfileAdapter
import com.example.cvmaker.cv.CvModelRequest
import com.example.cvmaker.databinding.CreationTypeBottomsheetLayoutBinding
import com.example.cvmaker.databinding.DeleteBottomsheetLayoutBinding
import com.example.cvmaker.databinding.FragmentNewProfileBinding
import com.example.cvmaker.databinding.ProfileDetailBottomSheetBinding
import com.example.cvmaker.databinding.RenameBottomsheetLayoutBinding
import com.example.cvmaker.databinding.SaveBottomsheetLayoutBinding
import com.example.cvmaker.model.profilemodels.ProfileModelUI
import com.example.cvmaker.model.profilemodels.profile.ProfileDb
import com.example.cvmaker.model.profilemodels.profile.UpdateProfileModdel
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.isNetworkAvailable
import com.example.cvmaker.utils.tryCatch
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.getValue

class NewProfileFragment : Fragment() {

    private lateinit var binding: FragmentNewProfileBinding

    private val profileAdapter by lazy { NewProfileAdapter() }

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private val myViewModel by activityViewModels<MyViewModel>()

    private var lastClickTime = 0L

    private val clickDelay = 500L // Set your desired click delay in milliseconds

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var coroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

    // --- New properties for Keyboard Adjustment ---
    private var originalRootPaddingBottom: Int = 0
    private var isOriginalPaddingCaptured: Boolean = false
    // --- End New properties for Keyboard Adjustment ---

    companion object {
        var profileList: MutableList<ProfileModelUI> = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBackPress()
        binding.profilesRecyclerview.setHasFixedSize(true) // Prevents unnecessary re-measurements
        binding.profilesRecyclerview.isNestedScrollingEnabled =
            false // Avoid nested scroll conflicts

        // Set adapter once to avoid losing listeners
        binding.profilesRecyclerview.adapter = profileAdapter

        Log.d("onViewCreated123", "onViewCreated: called")
        initListener()
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

        val animatorSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
        }
        animatorSet.start()


        //AD new profile button

        val scaleXNew = ObjectAnimator.ofFloat(binding.addProfile, "scaleX", 1f, 1.1f).apply {
            duration = 600
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }
        val scaleYNew = ObjectAnimator.ofFloat(binding.addProfile, "scaleY", 1f, 1.1f).apply {
            duration = 600
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }

        val animatorSetNew = AnimatorSet().apply {
            playTogether(scaleXNew, scaleYNew)
        }
        animatorSetNew.start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
        isOriginalPaddingCaptured = false // Reset for next view creation
    }

    private fun backPressed() {
        when (sharedViewModel.noprofile) {
            "createAiCoverLetter" -> {
                sharedViewModel.profileCase = "createAiCoverLetter"

            }

            "createCv" -> {
                sharedViewModel.profileCase = "createCv"

            }

            else -> {

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
            onBackPressedCallback?.let {
                getViewLifecycleOwnerOrNull()?.let { it1 ->
                    activity?.onBackPressedDispatcher?.addCallback(
                        it1, it
                    )
                }
            }
        }
    }

    private fun initListener() {
        retrofitApiListener()
        clickListener()
    }

    private fun retrofitApiListener() {
        /*tryCatch {
            context?.let { ctx ->
                val intValue = ViewUtils.getIntFromSharedPreferences(ctx, "user_id", -1)
                Log.d("ProfileFragment", "retrofitApiListener: user_id = $intValue")
                binding.btnProgressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
                    Log.d("ProfileFragment", "Launching getUserProfiles API call")
                    RetrofitClient.getUserProfiles(intValue, onSuccess = { it2 ->
                        Log.d(
                            "ProfileFragment",
                            "getUserProfiles success: profiles count = ${it2.size}"
                        )
                        launch(coroutineExceptionHandler) {
                            profileList.clear()
                            sharedViewModel.profileList.clear()
                            it2.map {
                                profileList.add(
                                    ProfileModelUI(
                                        id = it.id, profileModelItem = it
                                    )
                                )
                                sharedViewModel.profileList.add(
                                    ProfileModelUI(
                                        id = it.id, profileModelItem = it
                                    )
                                )
                            }


                            //profileAdapter.setData(profileList)
                            Log.d(
                                "ProfileFragment",
                                "Profiles loaded into profileList and sharedViewModel.profileList"
                            )
                            Log.d(
                                "ProfileFragment",
                                "retrofitApiListener: ${profileList.size} \n ${sharedViewModel.profileList.size}"
                            )

                            withContext(Dispatchers.Main) {
                                Log.d(
                                    "ProfileFragment",
                                    "Hiding progress bar and calling adapterListener"
                                )
                                binding.btnProgressBar.visibility = View.GONE
                                adapterListener()
                            }

                        }
                    }, onError = {
                        Log.e("ProfileFragment", "getUserProfiles error")
                        launch(Dispatchers.Main) {
                            withContext(Dispatchers.Main) {
                                Log.d(
                                    "ProfileFragment",
                                    "Hiding progress bar and calling adapterListener"
                                )
                                binding.btnProgressBar.visibility = View.GONE
                                adapterListener()
                            }
                        }
                    })

                }
            }
        }*/
    }

    private fun adapterListener() {
        tryCatch {
            Log.d("ProfileFragment", "adapterListener: called")
            activity?.let {
                context?.let { ctx ->
                    Log.d("ProfileFragment", "adapterListener: context and activity are not null")
                    if (profileList.isNotEmpty()) {
                        Log.d(
                            "ProfileFragment",
                            "adapterListener: profileList is not empty, size = ${profileList.size}"
                        )
                        binding.profilesRecyclerview.isVisible = true
                        binding.addProfile.isVisible = true
                        binding.emptyDataConst.isVisible = false
                        profileAdapter.setData(profileList) // Use `setData()` with `DiffUtil`

                    } else {
                        Log.d("ProfileFragment", "adapterListener: profileList is empty")
                        binding.addProfile.isVisible = false
                        sharedViewModel.noprofile = ""
                        binding.profilesRecyclerview.isVisible = false
                        binding.emptyDataConst.isVisible = true
                    }

                    // Attach click listener only once
                    if (profileAdapter.listener == null) {
                        Log.d("ProfileFragment", "adapterListener: setting profileAdapter listener")
                        profileAdapter.setOnClickListener(object : NewProfileAdapter.OnClickListener {
                            override fun onPopMenu(position: Int, item: ProfileModelUI) {
                                Log.d(
                                    "ProfileFragment",
                                    "adapterListener: onPopMenu clicked at position $position"
                                )
                                showProfileBottomSheet(position, item) {
                                    binding.addProfile.isVisible = false
                                }
                            }

                            override fun onItemClick(position: Int, item: ProfileModelUI) {
                                Log.d(
                                    "ProfileFragment",
                                    "adapterListener: onItemClick clicked at position $position"
                                )
                                showCreationTypeBottomSheet(item)
                            }

                            override fun onClick(v: View?) {
                                Log.d("ProfileFragment", "adapterListener: onClick called")
                            }
                        })
                    }
                }
            }
        }
    }

    private fun clickListener() {
        binding.backButton.setOnClickListener {
            backPressed()
        }

        binding.addProfile.setOnClickListener {
            tryCatch {
                sharedViewModel.selectedimageasFile = null
                sharedViewModel.selectedimageUri = null
                sharedViewModel.cvModel = CvModelRequest()
                sharedViewModel.profileCase = "createProfile"

            }
        }

//        binding.addProfile.setOnClickListener {
//            tryCatch {
//
//                sharedViewModel.selectedimageasFile = null
//                sharedViewModel.selectedimageUri = null
//                sharedViewModel.cvModel = CvModelRequest()
//                sharedViewModel.profileCase = "createProfile"
//
//            }
//        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showProfileBottomSheet(position: Int, item: ProfileModelUI, emptyList: () -> Unit) {
        tryCatch {
            context?.let { ctx ->
                val binding = ProfileDetailBottomSheetBinding.inflate(layoutInflater)
                val bottomSheetDialog = BottomSheetDialog(ctx, R.style.MyBottomSheetDialog)
                bottomSheetDialog.setContentView(binding.root)

                binding.renameTxt.setOnClickListener { showRenameBottomSheet() }

                binding.text.text =
                    item.profileModelItem.first_name + " " + item.profileModelItem.last_name
                binding.editTxt.setOnClickListener {
                    tryCatch {
                        sharedViewModel.profileCase = "updateProfile"
                        if (profileList.isNotEmpty()) sharedViewModel.selectedimageasFile = null
                        sharedViewModel.cvModel = CvModelRequest()
                        sharedViewModel.cvModel.apply {
                            id = profileList[position].profileModelItem.id
                            additional_info = profileList[position].profileModelItem.additional_info
                            image = profileList[position].profileModelItem.image
                            address = profileList[position].profileModelItem.address
                            cover_letter = profileList[position].profileModelItem.cover_letter
                            cv_email = profileList[position].profileModelItem.cv_email
                            date_of_birth = profileList[position].profileModelItem.date_of_birth
                            designation = profileList[position].profileModelItem.designation
                            driving_license = profileList[position].profileModelItem.driving_license
                            educations =
                                profileList[position].profileModelItem.educations.toMutableList()
                            experiences =
                                profileList[position].profileModelItem.experiences.toMutableList()
                            other_skills =
                                profileList[position].profileModelItem.other_skills.toMutableList()
                            references =
                                profileList[position].profileModelItem.references.toMutableList()
                            projects =
                                profileList[position].profileModelItem.projects.toMutableList()
                            interests =
                                profileList[position].profileModelItem.interests.toMutableList()
                            first_name = profileList[position].profileModelItem.first_name + " "
                            gender = profileList[position].profileModelItem.gender
                            last_name = profileList[position].profileModelItem.last_name
                            marital_status = profileList[position].profileModelItem.marital_status
                            objective = profileList[position].profileModelItem.objective
                            phone = profileList[position].profileModelItem.phone
                            user = profileList[position].profileModelItem.user
                            website = profileList[position].profileModelItem.website
                        }

                        sharedViewModel.updateProfileModdel = UpdateProfileModdel()
                        sharedViewModel.updateProfileModdel.apply {
                            template = 1
                            user_profile_id = profileList[position].profileModelItem.id
                            user = profileList[position].profileModelItem.user
                        }

                        bottomSheetDialog.dismiss()

                    }
                }


                binding.renameTxt.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    showSaveBottomSheet()
                }

                binding.deleteTxt.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    showDeleteBottomSheet(item, position) {
                        emptyList.invoke()
                    }
                }

                // Show the bottom sheet
                bottomSheetDialog.show()
            }
        }
    }

    private fun showRenameBottomSheet() {
        tryCatch {
            context?.let { ctx ->
                val binding = RenameBottomsheetLayoutBinding.inflate(layoutInflater)
                val bottomSheetDialog = BottomSheetDialog(ctx, R.style.MyBottomSheetDialog)
                bottomSheetDialog.setContentView(binding.root)
                binding.cancelButton.setOnClickListener {
                    bottomSheetDialog.dismiss()
                }
                // Show the bottom sheet
                bottomSheetDialog.show()
            }
        }
    }

    private fun showCreationTypeBottomSheet(item: ProfileModelUI) {
        tryCatch {
            context?.let { ctx ->

                val binding = CreationTypeBottomsheetLayoutBinding.inflate(layoutInflater)
                val bottomSheetDialog = BottomSheetDialog(ctx, R.style.MyBottomSheetDialog)
                // Set the view for the bottom sheet
                bottomSheetDialog.setContentView(binding.root)

                binding.aiCoverLetter.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    SharedViewModel.selectedProfile = null
                    SharedViewModel.selectedProfile = item
                    createCoverLetter(item)
                }

                binding.createCv.setOnClickListener {

                    bottomSheetDialog.dismiss()
                    SharedViewModel.selectedProfile = null
                    SharedViewModel.selectedProfile = item
                    createCv(item)
                }

                // Show the bottom sheet
                if (!bottomSheetDialog.isShowing) {
                    bottomSheetDialog.show()
                }
            }
        }
    }

    private fun createCv(item: ProfileModelUI) {
        tryCatch {
        /*    context?.let { ctx ->
                var intValue = ViewUtils.getIntFromSharedPreferences(ctx, "user_id", -1)
                val stringValue = ViewUtils.getStringFromSharedPreferences(ctx, "user_id", " ")

                CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
                    if (stringValue.isEmpty() || intValue == -1) {
                        // Generate a unique identifier
                        val uniqueId = UUID.randomUUID().toString()
                        RetrofitClient.createUser(ctx, uniqueId, onSuccess = { it1 ->
                            tryCatch {
                                intValue = it1.data.id
                                ViewUtils.saveDataToSharedPreferences(
                                    ctx, "user_id", uniqueId, it1.data.id
                                )

                            }
                        }) {}
                        getUserProfile(intValue)
                    } else {
                        getUserProfile(intValue)
                    }
                    withContext(Dispatchers.Main) {
                        if (sharedViewModel.profileCount == 0) {
                            Toast.makeText(
                                ctx, getString(R.string.you_have_no_profile), Toast.LENGTH_SHORT
                            ).show()
                            sharedViewModel.noprofile = "createCv"
                            navigateToFragment(
                                sharedNavVM = sharedNavigationViewModel,
                                targetDestinationId = R.id.profileDraftFragment
                            )
                        } else {
                            sharedViewModel.setData(sharedViewModel.profileCount - 1)
                            sharedViewModel.profileCase = "createCv"
                            navigateToFragment(
                                sharedNavVM = sharedNavigationViewModel,
                                targetDestinationId = R.id.fragmentTemplates
                            )
                        }
                    }
                }
            }*/
        }

    }

    private fun showSaveBottomSheet() {
        tryCatch {
            context?.let { ctx ->
                val binding = SaveBottomsheetLayoutBinding.inflate(layoutInflater)
                val bottomSheetDialog = BottomSheetDialog(ctx, R.style.MyBottomSheetDialog)
                bottomSheetDialog.setContentView(binding.root)

                binding.cancelButton.setOnClickListener {
                    bottomSheetDialog.dismiss()
                }
                // Show the bottom sheet
                bottomSheetDialog.show()
            }
        }
    }


    private fun showDeleteBottomSheet(item: ProfileModelUI, position: Int, emptyList: () -> Unit) {
        tryCatch {
            context?.let { ctx ->
                val binding = DeleteBottomsheetLayoutBinding.inflate(layoutInflater)
                val bottomSheetDialog = BottomSheetDialog(ctx, R.style.MyBottomSheetDialog)
                // Set the view for the bottom sheet
                bottomSheetDialog.setContentView(binding.root)

                binding.deleteButton.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
                        // save it in db for restoring profile by profile id
                        myViewModel.insertProfileRequest(ProfileDb(0, item.id))


                        withContext(Dispatchers.Main) {
                            updateUi()
                            profileAdapter.setData(profileList)
                        }
                    }
                }
                binding.cancelButton.setOnClickListener {
                    bottomSheetDialog.dismiss()
                }
                // Show the bottom sheet
                bottomSheetDialog.show()
            }
        }
    }

    private fun updateUi() {
        tryCatch {
            if (profileList.isNotEmpty()) {
//                if (profileList.size >= 3) {
//                    showNativeAdmob()
//                } else {
//                    hideAll()
//                }
                binding.profilesRecyclerview.isVisible = true
                binding.emptyDataConst.isVisible = false
            } else {
                binding.profilesRecyclerview.isVisible = false
                binding.emptyDataConst.isVisible = true
//                hideAll()
            }
        }
    }

    private fun createCoverLetter(item: ProfileModelUI) {
        tryCatch {
           /* context?.let { ctx ->
                var intValue = ViewUtils.getIntFromSharedPreferences(ctx, "user_id", -1)
                val stringValue = ViewUtils.getStringFromSharedPreferences(ctx, "user_id", " ")
                CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
                    if (stringValue.isEmpty() || intValue == -1) {
                        // Generate a unique identifier
                        val uniqueId = UUID.randomUUID().toString()
                        RetrofitClient.createUser(ctx, uniqueId, onSuccess = { it1 ->
                            launch {
                                intValue = it1.data.id
                                ViewUtils.saveDataToSharedPreferences(
                                    ctx, "user_id", uniqueId, it1.data.id
                                )
                            }
                        }) {}
                        getUserProfile(intValue)
                    } else {
                        getUserProfile(intValue)
                    }
                    withContext(Dispatchers.Main) {
                        if (sharedViewModel.profileCount == 0) {
                            Toast.makeText(
                                ctx, getString(R.string.you_have_no_profile), Toast.LENGTH_SHORT
                            ).show()
                            sharedViewModel.noprofile = "createAiCoverLetter"
                            navigateToFragment(
                                sharedNavVM = sharedNavigationViewModel,
                                targetDestinationId = R.id.profileDraftFragment
                            )
                        } else {
                            sharedViewModel.setData(sharedViewModel.profileCount - 1)
                            sharedViewModel.profileCase = "createAiCoverLetter"
                            navigateToFragment(
                                sharedNavVM = sharedNavigationViewModel,
                                targetDestinationId = R.id.fragmentTemplates
                            )
                        }
                    }
                }
            }*/
        }
    }

    private suspend fun getUserProfile(intValue: Int) {
       /* RetrofitClient.getUserProfiles(intValue, onSuccess = { it2 ->
            tryCatch {
                sharedViewModel.profileCount = it2.size
                profileList.clear()
                it2.map {
                    profileList.add(
                        ProfileModelUI(
                            id = it.id, profileModelItem = it
                        )
                    )
                }
                sharedViewModel.profileList.clear()
                sharedViewModel.profileList.addAll(profileList)
            }
        }, onError = {})*/
    }


    override fun onResume() {
        super.onResume()
//        initListener()
    }




}