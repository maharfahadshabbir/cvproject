package com.example.cvmaker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cvmaker.data.repositry.MyRepositry
import com.example.cvmaker.typeConvertor.CvModelRequestEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(private val repository: MyRepositry) : ViewModel() {

    // draft related work
    suspend fun insertCvModelRequest(cvModelRequest: CvModelRequestEntity){
        repository.insertCvModelRequest(cvModelRequest)
    }

    fun updateCvModelRequest(entity: CvModelRequestEntity) = viewModelScope.launch {
        repository.updateCvModelRequest(entity)
    }

    suspend fun getCvModelRequest(): List<CvModelRequestEntity?>{
        return  repository.getCvModelRequest()
    }

}