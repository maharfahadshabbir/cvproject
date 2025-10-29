package com.example.cvmaker.model.profilemodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ReferenceX(
    @SerializedName("company") val company: String?,
    @SerializedName("contact_information") val contact_information: ContactInformation,
    @SerializedName("name") val name: String?,
    @SerializedName("position") val position: String?
)