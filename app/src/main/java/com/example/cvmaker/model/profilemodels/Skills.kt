package com.example.cvmaker.model.profilemodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Skills(
    @SerializedName("soft_skills") val soft_skills: List<String>,
    @SerializedName("technical_skills") val technical_skills: List<String>
)