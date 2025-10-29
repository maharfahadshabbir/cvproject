package com.example.cvmaker.model.profilemodels

import com.google.gson.annotations.SerializedName

data class Experience(
    @SerializedName("id") var id: Int= 0,
    @SerializedName("company_name") var company_name: String="",
    @SerializedName("description") var description: String="",
    @SerializedName("designation") var designation: String="",
    @SerializedName("end_at") var end_at: String? = null,
    @SerializedName("location") var location: String="",
    @SerializedName("start_at") var start_at: String? = null,
    @SerializedName("present") var present: Boolean = false
)