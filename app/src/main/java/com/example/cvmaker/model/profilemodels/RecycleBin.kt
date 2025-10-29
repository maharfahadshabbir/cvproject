package com.example.cvmaker.model.profilemodels

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "recycle_bin_model_requests")
data class RecycleBin(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var pdfUrl:String,
    val generationId:Int,
    val profileId:Int,
    var templateId:Int,
    var cVorCoverLetter:String,
    val timestamp: Long = System.currentTimeMillis()
)
