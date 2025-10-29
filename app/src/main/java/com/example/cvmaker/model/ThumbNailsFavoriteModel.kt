package com.example.cvmaker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "thumb_nial_favorite_model_requests")
data class ThumbNailsFavoriteModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val favorite: Boolean,
    val categoryId: Int,
    val thumbNailId: Int
)
