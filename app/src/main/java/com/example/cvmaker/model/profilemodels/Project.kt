package com.example.cvmaker.model.profilemodels

import com.google.gson.annotations.SerializedName

data class Project(
    @SerializedName("id") var id: Int= 0,
    @SerializedName("description") var description: String="",
    @SerializedName("title") var title: String=""
)