package com.example.cvmaker.cv

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cv_name_model_requests")
data class CvNameModel(
    @PrimaryKey()
    val id: Long = 0,
    val cvName: String,
    val generationId: Int
)
