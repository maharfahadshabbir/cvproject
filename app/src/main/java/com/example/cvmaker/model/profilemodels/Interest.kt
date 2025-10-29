package com.example.cvmaker.model.profilemodels

import com.google.gson.annotations.SerializedName

data class Interest(
    @SerializedName("id") var id: Int= 0,
    @SerializedName("name") var name: String=""
)