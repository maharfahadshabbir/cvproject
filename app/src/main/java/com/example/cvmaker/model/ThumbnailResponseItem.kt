package com.example.cvmaker.model

data class ThumbnailResponseItem(
    val base: String,
    val id: Int,
    val is_active: Boolean,
    val is_premium: Boolean,
    val thumbnail: String,
    val title: String
)
