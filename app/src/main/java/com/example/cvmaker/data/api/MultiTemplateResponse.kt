package com.example.cvmaker.data.api

data class MultiTemplateResponse(
    val success: Boolean,
    val generated_files: List<GeneratedFile> = emptyList()
)
