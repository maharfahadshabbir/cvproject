package com.example.cvmaker.model.profilemodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class OtherSkill(
    @SerializedName("id") var id: Int= 0,
    @SerializedName("name") var name: String="",
    @SerializedName("rating") var rating: Int= 1
)