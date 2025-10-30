package com.example.cvmaker.fragments.profiledetail

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.cvmaker.databinding.FragmentPersonalDetailBinding
import com.example.cvmaker.model.workingmodels.PersonalDetailModel
import com.example.cvmaker.viewmodels.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PersonalDetailFragment : Fragment() {

    private lateinit var binding: FragmentPersonalDetailBinding
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private var photoFile: File? = null
    private var photoUri: Uri? = null
    private var byteArray: ByteArray? = null
    private var isAdditionalVisible = false

    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    /** Camera launcher */
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) handleCameraPhoto()
        }

    /** Gallery launcher */
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleGalleryImage(it) }
        }

    private var backPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonalDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPress()
        setupKeyboardResize()
        setupClicks()
        setupTextWatchers()
        populateExistingData()
        setupKeyboardPadding(view)
    }

    // ------------------------------------------------------------
    // 🖱️ CLICKS
    // ------------------------------------------------------------
    private fun setupClicks() = with(binding) {
        backButton.setOnClickListener { findNavController().popBackStack() }
        previewCv.setOnClickListener { previewCv() }
        expnextbtn.setOnClickListener { validateAndSave() }

        // Profile image
        profilePhoto.setOnClickListener { openPhotoChooser() }
        actionImg.setOnClickListener { openPhotoChooser() }

        // DOB
        etDob.setOnClickListener { showDatePicker(etDob) }
        ivDobPicker.setOnClickListener { showDatePicker(etDob) }

        // Additional Info Toggle
        additionalInfo.setOnClickListener { toggleAdditionalInfo() }

        // Gender
        rbMale.setOnClickListener { onGenderSelected("Male") }
        rbFemale.setOnClickListener { onGenderSelected("Female") }
        rbOther.setOnClickListener { onGenderSelected("Other") }

        // Marital Status
        rbMarried.setOnClickListener { onMaritalSelected("Married") }
        rbUnmarried.setOnClickListener { onMaritalSelected("Unmarried") }

        // Save
        btnSave.setOnClickListener { validateAndSave() }
    }

    private fun toggleAdditionalInfo() {
        isAdditionalVisible = !isAdditionalVisible
        binding.additionalContainer.isVisible = isAdditionalVisible
        Toast.makeText(
            requireContext(),
            if (isAdditionalVisible) "Showing Additional Info" else "Hiding Additional Info",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun onGenderSelected(gender: String) {
        sharedViewModel.cvModel.gender = gender
    }

    private fun onMaritalSelected(status: String) {
        sharedViewModel.cvModel.marital_status = status
    }

    private fun previewCv() {
        Toast.makeText(requireContext(), "Preview CV clicked", Toast.LENGTH_SHORT).show()
    }

    // ------------------------------------------------------------
    // 📸 CAMERA & GALLERY
    // ------------------------------------------------------------
    private fun openPhotoChooser() {
        val options = arrayOf("Camera", "Gallery")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }.show()
    }

    private fun openCamera() {
        photoFile = File.createTempFile(
            "profile_",
            ".jpg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile!!
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraLauncher.launch(intent)
    }

    private fun handleCameraPhoto() {
        photoFile?.let { file ->
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = fixImageRotation(file)
                saveBitmapToViewModel(bitmap)
                withContext(Dispatchers.Main) {
                    Glide.with(requireContext()).load(bitmap).into(binding.profilePhoto)
                }
            }
        }
    }

    private fun handleGalleryImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            saveBitmapToViewModel(bitmap)
            withContext(Dispatchers.Main) {
                Glide.with(requireContext()).load(uri).into(binding.profilePhoto)
            }
        }
    }

    private suspend fun saveBitmapToViewModel(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        byteArray = baos.toByteArray()
        sharedViewModel.selectedimageasFile = photoFile
        sharedViewModel.selectedimageUri = photoUri
    }

    private fun fixImageRotation(file: File): Bitmap {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // ------------------------------------------------------------
    // 📅 DATE PICKER
    // ------------------------------------------------------------
    private fun showDatePicker(target: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                target.setText(dateFormat.format(calendar.time))
                sharedViewModel.cvModel.date_of_birth = dateFormat.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }



    // ------------------------------------------------------------
    // ⌨️ KEYBOARD HANDLING
    // ------------------------------------------------------------
    private fun setupKeyboardResize() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun setupKeyboardPadding(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            binding.scrollView.setPadding(0, 0, 0, if (keypadHeight > screenHeight * 0.15) keypadHeight else 0)
        }
    }

    // ------------------------------------------------------------
    // 🔤 TEXT WATCHERS
    // ------------------------------------------------------------
    private fun setupTextWatchers() = with(binding) {
        nameEdittext.addTextChangedListener { sharedViewModel.cvModel.first_name = it.toString() }
        emailEdittext.addTextChangedListener { sharedViewModel.cvModel.cv_email = it.toString() }
        phoneEdittext.addTextChangedListener { sharedViewModel.cvModel.phone = it.toString() }
        addressEdittext.addTextChangedListener { sharedViewModel.cvModel.address = it.toString() }
    }

    // ------------------------------------------------------------
    // 🔙 BACK HANDLING
    // ------------------------------------------------------------
    private fun setupBackPress() {
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback!!)
    }

    // ------------------------------------------------------------
    // 🧠 RESTORE DATA
    // ------------------------------------------------------------
    private fun populateExistingData() = with(binding) {
        sharedViewModel.cvModel.let {
            nameEdittext.setText(it.first_name)
            emailEdittext.setText(it.cv_email)
            phoneEdittext.setText(it.phone)
            addressEdittext.setText(it.address)
            etDob.setText(it.date_of_birth)

            when (it.gender) {
                "Male" -> rbMale.isChecked = true
                "Female" -> rbFemale.isChecked = true
                "Other" -> rbOther.isChecked = true
            }

            when (it.marital_status) {
                "Married" -> rbMarried.isChecked = true
                "Unmarried" -> rbUnmarried.isChecked = true
            }
        }
        sharedViewModel.selectedimageUri?.let {
            Glide.with(requireContext()).load(it).into(binding.profilePhoto)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback?.remove()
        backPressedCallback = null
    }

    // 💾 VALIDATION & SAVE
    // ------------------------------------------------------------
    private fun validateAndSave() = with(binding) {
        val name = nameEdittext.text.toString().trim()
        val email = emailEdittext.text.toString().trim()
        val phone = phoneEdittext.text.toString().trim()
        val dobb = etDob.text.toString().trim()
        val address = addressEdittext.text.toString().trim()
        val phone2 = etPhone2.text.toString().trim()
        val idCard = etIdcard.text.toString().trim()
        val passport = etPassport.text.toString().trim()
        val nationality = etNationality.text.toString().trim()

        val gender = when {
            rbMale.isChecked -> "Male"
            rbFemale.isChecked -> "Female"
            rbOther.isChecked -> "Other"
            else -> null
        }

        val maritalStatus = when {
            rbMarried.isChecked -> "Married"
            rbUnmarried.isChecked -> "Unmarried"
            else -> null
        }

        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            Toast.makeText(requireContext(), "Name, Email, and Phone are required", Toast.LENGTH_SHORT).show()
            return
        }

        val model = PersonalDetailModel(
            name = name,
            email = email,
            phone = phone,
            dateOfBirth = dobb,
            address = address,
            phone2 = phone2,
            idCard = idCard,
            passport = passport,
            nationality = nationality,
            gender = gender,
            maritalStatus = maritalStatus
        )

        sharedViewModel.cvModelRequestDb.personalDetails = model

        findNavController().navigateUp()
        Toast.makeText(requireContext(), "Personal details saved!", Toast.LENGTH_SHORT).show()
    }

}




