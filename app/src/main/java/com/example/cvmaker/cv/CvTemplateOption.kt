package com.example.cvmaker.cv

data class CvTemplateOption(
    val templateName: String,
    val imageRes: Int,
    var isSelected: Boolean = false
)
