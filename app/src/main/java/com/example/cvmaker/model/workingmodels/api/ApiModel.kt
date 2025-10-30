package com.example.cvmaker.model.workingmodels.api

data class ApiModel(
    val achievements: List<String>,
    val certifications: List<Certification>,
    val educations: List<Education>,
    val experiences: List<Experience>,
    val languages: List<String>,
    val personal_info: PersonalInfo,
    val projects: List<Project>,
    val skills: List<String>,
    val template_name: String,
    val template_type: String
)