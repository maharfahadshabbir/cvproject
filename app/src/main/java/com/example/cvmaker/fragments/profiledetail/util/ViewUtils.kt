package com.example.cvmaker.fragments.profiledetail.util

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.example.cvmaker.R
import com.example.cvmaker.cv.CvModelRequest
import com.example.cvmaker.utils.showToastSafe
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ExecutionException
import java.util.regex.Pattern

object ViewUtils {
    private lateinit var sharedPreferences: SharedPreferences


//    private var createUserJob: Job? = null

    var cvDialogShowing = false

//    var dialogFragment: ProgressDialogFragment? = null

    var progress = 0


    var error = ""

    //    val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z]+\\.[a-z]+"
    val emailPattern = Pattern.compile(
        "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
    )


    val phoneNumberPattern = "^[+]?[0-9]{7,14}$"

    val PREFS_NAME = "AppOpenAdPrefs"
    val AD_COUNT_KEY = "ad_open_count"


    var first_blink = 0

    // for app languages
    var LANG_KEY = "language_key"
    var LANG_KEY1 = "language_key1"
    var LANG_NAME_KEY = "language_name_key"
    var PREFERENCE_NAME = "MyPreferences"

    private var lastClickTime = 0L
    private const val clickDelay = 500L // Set your desired click delay in milliseconds
    fun View.setDebouncedClickListener(listener: View.OnClickListener) {
        setOnClickListener { view ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= clickDelay) {
                lastClickTime = currentTime
                listener.onClick(view)
            }
        }
    }

    // for first launch
    private const val FIRST_LAUNCH_KEY = "FIRST_LAUNCH"
    private const val LANGUAGE_KEY = "LANGUAGE"

