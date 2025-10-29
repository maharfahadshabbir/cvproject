package com.example.cvmaker.coverletter

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CoverLetterResponse(
    @SerializedName("cover_letter") val cover_letter: String,
    @SerializedName("websocket_url") val websocket_url: String,
    @SerializedName("thumbnail_link") val thumbnail_link: String,
    @SerializedName("message") val message: String
)