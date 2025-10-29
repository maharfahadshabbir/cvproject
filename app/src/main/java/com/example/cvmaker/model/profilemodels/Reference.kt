package com.example.cvmaker.model.profilemodels

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Reference(
    @SerializedName("id")val id: String = UUID.randomUUID().toString(),
    @SerializedName("company_name") var company_name: String="",
    @SerializedName("designation") var designation: String="",
    @SerializedName("email") var email: String="",
    @SerializedName("name") var name: String="",
    @SerializedName("phone") var phone: String=""
)