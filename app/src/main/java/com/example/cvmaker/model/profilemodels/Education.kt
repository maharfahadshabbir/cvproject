package com.example.cvmaker.model.profilemodels

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class Education(
    @SerializedName("end_at") var end_at: String? = null,
    @SerializedName("id") var id: Int= 0,
    @SerializedName("location") var location: String="",
    @SerializedName("name") var name: String="",
    @SerializedName("school") var school: String="",
    @SerializedName("start_at") var start_at: String ? = null,
    @SerializedName("present") var present: Boolean = false
)
