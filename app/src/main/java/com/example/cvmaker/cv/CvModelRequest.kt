package com.example.cvmaker.cv

import androidx.annotation.Keep
import com.example.cvmaker.model.profilemodels.Education
import com.example.cvmaker.model.profilemodels.Experience
import com.example.cvmaker.model.profilemodels.Interest
import com.example.cvmaker.model.profilemodels.OtherSkill
import com.example.cvmaker.model.profilemodels.Project
import com.example.cvmaker.model.profilemodels.Reference
import com.google.gson.annotations.SerializedName

@Keep
data class CvModelRequest(
    @SerializedName("id") var id: Int= 0,
    @SerializedName("additional_info") var additional_info: String="",
    @SerializedName("image") var image: String? = null,
    @SerializedName("address") var address: String="",
    @SerializedName("cover_letter") var cover_letter: String="",
    @SerializedName("cv_email") var cv_email: String="",
    @SerializedName("date_of_birth") var date_of_birth: String?=null,
    @SerializedName("designation") var designation: String="",
    @SerializedName("driving_license") var driving_license: String="",
    @SerializedName("educations") var educations: MutableList<Education> = mutableListOf(),
    @SerializedName("experiences") var experiences: MutableList<Experience?> = mutableListOf(),
    @SerializedName("other_skills") var other_skills: MutableList<OtherSkill> = mutableListOf(),
    @SerializedName("references") var references: MutableList<Reference> = mutableListOf(),
    @SerializedName("projects") var projects: MutableList<Project> = mutableListOf(),
    @SerializedName("interests") var interests: MutableList<Interest> = mutableListOf(),
    @SerializedName("first_name") var first_name: String="",
    @SerializedName("gender") var gender: String="",
    @SerializedName("last_name") var last_name: String="",
    @SerializedName("marital_status") var marital_status: String="",
    @SerializedName("objective") var objective: String="",
    @SerializedName("phone") var phone: String="",
    @SerializedName("template") var template: Int=4,
    @SerializedName("user") var user: Int = 1,
    @SerializedName("website") var website: String = ""



)