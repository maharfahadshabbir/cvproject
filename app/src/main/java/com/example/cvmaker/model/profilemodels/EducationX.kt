package com.example.cvmaker.model.profilemodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EducationX(
    @SerializedName("degree") val degree: String,
    @SerializedName("field_of_study") val field_of_study: String,
    @SerializedName("institution") val institution: String,
    @SerializedName("year_of_graduation") val year_of_graduation: String?
)