//    fun init(app: Context) {
//        sharedPreferences = app.getSharedPreferences(Constants.PREF_KEY, Context.MODE_PRIVATE)
//    }

    fun isFirstLaunch(context: Context): Boolean {

        return getStringSharedPreferences(context, FIRST_LAUNCH_KEY, "true").toBoolean()
    }

    fun setFirstLaunch(context: Context, isFirstLaunch: Boolean) {
        setStringSharedPreferences(context, FIRST_LAUNCH_KEY, isFirstLaunch.toString())
    }




    fun isOnline(context: Context): Boolean {

        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (connectivityManager != null) {

                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
            return false
        } catch (_: Exception) {
            return false
        } catch (_: java.lang.Exception) {
            return false
        }
    }


    fun formatDate(dateString: String): String {
        try {
            // Parse the input date
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsedDate = inputFormat.parse(dateString)

            // Check if parsedDate is not null (null check is necessary)
            parsedDate?.let {
                // Format the parsed date to the desired format "MM/DD/YY"
                val outputFormat = SimpleDateFormat("MM/dd/yy", Locale.US)
                return outputFormat.format(parsedDate)
            }

            // Return an empty string or any other appropriate default value if parsedDate is null
            return ""
        } catch (_: Exception) {
            return ""
        } catch (_: java.lang.Exception) {
            return ""
        }
    }


    fun launchPermissionSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }


    fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap? {

        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    fun bitmapToFile(bitmap: Bitmap, fileName: String): File? {
        try {
            // Create a temporary file in the cache directory
            val tempFile = File.createTempFile(fileName, ".jpg")

            // Compress the bitmap and write it to the file
            FileOutputStream(tempFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
            }

            // Return the temporary file
            return tempFile
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        }
    }


    fun convertImageViewToByteArray(imageView: ImageView): ByteArray? {
        try {
            val drawable = imageView.drawable

            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                return byteArrayOutputStream.toByteArray()
            }

            return null
        } catch (_: Exception) {
            return null
        } catch (_: java.lang.Exception) {
            return null
        }
    }


    fun Context.shareMultiplePDFs(files: List<File>, callBack: (Boolean) -> Unit) {
        try {
            if (files.isNotEmpty()) {
                val uris = ArrayList<Uri>()
                for (file in files) {
                    if (file.exists()) {

                        val uri = FileProvider.getUriForFile(
                            this,
                            "com.appsresort.resume.builder.cv.maker.online.fileprovider",
                            file
                        )
                        uris.add(uri)
                    } else {
                        Log.e("ShareMultiplePDFs", "File does not exist: ${file.absolutePath}")
                    }
                }


                if (uris.isNotEmpty()) {
                    val share = Intent(Intent.ACTION_SEND_MULTIPLE)
                    share.type = "application/pdf"
                    share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    share.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    startActivity(share)
                    callBack(true) // Callback with success
                    Log.d("ShareMultiplePDFs", "Sharing successful")
                    return
                } else {
                    Log.e("ShareMultiplePDFs", "No valid URIs to share")
                }
            } else {
                Log.e("ShareMultiplePDFs", "No files to share")
            }
        } catch (e: Exception) {
            Log.e("ShareMultiplePDFs", "Error sharing PDFs: ${e.message}", e)
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            Log.e("ShareMultiplePDFs", "Error sharing PDFs: ${e.message}", e)
            e.printStackTrace()
        }

        callBack(false) // Callback with failure
        Log.e("ShareMultiplePDFs", "Sharing failed")
    }


    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val filePath =
                "${context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/$fileName"

            try {
                val fileOutputStream = FileOutputStream(filePath)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()

                return Uri.fromFile(File(filePath))
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        } catch (_: Exception) {
            return null
        } catch (_: java.lang.Exception) {
            return null
        }
    }


    fun setupEditTextofAdaptors(
        editText: EditText,
        inputType: Int,
        imeAction: Int,
        nextField: EditText? = null
    ) {
        tryCatch {
            editText.inputType = inputType
            editText.imeOptions = imeAction
            editText.setOnEditorActionListener { _, actionId, event ->
                try {
                    if (actionId == EditorInfo.IME_ACTION_NEXT || (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                        // Handle the "Next" action here (e.g., move focus to the next field or hide keyboard)
                        if (nextField != null) {
                            nextField.requestFocus()
                        } else {
                            hideKeyboard_c(editText)
                        }
                        true
                    } else {
                        false
                    }
                } catch (_: Exception) {
                    false
                } catch (_: java.lang.Exception) {
                    false
                }

            }
        }

    }


    fun glideImageToFile(context: Context, imageUrl: String): File? {
        return try {
            // Load the image into a FutureTarget
            val futureTarget: FutureTarget<File> = Glide.with(context)
                .asFile()
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA) // Retrieve the image from cache if available
                .submit()

            // Get the file synchronously
            val imageFile: File = futureTarget.get()

            // Return the file
            imageFile
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }


    fun hideKeyboard_c(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    // Add InputFilter to capitalize the first letter
    val capitalizeFilter = InputFilter { source, _, _, dest, dstart, _ ->
        try {
            if (dstart == 0 && source.isNotEmpty() && !Character.isUpperCase(source[0])) {

                val capitalizedChar = Character.toUpperCase(source[0])
                val result = SpannableStringBuilder(capitalizedChar.toString())
                result.append(source.subSequence(1, source.length))

                result
            } else {
                null
            }
        } catch (_: IndexOutOfBoundsException) {
            null
        } catch (_: Exception) {
            null
        } catch (_: java.lang.Exception) {
            null
        }
    }

    fun applyCapitalizeFilter(editText: EditText) {
        editText.filters = arrayOf(capitalizeFilter)
    }

    fun isKeyboardVisible(context: Context): Boolean {
        val im =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val windowHeightMethod =
            InputMethodManager::class.java.getMethod("getInputMethodWindowVisibleHeight")
        val height = windowHeightMethod.invoke(im) as Int
        return height > 0
    }

    // Function to hide the keyboard
    fun hideKeyboardForcefully(context: Context) {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // Try to get the current focused view
            val view = (context as? Activity)?.currentFocus ?: View(context)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun validateInput(text: String): Boolean {
        var valid = false
        tryCatch {
            valid = text.matches(Regex("^[a-zA-Z\\s]*$"))
        }
        return valid
    }


    // Function to save integer value to SharedPreferences
    fun saveDataToSharedPreferences(
        context: Context,
        keyPrefix: String,
        stringValue: String,
        intValue: Int
    ) {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            val sharedPreferences =
                context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString(keyPrefix + "_string", stringValue)
                putInt(keyPrefix + "_int", intValue)
            }
        }
    }

    // Function to retrieve integer value from SharedPreferences
    fun getStringFromSharedPreferences(
        context: Context,
        keyPrefix: String,
        defaultValue: String
    ): String {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(keyPrefix + "_string", defaultValue) ?: defaultValue
    }

    fun getIntFromSharedPreferences(context: Context, keyPrefix: String, defaultValue: Int): Int {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(keyPrefix + "_int", defaultValue)
    }

    fun getStringSharedPreferences(context: Context, key: String, defaultValue: String): String {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun setStringSharedPreferences(context: Context, key: String, value: String) {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            val sharedPreferences =
                context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putString(key, value)
            }
        }
    }





    interface PreviewCvCallback {
        fun onPreviewCompleted(pdfUrl: String)
        fun onPreviewFailed(errorMessage: String)
    }

