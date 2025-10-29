package com.example.cvmaker.coverletter

import android.app.Activity
import com.example.cvmaker.model.profilemodels.Education
import com.example.cvmaker.model.profilemodels.Experience
import com.example.cvmaker.model.profilemodels.Interest
import com.example.cvmaker.model.profilemodels.OtherSkill
import com.example.cvmaker.model.profilemodels.Project
import com.example.cvmaker.model.profilemodels.Reference
import com.example.cvmaker.model.profilemodels.profile.AchievementsAndAward
import com.example.cvmaker.model.profilemodels.profile.IndustryKnowledge
import com.example.cvmaker.model.profilemodels.profile.Language
import com.example.cvmaker.model.profilemodels.profile.Publication
import com.example.cvmaker.model.profilemodels.profile.Social
import com.example.cvmaker.model.profilemodels.profile.Tool

data class UserData(
    val achievements_and_awards: List<AchievementsAndAward>,
    val activities: List<Activity>,
    val additional_info: String,
    val address: String,
    val cover_letter: String,
    val cv_email: String,
    val date_of_birth: String,
    val designation: String,
    val driving_license: String,
    val educations: List<Education>,
    val experiences: List<Experience>,
    val first_name: String,
    val gender: String,
    val image: String,
    val industry_knowledge: List<IndustryKnowledge>,
    val interests: List<Interest>,
    val languages: List<Language>,
    val last_name: String,
    val marital_status: String,
    val objective: String,
    val other_skills: List<OtherSkill>,
    val phone: String,
    val projects: List<Project>,
    val publications: List<Publication>,
    val recipients_title: String,
    val recipient_name: String,
    val references: List<Reference>,
    val social: Social,
    val template: Int,
    val tools: List<Tool>,
    val user_profile_id: Int,
    val website: String
)