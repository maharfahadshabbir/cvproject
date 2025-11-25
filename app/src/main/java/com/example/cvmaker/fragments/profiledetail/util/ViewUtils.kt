package com.example.cvmaker.fragments.profiledetail.util

import android.content.Context
import android.content.SharedPreferences
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.cvmaker.utils.tryCatch
import com.example.cvmaker.viewmodels.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

object ViewUtils {
    private lateinit var sharedPreferences: SharedPreferences

    var error = ""

    val emailPattern = Pattern.compile(
        "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
    )

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



    fun validateInput(text: String): Boolean {
        var valid = false
        tryCatch {
            valid = text.matches(Regex("^[a-zA-Z\\s]*$"))
        }
        return valid
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




    fun Fragment.checkProfileCase(sharedViewModel: SharedViewModel): Boolean {
        return !(sharedViewModel.profileCase == "createProfile" || sharedViewModel.profileCase == "updateProfile")
    }


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

}