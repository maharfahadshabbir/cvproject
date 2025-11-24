package com.example.cvmaker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cvmaker.data.repositry.MyRepositry
import com.example.cvmaker.typeConvertor.CvModelRequestEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewProfileViewModel @Inject constructor(
    private val repo: MyRepositry
) : ViewModel() {

    private val _profiles = MutableStateFlow<List<CvModelRequestEntity>>(emptyList())
    val profiles: StateFlow<List<CvModelRequestEntity>> = _profiles

    fun loadProfiles() {
        viewModelScope.launch {
            // load all saved CVs from DB
            val list = repo.getCvModelRequest().filterNotNull()
            _profiles.value = list
        }
    }

    fun deleteProfileById(id: Long?) {
        if (id == null || id <= 0L) return
        viewModelScope.launch {
            repo.deleteCvModelRequestById(id)
            loadProfiles() // Refresh list
        }
    }
}
