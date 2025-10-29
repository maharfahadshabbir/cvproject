package com.example.cvmaker.model.profilemodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ProjectX(
    @SerializedName("description") val description: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("technologies_used") val technologies_used: List<Any>,
    @SerializedName("title") val title: String
)