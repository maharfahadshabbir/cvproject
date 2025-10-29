package com.example.cvmaker.cv

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CvModelResponse(
    @SerializedName("websocket_url") var websocket_url : String,
    @SerializedName("pdf_link") var pdf_link : String,
    @SerializedName("thumbnail_link") var thumbnail_link:String,
    @SerializedName("data") var data: CvModelRequest

)
