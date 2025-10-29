package com.example.cvmaker.model.profilemodels.profile

import com.example.cvmaker.model.profilemodels.Education
import com.example.cvmaker.model.profilemodels.Experience
import com.example.cvmaker.model.profilemodels.Interest
import com.example.cvmaker.model.profilemodels.OtherSkill
import com.example.cvmaker.model.profilemodels.Project
import com.example.cvmaker.model.profilemodels.Reference


data class UpdateProfileModdel(
    var achievements_and_awards: MutableList<AchievementsAndAward> = mutableListOf(),
    var activities: MutableList<Activities> = mutableListOf(),
    var additional_info: String="",
    var address: String="",
    var cover_letter: String="",
    var cv_email: String="",
    var date_of_birth: String? = null,
    var designation: String="",
    var driving_license: String="",
    var educations: MutableList<Education> = mutableListOf(),
    var experiences: MutableList<Experience?> = mutableListOf(),
    var first_name: String="",
    var gender: String="",
    var id: Int = 0,
    var image: String?= null,
    var industry_knowledge: MutableList<IndustryKnowledge> = mutableListOf(),
    var interests: MutableList<Interest> = mutableListOf(),
    var languages: MutableList<Language> = mutableListOf(),
    var last_name: String="",
    var marital_status: String="",
    var objective: String="",
    var other_skills: MutableList<OtherSkill> = mutableListOf(),
    var phone: String="",
    var projects: MutableList<Project> = mutableListOf(),
    var publications: MutableList<Publication> = mutableListOf(),
    var references: MutableList<Reference> = mutableListOf(),
    var social: Social? = null,
    var tools: MutableList<Tool> = mutableListOf(),
    var user: Int = 1,
    var website: String="",
    var template: Int = 4,
    var user_profile_id: Int = 1,

    )