//    fun Fragment.previewCv(){}



    fun Fragment.previewCv(
        sharedViewModel: SharedViewModel,
        destinationId: Int
    ) {
        val callback = object : PreviewCvCallback {
            override fun onPreviewCompleted(pdfUrl: String) {
                // Handle successful preview completion
                // Here you can do whatever you want with the PDF URL
                Log.d("testing1", "Preview completed. PDF URL 123: $pdfUrl")
//                findNavController().navigate(R.id.fragmentPreviewApi)
//
//                if (first_blink == 1) {
//                activity?.let {
//                    if (it is MainActivity) {
//                        InterstitialHelper.showAndLoadInterstitial(
//                            it,
//                            it.getString(R.string.cv_generation_interstitial)
//                        ) {
//                            navigateToFragment(sharedNavVM = sharedNavigationViewModel, targetDestinationId = destinationId)
//                            Log.d("PreviewCvCallback", "Preview completed. PDF URL: $pdfUrl")
//
//                        }
//                    }
//                }
//                } else {
//                    Log.d("PreviewCvCallback", "in else Preview completed. PDF URL: $pdfUrl")
//
//                }


            }

            override fun onPreviewFailed(errorMessage: String) {
                // Handle preview failure
                Log.d("testing1", "Preview failed: $errorMessage")

//                previewFailed = true

            }
        }

        activity?.let {
//            sharedViewModel.previewCv( it, it, this)
        }

    }


   /* fun SharedViewModel.previewCv(
        requireContext: FragmentActivity,
        callback: PreviewCvCallback,
        fragment: Fragment
    ) {
        Log.i("MyNewTag123", " previewCv CALLED")

        Log.e("testing1", "preViewCV fun")
        var webSocketUrl = ""
        val name = cvModel.first_name + cvModel.last_name

//        if (cvModel.phone.isNotEmpty() && cvModel.cv_email.isNotEmpty() && name.isNotEmpty() &&
//            cvModel.cv_email.matches(
//                ViewUtils.emailPattern.toRegex()
//            ) && cvModel.first_name.matches(Regex("^[a-zA-Z\\s]*$")) && cvModel.phone.matches(
//                ViewUtils.phoneNumberPattern.toRegex()
//            )
//        ) {

        cancel_click = false


        // Call the show function
        show(
//            context = requireContext,
            activity = requireContext,
            title = "Cv Generating ...",
            showLoadingProgress = false,
            onCancelClicked = { cancelled ->
                cancel_click = cancelled
                if (cancelled) {

                    RetrofitClient.cancelCreateUserRequest() // cancels the coroutine + request
                    RetrofitClient.closeWebSocket()
                    cvDialogShowing = false

                    // Check the previous back stack entry
                    sharedNavigationViewModel.getPreviousFragmentDestination()
                        ?.let { backStackEntry ->

                            when (backStackEntry) {
                                R.id.coverLetterFragmentCv -> {
                                    // Navigate to a specific destination when coming from savedDraftFragment
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.coverLetterFragmentCv
                                    )
                                }

                                R.id.educationFragment -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.educationFragment
                                    ) // Replace with your destination
                                }

                                R.id.experienceFragment -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.experienceFragment
                                    ) // Replace with your destination
                                }

                                R.id.fragmentObjective -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.fragmentObjective
                                    ) // Replace with your destination
                                }

                                R.id.fragmentTemplates -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.fragmentTemplates
                                    ) // Replace with your destination
                                }

                                R.id.fiftyWeeklyPremiumFragment -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.fragmentTemplates
                                    ) // Replace with your destination
                                }

                                R.id.freeTrialPremiumFragment -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.fragmentTemplates
                                    ) // Replace with your destination
                                }

                                R.id.interestFragment -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.interestFragment
                                    ) // Replace with your destination
                                }

                                R.id.personalDetailsFragment -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.personalDetailsFragment
                                    ) // Replace with your destination
                                }

                                R.id.projectFragment -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.projectFragment
                                    ) // Replace with your destination
                                }

                                R.id.referenceFragment -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.referenceFragment
                                    ) // Replace with your destination
                                }

                                R.id.skillFragment -> {
                                    // Navigate to a specific destination when from where coming
                                    fragment.navigateToFragment(
                                        sharedNavVM = sharedNavigationViewModel,
                                        targetDestinationId = R.id.skillFragment
                                    ) // Replace with your destination
                                }

                                else -> {
                                }
                            }
                        }


                    // Handle cancel action
                    // In this case, cancelled will always be false since the cancel button returns false
                    // Do something when the cancel button is clicked
                    // For example, you can show a message indicating cancellation
                } else {
                    // Handle other actions if needed
                    // In this case, this block will be executed when the dialog is dismissed without clicking cancel
                    // Do something when the dialog is dismissed without clicking cancel
                }
            },
//            apiRequestCallback = {
            apiRequestCallback = { updateProgress ->

                first_blink = 0
                // Retrieve the integer value from SharedPreferences
                val stringValue = getStringFromSharedPreferences(requireContext, "user_id", " ")
                var intValue = getIntFromSharedPreferences(requireContext, "user_id", -1)

//                createUserJob = CoroutineScope(Dispatchers.IO).launch {
                val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
                }

                CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {

                    if (stringValue.isEmpty() || intValue == -1) {
                        Log.i("MyNewTag123", " previewCv CALLED 1")
                        // Generate a unique identifier
                        val uniqueId = UUID.randomUUID().toString()

                        RetrofitClient.createUser(requireContext,
                            uniqueId,
                            onSuccess = { it1 ->
                                launch(coroutineExceptionHandler) {
                                    intValue = it1.data.id
                                    uploadPhoto(intValue)
                                    Log.d("MyNewTag123", "initListener----: $it1")
                                    saveDataToSharedPreferences(
                                        requireContext,
                                        "user_id",
                                        uniqueId,
                                        it1.data.id
                                    )
                                }
                            }) {
                        }
                    } else {
                        Log.i("MyNewTag123", " previewCv CALLED 2")
                        uploadPhoto(intValue)
                    }

                    RetrofitClient.createPdfLink(
                        requireContext,
                        PdfGeneration(intValue, profileId, templateValue),
                        onSuccess = { it1 ->

                            launch(coroutineExceptionHandler) {
                                pdfUrl = RetrofitClient.BASE_URL_Media + it1.pdf_link
                                webSocketUrl = it1.websocket_url
                                Log.i("MyNewTag123", " previewCv success => ${it1.websocket_url}")
                                selectedimageasFile = null
                                selectedimageUri = null
                                RetrofitClient.connectToWebSocket(
                                    "ws://cv2.funsol.cloud${webSocketUrl}",
                                    onMessage = { cvUrl ->
                                        Log.i("MyNewTag123", "WebSocket onMessage $cvUrl")
                                        val coroutineExceptionHandlerTwo =
                                            CoroutineExceptionHandler { _, _ ->
                                                callback.onPreviewFailed("Error parsing")
                                            }
                                        CoroutineScope(Main + coroutineExceptionHandlerTwo).launch {
                                            if (RetrofitClient.webSocket == null) {
                                                return@launch // ignore if closed
                                            }
                                            progress =
                                                extractProgressFromMessage(cvUrl) // Parse the progress from the WebSocket message

                                            updateProgress(progress) // Update the UI with the received progress

                                            Log.e("MyNewTag123", "progress = $progress")
                                            if (progress == 100) { // When progress reaches 100%, trigger completion
                                                cvDialogShowing = false
                                                dialogFragment?.dismiss()
//                                            }
                                                tryCatch {
                                                    val existingDialog =
                                                        requireContext.supportFragmentManager.findFragmentByTag(
                                                            "ProgressDialogFragment"
                                                        )

                                                    if (existingDialog is ProgressDialogFragment) {
                                                        cvDialogShowing = false
                                                        Log.e(
                                                            "MyNewTag123",
                                                            "existingDialog called"
                                                        )
                                                        existingDialog.dismissAllowingStateLoss()
                                                    }
                                                }


                                                callback.onPreviewCompleted(pdfUrl)
                                            }

                                        }


//                            old original code
//                            Log.e("webSocketMessage", "message = $cvUrl")
//                            CoroutineScope(Main).launch {
//                                delay(4000)
//
//                                Log.d("TApreviewCvG", "previewCv: main starts")
//                                if (!cancel_click) {
//                                    //profileId = -1
//                                    first_blink++
//                                    Log.d("TApreviewCvG", "previewCv: main starts in if ")
//                                    callback.onPreviewCompleted(pdfUrl)
//                                } else {
//                                    callback.onPreviewFailed("Preview cancelled")
//                                }
//                            }

                                    },
                                    onOpen = { isSuccessfullyOpen ->

//                                        if (!isSuccessfullyOpen){
//                                            cvDialogShowing = false
//                                            dialogFragment?.dismiss()
//                                            tryCatch {
//                                                val existingDialog =
//                                                    requireContext.supportFragmentManager.findFragmentByTag(
//                                                        "ProgressDialogFragment"
//                                                    )
//
//                                                if (existingDialog is ProgressDialogFragment) {
//                                                    cvDialogShowing = false
//                                                    Log.e("MyNewTag123", "existingDialog called")
//                                                    existingDialog.dismissAllowingStateLoss()
//                                                }
//                                            }
//                                        }
                                        Log.i("MyNewTag123", "WebSocket onOpen $isSuccessfullyOpen")
//                                        fragment.showToastSafe("Something went wrong")
                                    },
                                    onFailure = { error ->
                                        Log.i("MyNewTag123", "WebSocket onFailure $error")
                                        launch(Main) {
                                            fragment.showToastSafe(error)
                                        }

                                    })
                            }


                        },
                        onError = { error ->
                            Log.i("MyNewTag123", " previewCv CALLED 3")
                            fragment.showToastSafe(
                                error
                            )

                            // Handle error here if needed
                        })
                }

            }
        )


    }*/


    fun Fragment.showToast(message: String) {
/*        context?.let { currentContext ->
            if (InterstitialHelper.isNetworkAvailable(currentContext)) {
                showToastSafe(message)

            } else {
                if (message == "Some Error Occurred.") {
                    showToastSafe(
                        getStringSafe(R.string.no_internet_connection)
                    )

                } else {
                    showToastSafe(message)
                }
            }
        }*/

    }

    fun Fragment.showToastFromIo(message: String) {
        showToastSafe(message)
    }

    fun Fragment.checkProfileCase(sharedViewModel: SharedViewModel): Boolean {
        return !(sharedViewModel.profileCase == "createProfile" || sharedViewModel.profileCase == "updateProfile")
    }

    fun SharedViewModel.uploadPhoto(user_id: Int) {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }
        // Execute the RetrofitClient call
     /*   selectedimageasFile?.let { file ->
            CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
                RetrofitClient.uploadProfileImage(
                    "$user_id".toRequestBody(),
                    file,
                    onSuccess = { it1 ->
                        launch(coroutineExceptionHandler) {
                            cvModel.apply {
                                this?.image = RetrofitClient.BASE_URL_Media + it1.image_url
                            }
                            updateProfileModdel.apply {
                                this?.image = RetrofitClient.BASE_URL_Media + it1.image_url

                            }
                        }
                    }) {
                }
            }
        }*/

        cvModel.apply {
            this.user = user_id
        }
    }

    /**
     * Function to extract progress from WebSocket message
     */
    private fun extractProgressFromMessage(message: String): Int {
        return message.toIntOrNull()
            ?: 0 // Convert message to Int, default to 0 if conversion fails
    }


 /*   fun SharedViewModel.show(
//        context: Context,
        activity: FragmentActivity,
        title: String,
        showLoadingProgress: Boolean,
        onCancelClicked: (cancelled: Boolean) -> Unit,
//        apiRequestCallback: () -> Unit,
        apiRequestCallback: (progressCallback: (Int) -> Unit) -> Unit
    ) {
        tryCatch {
            if (activity.isFinishing || activity.isDestroyed) {
                return@tryCatch
            }

            val existingDialog =
                activity.supportFragmentManager.findFragmentByTag("ProgressDialogFragment")

            if (existingDialog != null && existingDialog is ProgressDialogFragment) {
                activity.supportFragmentManager.beginTransaction().remove(existingDialog)
                    .commitAllowingStateLoss()
            }

            dialogFragment = ProgressDialogFragment(
                title,
                showLoadingProgress,
                onCancelClicked,
                apiRequestCallback
            )
            cvDialogShowing = true
            dialogFragment?.show(activity.supportFragmentManager, "ProgressDialogFragment")
        }
    }*/


    //old original code
