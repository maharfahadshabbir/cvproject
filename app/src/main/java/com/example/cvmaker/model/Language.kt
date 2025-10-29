package com.example.cvmaker.model

data class Language(
    val code: String,
    val languageName: String,
    val subtitle: String,
    val flagResId: Int,
    var isSelected: Boolean = false
)