/*package com.example.cvmaker.fragments.profiledetail

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.cvmaker.R
import com.example.cvmaker.activities.MainActivity
import com.example.cvmaker.cv.CvModelRequest
import com.example.cvmaker.databinding.FragmentPersonalDetailBinding
import com.example.cvmaker.fragments.profiledetail.util.DatePickerUtil
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.applyCapitalizeFilter
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.bitmapToFile
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.bitmapToUri
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.checkProfileCase
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.convertImageViewToByteArray
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.glideImageToFile
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.hideKeyboardForcefully
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.isKeyboardVisible
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.previewCv
import com.example.cvmaker.fragments.profiledetail.util.ViewUtils.setupEditTextofAdaptors
import com.example.cvmaker.utils.getViewLifecycleOwnerOrNull
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.getValue


class PersonalDetailFragment : Fragment(){


    private lateinit var binding: FragmentPersonalDetailBinding

    var bytearray: ByteArray? = null
    private var photoFile: File? = null
    private var photoUri: Uri? = null

    private val REQUEST_CODE_PICK_IMAGE = 1001
    private val REQUEST_IMAGE_CAPTURE = 1002

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private val datePickerUtil = DatePickerUtil()

    private var onEditTextCompleteListener: OnEditTextCompleteListener? = null


    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var coroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

    private var nameAnalyticTriggered = false
    private var emailAnalyticTriggered = false
    private var phoneAnalyticTriggered = false
    private var addressAnalyticTriggered = false
    private var dobAnalyticTriggered = false

    // Variables for keyboard handling
    private var originalBottomPadding = 0
    private var currentFocusedEditText: EditText? = null

    interface OnEditTextCompleteListener {
        fun onNameTextChangeChange(text: String)
        fun onEmailTextChange(text: String)
        fun onPhoneChange(text: String)
        fun onAddressChange(text: String)
        fun onDobChange(text: String)
    }

    private inner class GenericTextWatcher(private val fieldUpdater: (String) -> Unit) :
        TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Not used in this example
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Not used in this example
        }

        override fun afterTextChanged(s: Editable?) {
            // Notify the listener when EditText changes are completed
            s.let {
                fieldUpdater.invoke(it.toString())
            }
        }
    }

    fun setOnEditTextCompleteListener(listener: OnEditTextCompleteListener) {
        onEditTextCompleteListener = listener
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoFile?.let { file ->
                lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                    val correctedBitmap = fixImageRotation(file)
                    // save to ViewModel
                    sharedViewModel.selectedimageasFile = file
                    sharedViewModel.selectedimageUri = photoUri
                    // Save image as byte array
                    val baos = ByteArrayOutputStream()
                    correctedBitmap?.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    bytearray = baos.toByteArray()
                    withContext(Dispatchers.Main) {
                        Glide.with(requireContext())
                            .load(correctedBitmap)
                            .into(binding.profilePhoto)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonalDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureBackPress()
        setupKeyboardHandling()
        initListner()

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is visible
                onKeyboardShown(keypadHeight)
            } else {
                // Keyboard is hidden
                onKeyboardHidden()
            }
        }
    }

    private fun onKeyboardShown(keyboardHeight: Int) {
        binding.scrollView.setPadding(
            binding.scrollView.paddingLeft,
            binding.scrollView.paddingTop,
            binding.scrollView.paddingRight,
            keyboardHeight
        )
    }

    private fun onKeyboardHidden() {
        binding.scrollView.setPadding(
            binding.scrollView.paddingLeft,
            binding.scrollView.paddingTop,
            binding.scrollView.paddingRight,
            0
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }


    override fun onPause() {
        super.onPause()
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        )
    }

    private fun setupKeyboardHandling() {
        tryCatch {
            // Set focus change listeners for all EditTexts
            setupEditTextFocusListeners()
        }
    }

    private fun setupEditTextFocusListeners() {
        val editTexts = listOf(
            binding.nameEdittext,
            binding.emailEdittext,
            binding.phoneEdittext,
            binding.addressEdittext,
//            binding.nameEdittext
        )

        editTexts.forEach { editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    currentFocusedEditText = editText
                } else if (currentFocusedEditText == editText) {
                    currentFocusedEditText = null
                }
            }
        }
    }

    private fun handleKeyboardOpen(keyboardHeight: Int) {
        tryCatch {
            currentFocusedEditText?.let { focusedEditText ->
                // Post to ensure layout is complete
                binding.root.post {
                    // Calculate if the focused EditText is hidden by keyboard
                    val scrollView = findScrollView(binding.root)
                    scrollView?.let { scroll ->
                        val location = IntArray(2)
                        focusedEditText.getLocationInWindow(location)
                        val editTextBottom = location[1] + focusedEditText.height
                        val displayMetrics = resources.displayMetrics
                        val screenHeight = displayMetrics.heightPixels
                        val visibleArea = screenHeight - keyboardHeight
                        if (editTextBottom > visibleArea) {
                            // EditText is hidden, scroll to make it visible
                            val scrollOffset =
                                editTextBottom - visibleArea + 100 // Add some padding
                            scroll.smoothScrollBy(0, scrollOffset)
                        }
                    }
                }
            }
        }
    }

    private fun handleKeyboardClosed() {
        tryCatch {
            // Optionally scroll back to original position or maintain current position
            // You can implement custom behavior here if needed
            currentFocusedEditText = null
        }
    }

    private fun findScrollView(view: View): androidx.core.widget.NestedScrollView? {
        if (view is androidx.core.widget.NestedScrollView) {
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val scrollView = findScrollView(view.getChildAt(i))
                if (scrollView != null) return scrollView
            }
        }
        return null
    }

    private fun backPressed() {

        findNavController().popBackStack()
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

    private fun initListner() {
        tryCatch {
            activateListner()
            populateData()
            keyboardActionNext()
            applyCapitalizeFilter(binding.nameEdittext)
            pickImage()
            clickListener()
            binding.previewCv.isVisible = checkProfileCase(sharedViewModel)
        }
    }

    private fun clickListener() {

        sharedViewModel.let {
            it.globalDialog?.dismiss()
        }

        binding.backButton.setOnClickListener {
            backPressed()
        }

        binding.etDob.setOnClickListener {
            loadDatePickerandSetDate(binding.nameEdittext)
        }

        binding.nameEdittext.setOnFocusChangeListener { _, hasFocus ->
            tryCatch {
                if (hasFocus) {
                    currentFocusedEditText = binding.nameEdittext
                    context?.let {
                        hideKeyboardForcefully(it)
                        loadDatePickerandSetDate(binding.nameEdittext)
                    }
                }
            }
        }

        binding.expnextbtn.setOnClickListener {
            tryCatch {
                context?.let { ctx ->


//                    binding.asteriskName.visibility =
//                        if (binding.nameEdittext.text.toString().trim()
//                                .isEmpty()
//                        ) View.VISIBLE else View.GONE
//                    binding.asteriskEmail.visibility =
//                        if (binding.emailEdittext.text.toString().trim()
//                                .isEmpty()
//                        ) View.VISIBLE else View.GONE
//                    binding.asteriskPhone.visibility =
//                        if (binding.phoneEdittext.text.toString().trim()
//                                .isEmpty()
//                        ) View.VISIBLE else View.GONE
//                    binding.asteriskDob.visibility = if (binding.nameEdittext.text.toString().trim()
//                            .isEmpty()
//                    ) View.VISIBLE else View.GONE
//                    binding.asteriskAddress.visibility =
//                        if (binding.addressEdittext.text.toString().trim()
//                                .isEmpty()
//                        ) View.VISIBLE else View.GONE

                    if (binding.phoneEdittext.text.isNotEmpty()
                        && binding.emailEdittext.text.isNotEmpty()
                    ) {
                    }
                    else {
                        Toast.makeText(
                            ctx,
                            "At Least Enter Name, Email, Phone Number and your Address",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (binding.phoneEdittext.text.isNotEmpty()
                        && binding.emailEdittext.text.isNotEmpty()
                        && binding.nameEdittext.text.isNotEmpty()
                        && binding.emailEdittext.text.matches(ViewUtils.emailPattern.toRegex())
                        && binding.nameEdittext.text.matches(Regex("^[a-zA-Z\\s]*$"))
                        && binding.phoneEdittext.text.matches(ViewUtils.phoneNumberPattern.toRegex())
                        && binding.addressEdittext.text!!.isNotEmpty()
                    ) {
//                        if (BillingUtilsIAP.isPurchased) {
//                            navigateToFragment(
//                                sharedNavVM = sharedNavigationViewModel,
//                                targetDestinationId = R.id.customHomeFragment
//                            )
//                        } else {
//                            activity?.let {
//                                if (it is MainActivity) {
//                                    it.setVisibility(false)
//                                }
//                            }
//
//                            when (RemoteConfig.all_premium_screen_type) {
//                                1 -> {
//                                    navigateToFragment(
//                                        sharedNavVM = sharedNavigationViewModel,
//                                        targetDestinationId = R.id.freePremiumTrialFragment
//                                    )
//                                }
//
//                                2 -> {
//                                    navigateToFragment(
//                                        sharedNavVM = sharedNavigationViewModel,
//                                        targetDestinationId = R.id.freeTrialPremiumFragment
//                                    )
//                                }
//
//                                3 -> {
//                                    navigateToFragment(
//                                        sharedNavVM = sharedNavigationViewModel,
//                                        targetDestinationId = R.id.fiftyWeeklyPremiumFragment
//                                    )
//
//                                }
//
//                                4 -> {
//                                    navigateToFragment(
//                                        sharedNavVM = sharedNavigationViewModel,
//                                        targetDestinationId = R.id.weeklyTrialFragment
//                                    )
//                                }
//                            }
//                        }

                        Toast.makeText(
                            ctx,
                            "At Least Enter Name, Email, Phone Number and your Address",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            ctx,
                            "At Least Enter Name, Email, Phone Number and your Address",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.previewCv.setOnClickListener {
//            previewCv(sharedNavigationViewModel, sharedViewModel, R.id.fragmentPreviewApi)
        }
    }

    private fun pickImage() {
        tryCatch {
            context?.let { ctx ->
                bytearray = convertImageViewToByteArray(binding.profilePhoto)

                binding.actionImg.setOnClickListener {


                    if (isKeyboardVisible(ctx)) {
                        hideKeyboardForcefully(ctx)
                    }

                    openCamera()

//                    SelectPictureBottomSheet { action ->
//                        when (action) {
//                            0 -> {
//                                openCamera()
//                            }
//
//                            1 -> {
//                                showPhotoChooser()
//                            }
//                        }
//                    }.show(childFragmentManager, "SelectPictureBottomSheet")
                }
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create a temp file inside app's private storage
        photoFile = File.createTempFile(
            "camera_photo_",
            ".jpg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile!!
        )

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraLauncher.launch(intent)

    }

    private fun handleCameraResult(data: Intent?) {
        tryCatch {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                binding.profilePhoto.setImageBitmap(it)
                // Save the bitmap to a file or URI as needed
                lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                    val file = bitmapToFile(it, "camera_photo")
                    sharedViewModel.selectedimageasFile = file
                    val uri = bitmapToUri(requireContext(), it)
                    sharedViewModel.selectedimageUri = uri

                    // Save image as byte array
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    bytearray = byteArrayOutputStream.toByteArray()

                    withContext(Dispatchers.Main) {
                        // Update UI if needed, e.g., load the image into an ImageView
                        Glide.with(requireContext()).load(uri).into(binding.profilePhoto)
                    }
                }
            }
        }
    }

    private fun activateListner() {
        tryCatch {
            if (sharedViewModel.cvModel != null) {
                addListner()
            } else {
                val names = extractFirstAndLastName(binding.nameEdittext.text.toString())
                sharedViewModel.cvModel.first_name = names.first
                sharedViewModel.cvModel.last_name = names.second
                sharedViewModel.cvModel.phone = binding.phoneEdittext.text.toString()
                sharedViewModel.cvModel.cv_email = binding.emailEdittext.text.toString()
                sharedViewModel.cvModel.address = binding.addressEdittext.text.toString()
                sharedViewModel.cvModel.date_of_birth = binding.nameEdittext.text.toString()
                addListner()
            }
        }
    }

    private fun populateData() {
        tryCatch {
            context?.let { ctx ->
                if (sharedViewModel.cvModel != null) {
                    // Load profile photo
                    // Check if personalDetail is not null before accessing its properties
                    loadProfilePhoto(sharedViewModel.cvModel)
                    // Set text values

                    setTextValue(
                        binding.nameEdittext,
                        sharedViewModel.cvModel.first_name + sharedViewModel.cvModel.last_name
                    )
                    setTextValue(binding.emailEdittext, sharedViewModel.cvModel.cv_email)
                    setTextValue(binding.phoneEdittext, sharedViewModel.cvModel.phone)
                    setTextValue(binding.addressEdittext, sharedViewModel.cvModel.address)

                    if (sharedViewModel.cvModel.date_of_birth != null) {
                        if (sharedViewModel.cvModel.date_of_birth?.isNotEmpty() == true) {
                            // Reverse the format conversion: Parse "yyyy-MM-dd" and output "MM/dd/yyyy"
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

                            try {
                                val parsedDate =
                                    inputFormat.parse(sharedViewModel.cvModel.date_of_birth)
                                val reverseFormat = parsedDate?.let { outputFormat.format(it) }
                                setTextValue(binding.nameEdittext, reverseFormat)
                            } catch (e: ParseException) {
                                Log.e(
                                    "DateParsing",
                                    "Invalid date format: $sharedViewModel.cvModel.date_of_birth"
                                )
                            }
                        }
                    } else {
                        setTextValue(binding.nameEdittext, sharedViewModel.cvModel.date_of_birth)
                    }

                    Log.e("dateStored", "${sharedViewModel.cvModel.date_of_birth}-populate")
                }

                sharedViewModel.selectedimageUri?.let { it1 ->
                    Log.d("TAG_onActivityResult", "onActivityResult: uri is not null")
                    val imageView = binding.profilePhoto

                    CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.isVisible = true
                        }

                        val bitmap = ViewUtils.loadBitmapFromUri(
                            it1,
                            ctx
                        )
                        if (bitmap != null) {
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                            bytearray = byteArrayOutputStream.toByteArray()

                            if (sharedViewModel.cvModel != null) {
                                val file = bitmapToFile(bitmap, "userprofile")
                                sharedViewModel.selectedimageasFile = file
                            } else {
                                // Handle the case where the list is empty, e.g., add a new element to the list.
                            }
                        }

                        withContext(Dispatchers.Main) {
                            binding.progressBar.isVisible = false
                            Glide.with(ctx)
                                .load(it1)
                                .into(imageView)
                        }
                    }
                }
            }
        }
    }

    private fun keyboardActionNext() {
        tryCatch {
            setupEditTextofAdaptors(
                binding.nameEdittext,
                InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                EditorInfo.IME_ACTION_NEXT,
                binding.emailEdittext
            )
            setupEditTextofAdaptors(
                binding.emailEdittext,
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                EditorInfo.IME_ACTION_NEXT,
                binding.phoneEdittext
            )
            setupEditTextofAdaptors(
                binding.phoneEdittext,
                InputType.TYPE_CLASS_PHONE,
                EditorInfo.IME_ACTION_DONE
            )
            setupEditTextofAdaptors(
                binding.phoneEdittext,
                InputType.TYPE_CLASS_PHONE,
                EditorInfo.IME_ACTION_NEXT,
                binding.addressEdittext
            )
            setupEditTextofAdaptors(
                binding.addressEdittext,
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                EditorInfo.IME_ACTION_NEXT,
                binding.nameEdittext
            )
        }
    }

    private fun loadDatePickerandSetDate(editText: EditText) {
        tryCatch {
            context?.let { ctx ->
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

                val previousDate = editText.text.toString()
                if (previousDate.isNotEmpty()) {
                    try {
                        val parsedDate = dateFormat.parse(previousDate)
                        parsedDate?.let { calendar.time = it }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }
                datePickerUtil.showDatePickerDialog(
                    calendar,
                    0,
                    ctx,
                    object : DatePickerUtil.DateSelectedListener {
                        override fun onDateSelected(formattedDate: String) {
                            tryCatch {
                                val currentCalendar = Calendar.getInstance()
                                val currentYear = currentCalendar.get(Calendar.YEAR)
                                val currentday = currentCalendar.get(Calendar.DAY_OF_MONTH)
                                val currentMonth =
                                    currentCalendar.get(Calendar.MONTH) + 1
                                binding.nameEdittext.setText(formattedDate)

                                if (binding.nameEdittext.text.isNotEmpty()) {
                                    val selectedParts = binding.nameEdittext.text.split("/")

                                    val selectedMonth = selectedParts[0].toIntOrNull()
                                    val selectedDay = selectedParts[1].toIntOrNull()
                                    val selectedYear = selectedParts[2].toIntOrNull()

                                    if (selectedYear != null) {
                                        if (selectedMonth != null) {
                                            if (selectedDay != null) {
                                                if (((selectedYear < (currentYear)) || (selectedYear == currentYear && selectedMonth < currentMonth) || (selectedYear == currentYear && selectedMonth == currentMonth && selectedDay <= currentday))) {
                                                    binding.nameEdittext.error = null
                                                } else {
                                                    binding.nameEdittext.error =
                                                        ctx.getString(R.string.dateofbithmsg)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    })
            }
        }
    }

    private fun loadProfilePhoto(personalDetail: CvModelRequest?) {
        tryCatch {
            context?.let { ctx ->
                if (personalDetail != null) {
                    if (personalDetail.image != null) {
                        binding.progressBar.isVisible = true
                        if (personalDetail.image == "bitmaptype") {
                            sharedViewModel.cvModel.image = null
                            Glide.with(ctx)
                                .asBitmap()
                                .load(sharedViewModel.imageasBitmap)
                                .into(binding.profilePhoto)

                            sharedViewModel.selectedimageUri =
                                sharedViewModel.imageasBitmap?.let { bitmapToUri(ctx, it) }
                            binding.progressBar.isVisible = false
                            sharedViewModel.imageasBitmap = null

                        } else {
                            Log.d("TAGpersonalDetail", "loadProfilePhoto: ${personalDetail.image}")

                            Glide.with(ctx)
                                .load(personalDetail.image)
                                .placeholder(R.drawable.user_profile)
                                .error(R.drawable.user_profile)
                                .centerCrop()
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable>,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        Toast.makeText(
                                            ctx,
                                            "Failed to load image",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        binding.progressBar.isVisible = false
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable,
                                        model: Any,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        binding.progressBar.isVisible = false
                                        return false
                                    }
                                })
                                .into(binding.profilePhoto)

                            var file: File? = null
                            CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
//                                file = glideImageToFile(
//                                    ctx,
//                                    RetrofitClient.BASE_URL + "/media/images/" + personalDetail.image?.substringAfter(
//                                        "/media/images/"
//                                    )
//                                )

                                withContext(Dispatchers.Main) {
                                    if (file != null) {
                                        sharedViewModel.selectedimageasFile = file
                                    } else {
                                        Log.e("ImageFileError", "Failed to retrieve image file.")
                                    }
                                }
                            }
                        }
                    } else {
                        sharedViewModel.selectedimageUri?.let {
                            Glide.with(ctx)
                                .load(it)
                                .placeholder(R.drawable.user_profile)
                                .error(R.drawable.user_profile)
                                .into(binding.profilePhoto)

                        } ?: run {
                            Glide.with(ctx)
                                .load(R.drawable.user_profile)
                                .placeholder(R.drawable.user_profile)
                                .error(R.drawable.user_profile)
                                .into(binding.profilePhoto)
                        }
                    }
                }
            }
        }
    }

    private fun setTextValue(textView: TextView, value: String?) {
        value.let {
            textView.text = it
        }
    }

    fun addListner() {

        binding.nameEdittext.addTextChangedListener(GenericTextWatcher { newText ->
            tryCatch {
                activity?.let {
                    if (newText.isNotEmpty()) {
                        if (it is MainActivity) {
                            if (!nameAnalyticTriggered) {
                                nameAnalyticTriggered = true
                            }
                        }
                    }
                }
                if (newText.isEmpty()) {
                    binding.nameEdittext.error = null
                }
                if (newText.isNotEmpty() && newText[0] == ' ') {
                    binding.nameEdittext.setText(newText.trimStart())
                    binding.nameEdittext.setSelection(minOf(30, binding.nameEdittext.text.length))
                } else {
                    binding.nameEdittext.setSelection(minOf(30, binding.nameEdittext.text.length))
                }
                val names = extractFirstAndLastName(newText)
                sharedViewModel.cvModel.apply {
                    this.first_name = names.first
                    this.last_name = names.second
                }
                if (newText.isNotEmpty()) {
                    if (binding.nameEdittext.text.length < 35) {
                        if (!ViewUtils.validateInput(binding.nameEdittext.text.toString())) {
                            binding.nameEdittext.error = "Invalid Name"
                        } else {
                            binding.nameEdittext.error = null
                        }
                    } else {
                        val trimmedText = binding.nameEdittext.text.substring(0, 34)
                        binding.nameEdittext.setText(trimmedText)
                        binding.nameEdittext.setSelection(trimmedText.length)
                        binding.nameEdittext.error = "Character limit exceeded (30 characters max)."
                    }
                } else {
                    //binding.nameEdittext.error = "Name is Required"
                }
                onEditTextCompleteListener?.onNameTextChangeChange(newText)
            }
        })

        binding.emailEdittext.addTextChangedListener(GenericTextWatcher { newText ->
            tryCatch {
                activity?.let {
                    if (newText.isNotEmpty()) {
                        if (it is MainActivity) {
                            if (!emailAnalyticTriggered) {
                                emailAnalyticTriggered = true
                            }
                        }
                    }
                }
                if (newText.isEmpty()) {
                    binding.emailEdittext.error = null
                }
                sharedViewModel.cvModel.apply {
                    this.cv_email = newText
                }

                if (newText.isNotEmpty()) {
                    if (binding.emailEdittext.text.length < 35) {
                        if (!newText.matches(ViewUtils.emailPattern.toRegex())) {
                            binding.emailEdittext.error = "Invalid Email"
                        } else {
                            binding.emailEdittext.error = null
                        }
                    } else {
                        val trimmedText = binding.emailEdittext.text.substring(0, 34)
                        binding.emailEdittext.setText(trimmedText)
                        binding.emailEdittext.setSelection(trimmedText.length)
                        binding.emailEdittext.error =
                            "Character limit exceeded (30 characters max)."
                    }
                } else {
                    // binding.emailEdittext.error = "Email is Required"
                }

                onEditTextCompleteListener?.onEmailTextChange(newText)
            }
        })

        binding.phoneEdittext.addTextChangedListener(GenericTextWatcher { newText ->
            tryCatch {
                activity?.let {
                    if (newText.isNotEmpty()) {
                        if (it is MainActivity) {
                            if (!phoneAnalyticTriggered) {
                                phoneAnalyticTriggered = true
                            }
                        }
                    }
                }
                val phoneText = binding.phoneEdittext.text.toString().trim()

                if (newText.isEmpty()) {
                    binding.phoneEdittext.error = null
                }

                sharedViewModel.cvModel.apply {
                    this.phone = phoneText
                }

                if (phoneText.isNotEmpty()) {
                    if (phoneText.length in 7..15) {
                        if (phoneText.firstOrNull() == '+' || phoneText.firstOrNull() == '0' || phoneText.all { it.isDigit() }) {
                            val validPhoneNumberRegex = "^[+]?[0-9]{7,14}$".toRegex()

                            if (validPhoneNumberRegex.matches(phoneText)) {
                                binding.phoneEdittext.error = null
                            } else {
                                binding.phoneEdittext.error = "Invalid phone number"
                            }
                        } else {
                            binding.phoneEdittext.error = "Phone number should contain only digits"
                        }
                    } else if (phoneText.length > 15) {
                        val trimmedText = phoneText.substring(0, 15)
                        binding.phoneEdittext.setText(trimmedText)
                        binding.phoneEdittext.setSelection(trimmedText.length)
                        binding.phoneEdittext.error = "Phone number should be at most 15 digits"
                    } else {
                        binding.phoneEdittext.error = "Phone number should be at least 7 digits"
                    }
                } else {
                    // binding.phoneEdittext.error = "Phone number is required"
                }

                onEditTextCompleteListener?.onPhoneChange(newText)
            }
        })

        binding.addressEdittext.addTextChangedListener(GenericTextWatcher { newText ->
            tryCatch {
                activity?.let {
                    if (newText.isNotEmpty()) {
                        if (it is MainActivity) {
                            if (!addressAnalyticTriggered) {
                                addressAnalyticTriggered = true
                            }
                        }
                    }
                }
                if (newText.isEmpty()) {
                    binding.addressEdittext.error = null
                }
                val addressstring = binding.addressEdittext.text.toString().trim()
                val wordLimit = 200

                sharedViewModel.cvModel.apply {
                    this.address = addressstring
                }

                if (addressstring.isNotEmpty()) {
                    if (addressstring.length <= wordLimit) {
                        binding.addressEdittext.error = null
                    } else {
                        val trimmedText = addressstring.substring(0, wordLimit)
                        binding.addressEdittext.setText(trimmedText)
                        binding.addressEdittext.setSelection(trimmedText.length)
                        binding.addressEdittext.error = "Exceeded word limit (200 characters max)."
                    }
                } else {
                    // binding.addressEdittext.error = "Address is required"
                }

                binding.addressEdittext.maxLines = 5
                binding.addressEdittext.isVerticalScrollBarEnabled = true

                sharedViewModel.cvModel.address = newText
                onEditTextCompleteListener?.onAddressChange(newText)
            }
        })

        binding.nameEdittext.addTextChangedListener(GenericTextWatcher { newText ->
            tryCatch {
                activity?.let {
                    if (newText.isNotEmpty()) {
                        if (it is MainActivity) {
                            if (!dobAnalyticTriggered) {
                                dobAnalyticTriggered = true
                            }
                        }
                    }
                }
                sharedViewModel.cvModel.date_of_birth = newText
                Log.e("dateStored", "${binding.nameEdittext.text}-addTextChangedListener")

                if (newText.isNotEmpty()) {
                    val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    try {
                        val parsedDate = inputFormat.parse(newText)
                        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                        sharedViewModel.cvModel.apply {
                            this.date_of_birth = outputFormat.format(parsedDate!!)
                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        Log.d("MyNewTag", "exception here")
                    }
                } else {
                    sharedViewModel.cvModel.apply {
                        this.date_of_birth = null
                    }
                    Log.d("MyNewTag", "Date string is empty")
                }

                onEditTextCompleteListener?.onDobChange(newText)
            }
        })
    }

    private fun extractFirstAndLastName(fullName: String): Pair<String, String> {
        val words = fullName.split(" ")

        val firstName = if (words.size > 1) {
            words.subList(0, words.size - 1).joinToString(" ")
        } else {
            fullName
        }

        val lastName = if (words.size > 1) {
            words.last()
        } else {
            ""
        }

        return Pair(firstName, lastName)
    }

    private fun showPhotoChooser() {
        tryCatch {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                val imageView = binding.profilePhoto

                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.isVisible = true
                    }

                    sharedViewModel.selectedimageUri = uri

                    val bitmap =
                        ViewUtils.loadBitmapFromUri(
                            uri,
                            requireContext()
                        )
                    if (bitmap != null) {
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                        bytearray = byteArrayOutputStream.toByteArray()

                        if (sharedViewModel.cvModel != null) {
                            val file = bitmapToFile(bitmap, "userprofile")
                            sharedViewModel.selectedimageasFile = file
                        } else {
                            // Handle the case where the list is empty, e.g., add a new element to the list.
                        }
                    }

                    withContext(Dispatchers.Main) {
                        binding.progressBar.isVisible = false
                        Glide.with(requireContext())
                            .load(uri)
                            .into(imageView)
                    }
                }
            } ?: run {
                sharedViewModel.selectedimageasFile = null
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                val imageView = binding.profilePhoto

                CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.isVisible = true
                    }
                    sharedViewModel.selectedimageUri = uri

                    val bitmap = ViewUtils.loadBitmapFromUri(
                        uri,
                        requireContext()
                    )
                    if (bitmap != null) {
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                        bytearray = byteArrayOutputStream.toByteArray()

                        if (sharedViewModel.cvModel != null) {
                            val file = bitmapToFile(bitmap, "userprofile")
                            sharedViewModel.selectedimageasFile = file
                        } else {
                            // Handle the case where the list is empty, e.g., add a new element to the list.
                        }
                    }

                    withContext(Dispatchers.Main) {
                        binding.progressBar.isVisible = false
                        context?.let {
                            Glide.with(it)
                                .load(uri)
                                .into(imageView)
                        }
                    }
                }
            } else {
                sharedViewModel.selectedimageasFile = null
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            handleCameraResult(data)
        }
    }
    private fun fixImageRotation(file: File): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
        val exif = ExifInterface(file.absolutePath)

        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }




}*/