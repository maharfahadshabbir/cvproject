package com.example.cvmaker.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cvmaker.data.api.CvMakerRepository
import com.example.cvmaker.data.api.GenerateCvResult
import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CvMakerViewModel @Inject constructor(
    private val repo: CvMakerRepository   // ✅ no default, Hilt provides this
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class SingleDownload(val url: String) : UiState()
        data class MultipleDownloads(val files: List<Pair<String, String>>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state
    fun generate(cv: CvModelRequestDb, templateName: String? = null) {
        try {
            val gson = Gson()
            val cvJson = gson.toJson(cv)
            Log.d("CV_GENERATE", "Generating CV with template: $templateName")
            Log.d("CV_GENERATE", "Full CV data: $cvJson")


        } catch (e: Exception) {
            Log.e("CV_GENERATE", "Error generating CV", e)
        }
    }

}
