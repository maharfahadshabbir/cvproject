package com.example.cvmaker.coverletter

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CoverLetter(
    @SerializedName("job_description") val job_description: String,
    @SerializedName("user_id") val user_id: Int,
    @SerializedName("template") val template: Int,
    @SerializedName("user_profile_id") val user_profile_id: Int
)