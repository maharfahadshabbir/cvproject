package com.example.cvmaker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cvmaker.data.repositry.MyRepositry
import com.example.cvmaker.model.ThumbNailsFavoriteModel
import com.example.cvmaker.model.profilemodels.FavoriteModel
import com.example.cvmaker.model.profilemodels.RecycleBin
import com.example.cvmaker.model.profilemodels.profile.ProfileDb
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

    suspend fun deleteCvModelRequest(name: String){
        return  repository.deleteCvModelRequest(name)
    }

    suspend fun doesDraftExistDb():Boolean{
        return  repository.doesDraftExistDb()
    }



    // recycle bin related work
    suspend fun insertRecycleBinRequest(cvModelRequest: RecycleBin){
        repository.insertRecycleBinRequest(cvModelRequest)
    }

    suspend fun getRecycleBinRequest(): List<RecycleBin>?{
        return  repository.getRecycleBinRequest()
    }

    suspend fun deleteRecycleBinRequest(){
        return  repository.deleteRecycleBinRequest()
    }

    suspend fun deleteById(id:Long){
        return  repository.deleteById(id)
    }

    suspend fun doesRecycleBinExistDb():Boolean{
        return  repository.doesRecycleBinExistDb()
    }

    suspend fun deleteOlderThan(cutoff: Long){
        return  repository.deleteOlderThan(cutoff)
    }





    // favorite related work
    suspend fun insertFavoriteRequest(favorite: FavoriteModel){
        repository.insertFavoriteRequest(favorite)
    }

    suspend fun getFavoriteRequest(): List<FavoriteModel>? {
        return  repository.getFavoriteRequest()
    }

    suspend fun deleteFavoriteRequest(generationId:Int){
        return  repository.deleteFavoriteRequest(generationId)
    }
    suspend fun doesFavoriteExistDb():Boolean{
        return  repository.doesFavoriteExistDb()
    }

    suspend fun doesFavoriteExistDb(generationId: Int):Boolean{
        return  repository.doesFavoriteExistDb(generationId)
    }



    // Profile related work
    suspend fun insertProfileRequest(profileDb: ProfileDb){
        repository.insertProfileRequest(profileDb)
    }

    suspend fun getProfileRequest(): List<ProfileDb>? {
        return  repository.getProfileRequest()
    }

    suspend fun deleteProfileRequest(profileId: Int) {
        return  repository.deleteProfileRequest(profileId)
    }





    // thumbnails favourite related work
    suspend fun insertThumbnailsFavoriteRequest(favorite: ThumbNailsFavoriteModel){
        repository.insertThumbnailsFavoriteRequest(favorite)
    }

    suspend fun getThumbnailsFavoriteRequest(): List<ThumbNailsFavoriteModel>? {
        return  repository.getThumbnailsFavoriteRequest()
    }

    suspend fun deleteThumbnailsFavoriteRequest(thumbnailsId:Int,categoryId:Int){
        return  repository.deleteThumbnailsFavoriteRequest(thumbnailsId,categoryId)
    }

    suspend fun doesThumbnailsFavoriteExistDb(thumbnailsId:Int,categoryId: Int):Boolean{
        return  repository.doesThumbnailsFavoriteExistDb(thumbnailsId,categoryId)
    }





}