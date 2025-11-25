package com.example.cvmaker.data.repositry

import com.example.cvmaker.data.dao.DatabaseDaos
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


    suspend fun updateCvModelRequest(entity: CvModelRequestEntity) {
        databaseDaos.updateCvModelRequest(entity)
    }

    suspend fun deleteCvModelRequestById(id: Long) {
        databaseDaos.deleteCvModelRequestById(id)
    }




}