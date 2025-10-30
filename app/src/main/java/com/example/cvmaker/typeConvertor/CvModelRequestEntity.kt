package com.example.cvmaker.typeConvertor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cv_model_requests")
data class CvModelRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val json: String, // JSON representation of CvModelRequest
    val draftName: String,
    val profileOrDraft: Boolean,
    val updateDate: String,
    val creationDate: String,
    var imageName: String? = null,
    val userId: String // 👈 New field for unique user


)