//    fun SharedViewModel.show(
//        context: Context,
//        title: String,
//        showLoadingProgress: Boolean,
//        onCancelClicked: (cancelled: Boolean) -> Unit,
////        apiRequestCallback: () -> Unit,
//        apiRequestCallback: (progressCallback: (Int) -> Unit) -> Unit
//    ) {
//        Log.e("generatingDialog", "called from view Utils")
//        val dialog = Dialog(context)
//        val binding = ProgressDialogLayoutBinding.inflate(LayoutInflater.from(context))
//
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setContentView(binding.root)
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        cvDialogShowing = true
//        val layoutParams = WindowManager.LayoutParams()
//        layoutParams.copyFrom(dialog.window?.attributes)
//        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
//        dialog.window?.attributes = layoutParams
//
////        binding.creationNative.admobNativeContainerMain.removeAllViews()
////        binding.creationNative.nativeContainerMain.isVisible = true
////        binding.creationNative.loadingAd.isVisible = true
//
//
////        var height = 110
////        when (RemoteConfig.creation_native) {
////            0 -> {
////                binding.creationNative.nativeContainerMain.isVisible = false
////            }
////
////            1 -> {
////                height = 271
////                val params = binding.creationNative.nativeContainerMain.layoutParams
////                params.height =
////                    context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._100sdp)
////                binding.creationNative.nativeContainerMain.layoutParams = params
////
////            }
////
////            2 -> {
////                height = 400
////                val params = binding.creationNative.nativeContainerMain.layoutParams
////                params.height =
////                    context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._240sdp)
////                binding.creationNative.nativeContainerMain.layoutParams = params
////            }
////        }
//
////        if (InterstitialHelper.isNetworkAvailable(context)) {
////
////            with(binding) {
////                NativeHelper(context).loadAdsWithConfiguration(
////                    Constants.CREATION_AD,
////                    creationNative.nativeContainerMain,
////                    creationNative.admobNativeContainerMain,
////                    height,
////                    context.getString(R.string.native_inner),
////                    null
////                ) { nativeAd ->
////
////                }
////            }
////        } else {
////
////            binding.creationNative.nativeContainerMain.isVisible = false
////
////        }
//
//
//        if (isNetworkAvailable(context)) {
//
//            with(binding) {
//                nativeAd(context,binding.shimmerSmall, binding.shimmerLarge,
//                    binding.nativeConstraint,
//                    binding.nativeAdFrameSmall, binding.nativeAdFrameLarge,
//                    binding.nativeAdCardSmall, binding.nativeAdCardLarge)
//            }
//        } else {
//
////            binding.creationNative.nativeContainerMain.isVisible = false
//            hideAll(binding.nativeConstraint,
//                binding.nativeAdFrameSmall,
//                binding.nativeAdFrameLarge,
//                binding.nativeAdCardSmall,
//                binding.nativeAdCardLarge)
//
//        }
//
//
//        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
//        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)
//        val titleTextView = dialog.findViewById<TextView>(R.id.titleTextView)
//        val loadingProgressBar = dialog.findViewById<ProgressBar>(R.id.progressLoadingBar)
//        val loadingText = dialog.findViewById<TextView>(R.id.progressTextView)
//
//        if(showLoadingProgress){
//            progressBar.visibility = View.GONE
//            loadingProgressBar.visibility = View.VISIBLE
//            loadingText.visibility = View.VISIBLE
//
//        } else {
//            progressBar.visibility = View.VISIBLE
//            loadingProgressBar.visibility = View.GONE
//            loadingText.visibility = View.GONE
//        }
//
//
//        dialog.setCancelable(false)
//        globalDialog = dialog
//        // Set title
//        titleTextView.text = title
//        loadingProgressBar.isIndeterminate = false // Show progress in steps
//
//
//        // Set click listener for cancel button
//        cancelButton.setOnClickListener {
//            // Stop the animation and reset it to the beginning
//            // lottieAnimationView.cancelAnimation()
//            onCancelClicked.invoke(true) // Invoke callback indicating cancel button was clicked
//            cvDialogShowing = false
//            dialog.dismiss()
//        }
//
//        dialog.setOnDismissListener {
//            cvDialogShowing = false
//        }
//
//        // Show the dialog
//        dialog.show()
//
//        // Perform API request and update progress using Retrofit only if cancel button is not clicked
////        apiRequestCallback.invoke()
//        apiRequestCallback.invoke { progress ->
//            CoroutineScope(Dispatchers.Main).launch {
//                loadingProgressBar.progress = progress
//                loadingText.text = "$progress%" // Show percentage inside TextView
//
////                // Change ProgressBar color based on progress
////                val color = when {
////                    progress < 30 -> Color.RED
////                    progress < 70 -> Color.YELLOW
////                    else -> Color.GREEN
////                }
////
////                loadingProgressBar.progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
////                loadingProgressBar.progress = progress
//            }
//        }
//    }

    fun SharedViewModel.setData(position: Int) {

        tryCatch {
            if (profileList.isNotEmpty()) {
                cvModel = CvModelRequest()
                cvModel.apply {
                    id =
                        profileList[position].profileModelItem.id
                    additional_info =
                        profileList[position].profileModelItem.additional_info
                    image =
                        profileList[position].profileModelItem.image
                    address =
                        profileList[position].profileModelItem.address
                    cover_letter =
                        profileList[position].profileModelItem.cover_letter
                    cv_email =
                        profileList[position].profileModelItem.cv_email
                    date_of_birth =
                        profileList[position].profileModelItem.date_of_birth
                    designation =
                        profileList[position].profileModelItem.designation
                    driving_license =
                        profileList[position].profileModelItem.driving_license
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
                    first_name =
                        profileList[position].profileModelItem.first_name + " "
                    gender =
                        profileList[position].profileModelItem.gender
                    last_name =
                        profileList[position].profileModelItem.last_name
                    marital_status =
                        profileList[position].profileModelItem.marital_status
                    objective =
                        profileList[position].profileModelItem.objective
                    phone =
                        profileList[position].profileModelItem.phone
                    user =
                        profileList[position].profileModelItem.user
                    website =
                        profileList[position].profileModelItem.website

                }
                profileId = profileList[position].profileModelItem.id

            }
        }

    }


   /* @RequiresApi(Build.VERSION_CODES.O)
    fun mapSourceToTarget(source: PdfJsonModel): CvModelRequest {
        Log.d("DEBUG", "Received source data: $source")
        val sourceData = source.data[0]
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val fullName = sourceData.personal_information.name.split(" ")
        val firstName = if (fullName.isNotEmpty()) fullName[0] else ""
        val lastName =
            if (fullName.size > 1) fullName.subList(1, fullName.size).joinToString(" ") else ""

        val educations = sourceData.education.map { edu ->
            val edudates = parseDuration(edu.year_of_graduation.toString())
            Log.d("DEBUG", "Education dates: $edudates")

            Education(
                name = edu.degree,
                school = edu.institution,
                start_at = edudates?.first?.format(formatter),
                end_at = edudates?.second?.format(formatter),
                location = ""
            )
        }.toMutableList()

        val experiences = sourceData.work_experience.map { work ->
            val expdates = work.duration?.let { parseDuration(it) }
            Log.d("DEBUG", "Work experience dates: $expdates")

            work.company?.let {
                Experience(
                    designation = work.position,
                    company_name = it,
                    start_at = expdates?.first?.format(formatter),
                    end_at = expdates?.second?.format(formatter),
                    description = work.responsibilities,
                    location = ""
                )
            }
        }.toMutableList()

        val tools = sourceData.skills.technical_skills.map { skill ->
            Tool(0, name = skill)
        }.toMutableList()

        val industryKnowledge = sourceData.skills.soft_skills.map { skill ->
            IndustryKnowledge(0, name = skill)
        }.toMutableList()

        val references = sourceData.references.map { ref ->
            Reference(
                name = ref.toString(),
                designation = "",
                company_name = "",
                email = "",
                phone = ""
            )
        }.toMutableList()

        val achievementsAndAwards = sourceData.certifications.map { cert ->
            AchievementsAndAward(description = "", 0, name = cert.toString())
        }.toMutableList()

        val projects = sourceData.projects.map { proj ->
            Project(0, title = proj.title, description = "")
        }.toMutableList()

        return CvModelRequest(
            id = 0,
            additional_info = "",
            image = null,
            address = "",
            cover_letter = "",
            cv_email = sourceData.personal_information.contact_information.email,
            date_of_birth = null,
            designation = "",
            driving_license = "",
            educations = educations,
            experiences = experiences,
            other_skills = (tools.map {
                OtherSkill(
                    0,
                    it.name
                )
            } + industryKnowledge.map { OtherSkill(0, it.name) }).toMutableList(),
            references = references,
            projects = projects,
            interests = mutableListOf(
                Interest(name = ""),
                Interest(name = "")
            ),
            first_name = firstName,
            gender = "",
            last_name = lastName,
            marital_status = "",
            objective = "",
            phone = sourceData.personal_information.contact_information.phone,
            template = 4,
            user = 1,
            website = ""
        )
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseDuration(duration: String): Pair<LocalDate?, LocalDate?>? {
        val monthMap = mapOf(
            "january" to Month.JANUARY, "february" to Month.FEBRUARY, "march" to Month.MARCH,
            "april" to Month.APRIL, "may" to Month.MAY, "june" to Month.JUNE,
            "july" to Month.JULY, "august" to Month.AUGUST, "september" to Month.SEPTEMBER,
            "october" to Month.OCTOBER, "november" to Month.NOVEMBER, "december" to Month.DECEMBER
        )

        Log.d("DEBUG", "Parsing duration: $duration")

        if (duration.matches(Regex("\\d{4}"))) {
            val year = duration.toInt()
            val startDate = LocalDate.of(year, Month.JANUARY, 1)
            return startDate to null
        }

        val parts = duration.split(" - ")

        if (parts.size < 2) {
            Log.d("DEBUG", "Invalid duration format: $duration")
            return null
        }

        fun parseDate(part: String): LocalDate? {
            val subParts = part.split(" ")
            return when (subParts.size) {
                1 -> {
                    val year = subParts[0].toIntOrNull()
                    year?.let { LocalDate.of(it, Month.JANUARY, 1) }
                }

                2 -> {
                    val month = monthMap[subParts[0].lowercase(Locale.ROOT)]
                    val year = subParts[1].toIntOrNull()
                    if (month != null && year != null) {
                        LocalDate.of(year, month, 1)
                    } else {
                        null
                    }
                }

                else -> null
            }
        }

        val startDate = parseDate(parts[0])
        val endDate = if (parts[1].trim().equals("present", ignoreCase = true)) {
            LocalDate.now()
        } else {
            parseDate(parts[1])
        }

        return startDate to endDate
    }


    fun saveImageToInternalStorage(
        context: Context,
        imageUri: Uri?,
        fileName: String
    ) {
        imageUri?.let { currentUri ->
            tryCatch {
                val contentResolver: ContentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(currentUri)
                val file = File(context.filesDir, fileName)
                val outputStream = FileOutputStream(file)

                inputStream?.copyTo(outputStream)

                inputStream?.close()
                outputStream.close()
            }
        }

    }

    fun loadImageFromInternalStorage(context: Context, fileName: String): Bitmap? {
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                return bitmap
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun extractTimestamp(dateString: String): Long {
        val pattern = "Updated : "
        return try {
            val timestampString = dateString.substringAfter(pattern)
            timestampString.toLong()
        } catch (e: NumberFormatException) {
            // Handle the exception, for now, return 0
            0L
        } catch (e: java.lang.Exception) {
            // Handle the exception, for now, return 0
            0L
        } catch (e: Exception) {
            // Handle the exception, for now, return 0
            0L
        }
    }

    fun getCurrentTime(currentTime: Long): String {
        val simpleDateFormat = SimpleDateFormat("M-dd-yyyy", Locale.getDefault())
        return simpleDateFormat.format(Date(currentTime))
    }


    fun Any?.debug() = Log.d("find", "$this")

  /*  fun getDayCount(): Int {
        return sharedPreferences.getInt(App_Open_day_count, 1)
    }

    fun setDayCount(day: Int) {
        sharedPreferences.edit().putInt(App_Open_day_count, day).apply()
    }

    fun getDate(): Long {
        return sharedPreferences.getLong(App_Open_date, System.currentTimeMillis())
    }*/

    fun setRunCount(value: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            sharedPreferences.edit { putInt("run_count", value) }
        }
    }

    fun getRunCount(): Int {
        return sharedPreferences.getInt("run_count", 1)
    }

    fun incrementRunCount() {
        val newRunCount = getRunCount() + 1
        setRunCount(newRunCount)
    }


