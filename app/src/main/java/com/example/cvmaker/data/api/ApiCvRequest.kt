package com.example.cvmaker.data.api


data class ApiCvRequest(
    val template_type: String? = "ats",   // "ats" per doc (you can change)
    val template_name: String? = "",      // "" or null => all templates; "ats1" etc => single
    val personal_info: PersonalInfo? = null,
    val educations: List<ApiEducation> = emptyList(),
    val experiences: List<ApiExperience> = emptyList(),
    val skills: List<String> = emptyList(),
    val projects: List<ApiProject> = emptyList(),
    val references: List<ApiReference> = emptyList(),
    val interests: List<String> = emptyList(),
    val certifications: List<ApiCertification> = emptyList(),
    val achievements: List<ApiAchievement> = emptyList(),
    val languages: List<ApiLanguage> = emptyList()
)

data class PersonalInfo(
    val name: String? = "",
    val email: String? = "",
    val phone: String? = "",
    val dob: String? = "",
    val address: String? = "",
    val alternate_phone: String? = "",
    val id_card_number: String? = "",
    val passport_number: String? = "",
    val nationality: String? = "",
    val gender: String? = "",
    val marital_status: String? = "",
    val aboutme: String? = "",
    // portfolio
    val github_link: String? = "",
    val dribbble_link: String? = "",
    val behance_link: String? = "",
    val website_name: String? = "",
    val website_link: String? = ""
)

data class ApiEducation(
    val degree: String? = "",        // we’ll map from your course
    val institution: String? = "",   // your institute
    val year: String? = ""           // we’ll combine start/end or just end
)

data class ApiExperience(
    val job_title: String? = "",     // your designation
    val company: String? = "",       // your companyName
    val start_date: String? = "",
    val end_date: String? = "",
    val description: String? = ""
)

data class ApiProject(
    val title: String? = "",
    val description: String? = ""    // we’ll append the link, if present
)

data class ApiReference(
    val name: String? = "",
    val relation: String? = "",      // we’ll use designation/company here
    val phone: String? = "",
    val email: String? = ""
)

data class ApiCertification(
    val course: String? = "",
    val institute: String? = "",
    val grade: String? = "",
    val startDate: String? = "",
    val endDate: String? = "",
    val isCurrentStudent: Boolean? = false
)

data class ApiAchievement(
    val title: String? = "",
    val organization: String? = "",
    val year: String? = "",
    val description: String? = ""
)

data class ApiLanguage(
    val languageName: String? = "",
    val level: String? = ""          // "Novice/Beginner/Proficient/Expert"
)
