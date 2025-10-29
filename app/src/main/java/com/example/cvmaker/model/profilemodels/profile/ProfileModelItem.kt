package com.example.cvmaker.model.profilemodels.profile

import com.example.cvmaker.model.profilemodels.Education
import com.example.cvmaker.model.profilemodels.Experience
import com.example.cvmaker.model.profilemodels.Interest
import com.example.cvmaker.model.profilemodels.OtherSkill
import com.example.cvmaker.model.profilemodels.Project
import com.example.cvmaker.model.profilemodels.Reference
import com.google.gson.annotations.SerializedName

data class ProfileModelItem(
    @SerializedName("achievements_and_awards") val achievements_and_awards: List<AchievementsAndAward>,
    @SerializedName("activities") val activities: List<Activities>,
    @SerializedName("additional_info") val additional_info: String,
    @SerializedName("default") val default: Boolean,
    @SerializedName("address") val address: String,
    @SerializedName("cover_letter") val cover_letter: String,
    @SerializedName("created_at") val created_at: Long,
    @SerializedName("status") val status: Boolean,
    @SerializedName("cv_email") val cv_email: String,
    @SerializedName("date_of_birth") val date_of_birth: String?,
    @SerializedName("designation") val designation: String,
    @SerializedName("driving_license") val driving_license: String,
    @SerializedName("educations") val educations: List<Education>,
    @SerializedName("experiences") val experiences: List<Experience>,
    @SerializedName("first_name") val first_name: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("id") val id: Int,
    @SerializedName("image") val image: String?,
    @SerializedName("industry_knowledge") val industry_knowledge: List<IndustryKnowledge>,
    @SerializedName("interests") val interests: List<Interest>,
    @SerializedName("languages") val languages: List<Language>,
    @SerializedName("last_name") val last_name: String,
    @SerializedName("marital_status") val marital_status: String,
    @SerializedName("objective") val objective: String,
    @SerializedName("other_skills") val other_skills: List<OtherSkill>,
    @SerializedName("phone") val phone: String,
    @SerializedName("projects") val projects: List<Project>,
    @SerializedName("publications") val publications: List<Publication>,
    @SerializedName("references") val references: List<Reference>,
    @SerializedName("social") val social: Social,
    @SerializedName("tools") val tools: List<Tool>,
    @SerializedName("updated_at") val updated_at: Long,
    @SerializedName("user") val user: Int,
    @SerializedName("website") val website: String
)
