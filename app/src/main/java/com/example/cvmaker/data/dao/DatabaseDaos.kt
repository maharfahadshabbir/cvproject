package com.example.cvmaker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cvmaker.model.ThumbNailsFavoriteModel
import com.example.cvmaker.model.profilemodels.FavoriteModel
import com.example.cvmaker.model.profilemodels.RecycleBin
import com.example.cvmaker.model.profilemodels.profile.ProfileDb
import com.example.cvmaker.typeConvertor.CvModelRequestEntity

@Dao
interface DatabaseDaos {
    //draft related queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCvModelRequest(cvModelRequest: CvModelRequestEntity)

    @Query("SELECT * FROM cv_model_requests")
    suspend fun getCvModelRequest(): List<CvModelRequestEntity?>

    @Query("DELETE FROM cv_model_requests WHERE draftName = :name")
    suspend fun deleteCvModelRequest(name: String)

    @Query("SELECT EXISTS(SELECT 1 FROM cv_model_requests LIMIT 1)")
    suspend fun doesDraftExistDb(): Boolean

    //favorite related queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteRequest(favorite: FavoriteModel)

    @Query("SELECT * FROM favorite_model_requests ")
    suspend fun getFavoriteRequest(): List<FavoriteModel>?

    @Query("DELETE FROM favorite_model_requests Where generationId = :generationId")
    suspend fun deleteFavoriteRequest(generationId:Int)


    @Query("SELECT EXISTS(SELECT 1 FROM favorite_model_requests LIMIT 1)")
    suspend fun doesFavoriteExistDb(): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_model_requests WHERE generationId = :generationId)")
    suspend fun doesFavoriteExistDb(generationId: Int): Boolean

    // thumbnails favourite related work
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThumbnailsFavoriteRequest(favorite: ThumbNailsFavoriteModel)

    @Query("SELECT * FROM thumb_nial_favorite_model_requests ")
    suspend fun getThumbnailsFavoriteRequest(): List<ThumbNailsFavoriteModel>

    @Query("DELETE FROM thumb_nial_favorite_model_requests WHERE thumbNailId = :thumbnailsId AND categoryId = :categoryId")
    suspend fun deleteThumbnailsFavoriteRequest(thumbnailsId:Int,categoryId:Int)

    @Query("SELECT EXISTS(SELECT 1 FROM thumb_nial_favorite_model_requests WHERE categoryId = :categoryId AND thumbNailId = :thumbnailsId)")
    suspend fun doesThumbnailsFavoriteExistDb(thumbnailsId:Int,categoryId: Int):Boolean

    // recycle bin related work
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecycleBinRequest(cvModelRequest: RecycleBin)

    @Query("SELECT * FROM recycle_bin_model_requests")
    suspend fun getRecycleBinRequest(): List<RecycleBin>?

    @Query("DELETE FROM recycle_bin_model_requests")
    suspend fun deleteRecycleBinRequest()

    @Query("DELETE FROM recycle_bin_model_requests WHERE generationId = :generationId")
    suspend fun deleteById(generationId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM recycle_bin_model_requests LIMIT 1)")
    suspend fun doesRecycleBinExistDb(): Boolean

    @Query("DELETE FROM recycle_bin_model_requests WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    // profile related work
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileRequest(cvModelRequest: ProfileDb)

    @Query("SELECT * FROM profile_model_requests")
    suspend fun getProfileRequest(): List<ProfileDb>?

    @Query("DELETE FROM profile_model_requests WHERE profileId =:profileId")
    suspend fun deleteProfileRequest(profileId: Int)

}