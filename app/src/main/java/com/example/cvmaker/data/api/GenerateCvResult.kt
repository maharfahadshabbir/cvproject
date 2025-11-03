package com.example.cvmaker.data.api

sealed class GenerateCvResult {
    data class Single(val pdfUrl: String) : GenerateCvResult()
    data class Multiple(val files: List<Pair<String, String>>) : GenerateCvResult() // (template, url)
    data class Error(val message: String) : GenerateCvResult()
}