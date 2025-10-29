package com.example.cvmaker.model.profilemodels

data class RecycleBinAdapterModel(
    var id:Int = 0,
    var pdfUrl:String,
    var generationId:Int,
    var isChecked: Boolean = false
    )
