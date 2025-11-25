package com.example.cvmaker.cv

import com.example.cvmaker.model.workingmodels.CvModelRequestDb

data class CvApiRequest(
    val template_type: String = "ats",
    val template_name: String? = null,
    val personal_info: PersonalInfo,
    val educations: List<Education> = emptyList(),
    val experiences: List<Experience> = emptyList(),
    val skills: List<String> = emptyList(),
    val projects: List<Project> = emptyList(),
    val references: List<Reference> = emptyList(),
    val interests: List<String> = emptyList(),
    val certifications: List<Certification> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val languages: List<Language> = emptyList()
)

data class PersonalInfo(
    val name: String,
    val email: String,
    val phone: String = "",
    val dob: String = "",
    val address: String = "",
    val alternate_phone: String = "",
    val id_card_number: String = "",
    val passport_number: String = "",
    val nationality: String = "",
    val gender: String = "",
    val marital_status: String = "",
    val aboutme: String = "",
    val github_link: String = "",
    val dribbble_link: String = "",
    val behance_link: String = "",
    val website_name: String = "",
    val website_link: String = ""
)

data class Education(
    val degree: String,
    val institution: String,
    val year: String
)

data class Experience(
    val job_title: String,
    val company: String,
    val start_date: String,
    val end_date: String,
    val description: String
)

data class Project(
    val title: String,
    val description: String
)

data class Reference(
    val name: String,
    val contact: String,
    val relationship: String
)

data class Certification(
    val title: String,
    val issuer: String,
    val date: String
)

data class Achievement(
    val title: String,
    val description: String,
    val organization: String,
    val year: String
)

data class Language(
    val name: String,
    val proficiency: String
)

fun CvModelRequestDb.toApiRequest(templateName: String? = null): CvApiRequest {
    return CvApiRequest(
        template_name = templateName,
        personal_info = PersonalInfo(
            name = this.personalDetails?.name ?: "",
            email = this.personalDetails?.email ?: "",
            phone = this.personalDetails?.phone ?: "",
            dob = this.personalDetails?.dateOfBirth ?: "",
            address = this.personalDetails?.address ?: "",
            id_card_number = this.personalDetails?.idCard ?: "",
            passport_number = this.personalDetails?.passport ?: "",
            nationality = this.personalDetails?.nationality ?: "",
            alternate_phone = this.personalDetails?.phone2 ?: "",
            gender = this.personalDetails?.gender ?: "",
            marital_status = this.personalDetails?.maritalStatus ?: "",
        /*    aboutme = this.personalDetails?.aboutMe ?: "",
            github_link = this.personalDetails?.githubLink ?: "",
            dribbble_link = this.personalDetails?.dribbbleLink ?: "",
            behance_link = this.personalDetails?.behanceLink ?: "",
            website_name = this.personalDetails?.websiteName ?: "",
            website_link = this.personalDetails?.websiteLink ?: ""*/
        ),
        educations = this.educationList.map {
            Education(
                degree = it.course ?: "",
                institution = it.institute ?: "",
                year = it.startDate ?: ""
            )
        },
        achievements = this.achievementList.map {
            Achievement(
                title = it.title ?: "",
                description = it.description ?: "",
                organization = it.organization ?: "",
                year = it.year ?: ""
            )
        },
        certifications = this.certificationList.map {
            Certification(
                title = it.course ?: "",
                issuer = it.institute ?: "",
                date = it.startDate ?: ""
            )
        },
        experiences = this.experienceList.map {
            Experience(
                job_title = it.designation ?: "",
                company = it.companyName ?: "",
                start_date = it.startDate ?: "",
                end_date = it.endDate ?: "",
                description = it.detail ?: ""
            )
        },
        skills = this.skillsList.map { it.skillName ?: "" } as List<String>,
        projects = this.projectList.map { Project(it.projectTitle ?: "", it.description ?: "") },
        references = this.referenceList.map { Reference(it.name ?: "", it.phone ?: "", it.companyName ?: "") },
        interests = this.interestList.map { it.interestName ?: "" },
        languages = this.languageList.map { Language(it.languageName ?: "", it.level ?: "") }
    )
}

