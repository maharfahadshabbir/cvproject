package com.example.cvmaker.model.profilemodels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_model_requests")
data class FavoriteModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val favorite: Boolean,
    val generationId: Int,
    val pdfUrl: String

)
