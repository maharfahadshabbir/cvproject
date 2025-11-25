package com.example.cvmaker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cvmaker.typeConvertor.CvModelRequestEntity

@Dao
interface DatabaseDaos {
    //draft related queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCvModelRequest(cvModelRequest: CvModelRequestEntity)

    @Query("SELECT * FROM cv_model_requests")
    suspend fun getCvModelRequest(): List<CvModelRequestEntity?>



    @Update
    suspend fun updateCvModelRequest(entity: CvModelRequestEntity)


    @Query("DELETE FROM cv_model_requests WHERE id = :id")
    suspend fun deleteCvModelRequestById(id: Long)

}