/*    fun dialogNativeAd(
        context: Context,
        shimmerSmall: NativeContainerBinding,
        shimmerLarge: NativeContainerBinding,
        nativeConstraint: ConstraintLayout,
        nativeAdFrameSmall: FrameLayout,
        nativeAdFrameLarge: FrameLayout,
        nativeAdCardSmall: MaterialCardView,
        nativeAdCardLarge: MaterialCardView,
    ) {
        if (isNetworkAvailable(context) && !BillingUtilsIAP.isPurchased) {
            if (NativeHelper.dialogNativeAd == null && !NativeHelper.dialogNativeLoading) {  // load a new ad
                loadNativeAdmob(
                    context,
                    shimmerSmall,
                    shimmerLarge,
                    nativeConstraint,
                    nativeAdFrameSmall,
                    nativeAdFrameLarge,
                    nativeAdCardSmall,
                    nativeAdCardLarge
                )

            } else { //populate/show the preloaded ad
                Log.e("tempNativeAd", "abut to show native ad")
                when (RemoteConfig.inner_native) {
                    0 -> {
                        hideAll(
                            nativeConstraint,
                            nativeAdFrameSmall,
                            nativeAdFrameLarge,
                            nativeAdCardSmall,
                            nativeAdCardLarge
                        )
                    }

                    1 -> {

                        showSmallHideLarge(
                            shimmerSmall,
                            nativeConstraint,
                            nativeAdFrameSmall,
                            nativeAdFrameLarge,
                            nativeAdCardSmall,
                            nativeAdCardLarge
                        )

                    }

                    2 -> {
                        showLargeHideSmall(
                            shimmerLarge,
                            nativeConstraint,
                            nativeAdFrameSmall,
                            nativeAdFrameLarge,
                            nativeAdCardSmall,
                            nativeAdCardLarge
                        )
                    }

                    else -> {
                        hideAll(
                            nativeConstraint,
                            nativeAdFrameSmall,
                            nativeAdFrameLarge,
                            nativeAdCardSmall,
                            nativeAdCardLarge
                        )
                    }
                }
//                showNativeAdmob(context,
//                    shimmerSmall,
//                    shimmerLarge,
//                    nativeConstraint,
//                    nativeAdFrameSmall,
//                    nativeAdFrameLarge,
//                    nativeAdCardSmall,
//                    nativeAdCardLarge)

                startCheckingConditionInner(
                    context,
                    shimmerSmall,
                    shimmerLarge,
                    nativeConstraint,
                    nativeAdFrameSmall,
                    nativeAdFrameLarge,
                    nativeAdCardSmall,
                    nativeAdCardLarge
                )
            }
        } else {
            hideAll(
                nativeConstraint,
                nativeAdFrameSmall,
                nativeAdFrameLarge,
                nativeAdCardSmall,
                nativeAdCardLarge
            )
        }
//        } else {
//            hideAll(nativeConstraint,
//                nativeAdFrameSmall,
//                nativeAdFrameLarge,
//                nativeAdCardSmall,
//                nativeAdCardLarge)
//        }
    }*/



}