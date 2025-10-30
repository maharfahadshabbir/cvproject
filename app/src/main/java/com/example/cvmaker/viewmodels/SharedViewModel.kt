package com.example.cvmaker.viewmodels

import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cvmaker.cv.CvModelRequest
import com.example.cvmaker.coverletter.CoverLetterResponse
import com.example.cvmaker.cv.CvType
import com.example.cvmaker.model.profilemodels.ProfileModelUI
import com.example.cvmaker.model.profilemodels.profile.UpdateProfileModdel
import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import com.example.cvmaker.model.workingmodels.PersonalDetailModel
import java.io.File
import java.io.InputStream

class SharedViewModel : ViewModel() {

    companion object{
        var selectedProfile: ProfileModelUI?  = null
    }


    //working
    var cvModelRequestDb = CvModelRequestDb()


    val personalDetailsList = mutableListOf<PersonalDetailModel>()




    //working//
    var popUp30percent: Boolean = false
    var thumbNail: String = ""
    var categoriesTemplateValue: Boolean = false

    var isFirstLaunch: Boolean = false
    var noprofile: String = ""
    var selectedProfileUserName: String = ""
    var filterApplied = false
    var error = ""
    var currentFilter = ""
    var CoverLetterResponse: CoverLetterResponse? = null
    var profileList: MutableList<ProfileModelUI> = mutableListOf()

    var cvTypeList: List<CvType> = emptyList()

    var profileCase = " "
    var nextFragment: String = " "
    var templateValue: Int = -1
    var profileCount: Int = 0
    var selectedDocumentUri: Uri? = null
    var templateString: String = ""
    var title:String = ""
    fun setTitle( string: String, string1: String){
         title = string
         templateString = string1

    }
    var setString: String = ""

    var globalDialog: Dialog? = null
    private var cvId: Long = -1 // Initialize with a default value
    private var cvIdforEdits: Long = -1 // Initialize with a default value
    var selectedimageasFile: File? = null
    var selectedimageUri: Uri? = null
    var imageasBitmap:Bitmap? =null

    var pdfStream: InputStream? =null


    //Api related
    var editcv: Int = -1
    var profileId = -1
    var lastFragmentNamePreview: String = ""
    var lastFragmentNameCreation: String = ""
    var genration_id: Int = -1
    var user_id: Int = -1
    val cvModelList: MutableList<CvModelRequest> = mutableListOf()
    var cvModel: CvModelRequest = CvModelRequest()
    var updateProfileModdel: UpdateProfileModdel = UpdateProfileModdel()
    var pdfUrl = ""

    var cancel_click = false



    //

    var cvTypeList1: List<CvType> = emptyList()
    var cvTypeList2: List<CvType> = emptyList()
    var cvTypeList3: List<CvType> = emptyList()


    fun setList(position:Int,list:List<CvType>){

        when(position){
            0->{
                cvTypeList1 = list
            }
            1->{
                cvTypeList2 = list

            }
            2->{
                cvTypeList3 = list

            }
        }
    }



    private val _isNativeAdVisible = MutableLiveData<Boolean>(true)
    val isNativeAdVisible: LiveData<Boolean> get() = _isNativeAdVisible

    fun setNativeAdVisibility(isVisible: Boolean) {
        _isNativeAdVisible.value = isVisible
    }


    // MutableLiveData that holds a Boolean value
    private val _isFeatureEnabled = MutableLiveData<Boolean>()

    // Expose an immutable LiveData for observers
    val isFeatureEnabled: LiveData<Boolean> get() = _isFeatureEnabled

    // Function to update the value
    fun setFeatureEnabled(enabled: Boolean) {
        _isFeatureEnabled.value = enabled
    }
}