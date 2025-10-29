package com.example.cvmaker.data.repositry

import com.example.cvmaker.data.dao.DatabaseDaos
import com.example.cvmaker.model.ThumbNailsFavoriteModel
import com.example.cvmaker.model.profilemodels.FavoriteModel
import com.example.cvmaker.model.profilemodels.RecycleBin
import com.example.cvmaker.model.profilemodels.profile.ProfileDb
import com.example.cvmaker.typeConvertor.CvModelRequestEntity

import javax.inject.Inject

class MyRepositry @Inject constructor(private val databaseDaos: DatabaseDaos) {



    // draft related work
    suspend fun insertCvModelRequest(cvModelRequest: CvModelRequestEntity){
        databaseDaos.insertCvModelRequest(cvModelRequest)
    }

    suspend fun getCvModelRequest(): List<CvModelRequestEntity?>{
        return  databaseDaos.getCvModelRequest()
    }

    suspend fun deleteCvModelRequest(name: String){
        return  databaseDaos.deleteCvModelRequest(name)
    }
    suspend fun doesDraftExistDb():Boolean{
        return  databaseDaos.doesDraftExistDb()
    }




    // recycle bin related work
    suspend fun insertRecycleBinRequest(cvModelRequest: RecycleBin){
        databaseDaos.insertRecycleBinRequest(cvModelRequest)
    }

    suspend fun getRecycleBinRequest(): List<RecycleBin>?{
        return  databaseDaos.getRecycleBinRequest()
    }

    suspend fun deleteRecycleBinRequest(){
        return  databaseDaos.deleteRecycleBinRequest()
    }

    suspend fun deleteById(id:Long){
        return  databaseDaos.deleteById(id)
    }

    suspend fun doesRecycleBinExistDb():Boolean{
        return  databaseDaos.doesRecycleBinExistDb()
    }

    suspend fun deleteOlderThan(cutoff: Long){
        return  databaseDaos.deleteOlderThan(cutoff)
    }

    // draft related work
    suspend fun insertFavoriteRequest(favorite: FavoriteModel){
        databaseDaos.insertFavoriteRequest(favorite)
    }

    suspend fun getFavoriteRequest(): List<FavoriteModel>? {
        return  databaseDaos.getFavoriteRequest()
    }

    suspend fun deleteFavoriteRequest(generationId: Int) {
        return  databaseDaos.deleteFavoriteRequest(generationId)
    }
    suspend fun doesFavoriteExistDb():Boolean{
        return  databaseDaos.doesFavoriteExistDb()
    }

    suspend fun doesFavoriteExistDb(generationId: Int):Boolean{
        return  databaseDaos.doesFavoriteExistDb(generationId)
    }



    // Profile related work
    suspend fun insertProfileRequest(profileDb: ProfileDb){
        databaseDaos.insertProfileRequest(profileDb)
    }

    suspend fun getProfileRequest(): List<ProfileDb>? {
        return  databaseDaos.getProfileRequest()
    }

    suspend fun deleteProfileRequest(profileId: Int) {
        return  databaseDaos.deleteProfileRequest(profileId)
    }




    // thumbnails favourite related work
    suspend fun insertThumbnailsFavoriteRequest(favorite: ThumbNailsFavoriteModel){
        databaseDaos.insertThumbnailsFavoriteRequest(favorite)
    }

    suspend fun getThumbnailsFavoriteRequest(): List<ThumbNailsFavoriteModel>? {
        return  databaseDaos.getThumbnailsFavoriteRequest()
    }

    suspend fun deleteThumbnailsFavoriteRequest(thumbnailsId:Int,categoryId:Int){
        return  databaseDaos.deleteThumbnailsFavoriteRequest(thumbnailsId,categoryId)
    }

    suspend fun doesThumbnailsFavoriteExistDb(thumbnailsId:Int,categoryId: Int):Boolean{
        return  databaseDaos.doesThumbnailsFavoriteExistDb(thumbnailsId,categoryId)
    }


}