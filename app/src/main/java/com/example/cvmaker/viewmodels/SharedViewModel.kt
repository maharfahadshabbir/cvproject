package com.example.cvmaker.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {

    private val _cvData = MutableLiveData<CvModelRequestDb>()
    val cvData: LiveData<CvModelRequestDb> get() = _cvData

    fun setCvData(data: CvModelRequestDb) {
        _cvData.value = data
    }


    var editingProfileId: Long? = null  // Add this field!

    //working
    var cvModelRequestDb = CvModelRequestDb()

    var isEditingProfile: Boolean = false
    var editingProfileEntityId: Long? = null

    fun startNewProfile() {
        isEditingProfile = false
        editingProfileEntityId = null
        cvModelRequestDb = CvModelRequestDb()  // fresh data
    }

    fun startEditProfile(entityId: Long, data: CvModelRequestDb) {
        isEditingProfile = true
        editingProfileEntityId = entityId
        cvModelRequestDb = data
    }


    var noprofile: String = ""

    var error = ""


    var profileCase = " "

    var title: String = ""

    var selectedimageasFile: File? = null
    var selectedimageUri: Uri? = null

    var cvModel: CvModelRequestDb = CvModelRequestDb()
}
