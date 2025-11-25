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
import android.util.Log
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
import androidx.core.net.toUri

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
        ensurePersonalModel()
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

    private fun ensurePersonalModel(): PersonalDetailModel {
        val existing = sharedViewModel.cvModelRequestDb.personalDetails
        return if (existing != null) {
            Log.d("ensurePersonalModel", "Existing PersonalDetailModel found: $existing")
            existing
        } else {
            val newModel = PersonalDetailModel()
            sharedViewModel.cvModelRequestDb.personalDetails = newModel
            Log.d("ensurePersonalModel", "No existing PersonalDetailModel, created new one: $newModel")
            newModel
        }
    }



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

    // CAMERA RESULT
    private fun handleCameraPhoto() {
        photoFile?.let { file ->
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = fixImageRotation(file)
                // ✅ pass photoUri so we can store it
                saveBitmapToViewModel(bitmap, photoUri)
                withContext(Dispatchers.Main) {
                    Glide.with(requireContext()).load(bitmap).into(binding.profilePhoto)
                }
            }
        }
    }

    // GALLERY RESULT
    private fun handleGalleryImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            // ✅ pass gallery uri so we can store it
            saveBitmapToViewModel(bitmap, uri)
            withContext(Dispatchers.Main) {
                Glide.with(requireContext()).load(uri).into(binding.profilePhoto)
            }
        }
    }

    private suspend fun saveBitmapToViewModel(bitmap: Bitmap, sourceUri: Uri?) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        byteArray = baos.toByteArray()

        sharedViewModel.selectedimageasFile = photoFile
        sharedViewModel.selectedimageUri = sourceUri

        // 🔥 Save into PersonalDetailModel so it goes into CvModelRequestDb → Room
        val model = ensurePersonalModel()
        model.imageUri = sourceUri?.toString()
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
        nameEdittext.addTextChangedListener {
            val model = ensurePersonalModel()
            model.name = it?.toString()
        }
        emailEdittext.addTextChangedListener {
            val model = ensurePersonalModel()
            model.email = it?.toString()
        }
        phoneEdittext.addTextChangedListener {
            val model = ensurePersonalModel()
            model.phone = it?.toString()
        }
        addressEdittext.addTextChangedListener {
            val model = ensurePersonalModel()
            model.address = it?.toString()
        }
        etPhone2.addTextChangedListener {
            val model = ensurePersonalModel()
            model.phone2 = it?.toString()
        }
        etIdcard.addTextChangedListener {
            val model = ensurePersonalModel()
            model.idCard = it?.toString()
        }
        etPassport.addTextChangedListener {
            val model = ensurePersonalModel()
            model.passport = it?.toString()
        }
        etNationality.addTextChangedListener {
            val model = ensurePersonalModel()
            model.nationality = it?.toString()
        }
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
        // ✅ Prefer new DB model
        sharedViewModel.cvModelRequestDb.personalDetails?.let { model ->
            nameEdittext.setText(model.name)
            emailEdittext.setText(model.email)
            phoneEdittext.setText(model.phone)
            addressEdittext.setText(model.address)
            etDob.setText(model.dateOfBirth)
            etPhone2.setText(model.phone2)
            etIdcard.setText(model.idCard)
            etPassport.setText(model.passport)
            etNationality.setText(model.nationality)

            when (model.gender) {
                "Male" -> rbMale.isChecked = true
                "Female" -> rbFemale.isChecked = true
                "Other" -> rbOther.isChecked = true
            }

            when (model.maritalStatus) {
                "Married" -> rbMarried.isChecked = true
                "Unmarried" -> rbUnmarried.isChecked = true
            }

            // 🔥 Load image from saved imageUri if available
            model.imageUri?.let { saved ->
                val uri = saved.toUri()
                Glide.with(requireContext()).load(uri).into(profilePhoto)
            }
        } ?: run {
            // 🔁 Fallback to old cvModel (if any old data still there)
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

            // If not in new model but still in VM (old behavior)
            sharedViewModel.selectedimageUri?.let {
                Glide.with(requireContext()).load(it).into(profilePhoto)
            }
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

        // keep existing image if already set
        val existingImageUri =
            sharedViewModel.cvModelRequestDb.personalDetails?.imageUri
                ?: sharedViewModel.selectedimageUri?.toString()

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
            maritalStatus = maritalStatus,
            imageUri = existingImageUri
        )

        sharedViewModel.cvModelRequestDb.personalDetails = model

        findNavController().navigateUp()
        Toast.makeText(requireContext(), "Personal details saved!", Toast.LENGTH_SHORT).show()
    }


}
