package com.example.cvmaker.viewmodels

import com.example.cvmaker.data.api.GenerateCvResult



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cvmaker.data.api.CvMakerRepository
import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CvMakerViewModel  @Inject constructor(
    private val repo: CvMakerRepository = CvMakerRepository()
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
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val r = repo.generateCv(cv, templateName)) {
                is GenerateCvResult.Single -> _state.value = UiState.SingleDownload(r.pdfUrl)
                is GenerateCvResult.Multiple -> _state.value = UiState.MultipleDownloads(r.files)
                is GenerateCvResult.Error -> _state.value = UiState.Error(r.message)
            }
        }
    }
}
