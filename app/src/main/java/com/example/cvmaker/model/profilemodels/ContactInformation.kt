package com.example.cvmaker.model.profilemodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ContactInformation(
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String
)