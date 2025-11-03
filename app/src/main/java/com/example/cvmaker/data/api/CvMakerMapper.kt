package com.example.cvmaker.data.api


import com.example.cvmaker.model.workingmodels.*

object CvMakerMapper {

    fun toApiRequest(
        cv: CvModelRequestDb,
        templateName: String?,
        templateType: String? = "ats"
    ): ApiCvRequest {

        val portfolio = cv.portfolioList.firstOrNull()

        val personal = cv.personalDetails
        val about = cv.objective?.objective ?: ""

        return ApiCvRequest(
            template_type = templateType ?: "ats",
            template_name = templateName ?: "",
            personal_info = personal?.let {
                PersonalInfo(
                    name = it.name.orEmpty(),
                    email = it.email.orEmpty(),
                    phone = it.phone.orEmpty(),
                    dob = it.dateOfBirth.orEmpty(),
                    address = it.address.orEmpty(),
                    alternate_phone = it.phone2.orEmpty(),
                    id_card_number = it.idCard.orEmpty(),
                    passport_number = it.passport.orEmpty(),
                    nationality = it.nationality.orEmpty(),
                    gender = it.gender.orEmpty(),
                    marital_status = it.maritalStatus.orEmpty(),
                    aboutme = about,
                    github_link = portfolio?.github.orEmpty(),
                    dribbble_link = portfolio?.dribble.orEmpty(),
                    behance_link = portfolio?.behance.orEmpty(),
                    website_name = portfolio?.websiteName.orEmpty(),
                    website_link = portfolio?.websiteLink.orEmpty()
                )
            },
            educations = cv.educationList.map { edu ->
                ApiEducation(
                    degree = edu.course.orEmpty(),          // your "course" becomes "degree"
                    institution = edu.institute.orEmpty(),
                    year = formatYearRange(edu.startDate, edu.endDate, edu.isCurrentStudent)
                )
            },
            experiences = cv.experienceList.map { exp ->
                ApiExperience(
                    job_title = exp.designation.orEmpty(),
                    company = exp.companyName.orEmpty(),
                    start_date = exp.startDate.orEmpty(),
                    end_date = if (exp.isCurrentWorking) "Present" else exp.endDate.orEmpty(),
                    description = exp.detail.orEmpty()
                )
            },
            // API accepts simple list of strings. We'll use skillName.
            skills = cv.skillsList.mapNotNull { it.skillName?.takeIf { n -> n.isNotBlank() } },

            projects = cv.projectList.map { p ->
                val desc = buildString {
                    append(p.description.orEmpty())
                    if (!p.link.isNullOrBlank()) {
                        if (isNotEmpty()) append("\n")
                        append("Link: ${p.link}")
                    }
                }
                ApiProject(title = p.projectTitle.orEmpty(), description = desc)
            },
            references = cv.referenceList.map { ref ->
                val relation = listOfNotNull(ref.designation, ref.companyName)
                    .filter { it?.isNotBlank() == true }
                    .joinToString(" @ ")
                ApiReference(
                    name = ref.name.orEmpty(),
                    relation = relation,
                    phone = ref.phone.orEmpty(),
                    email = ref.email.orEmpty()
                )
            },
            interests = cv.interestList.mapNotNull { it.interestName?.takeIf { n -> n.isNotBlank() } },

            certifications = cv.certificationList.map { c ->
                ApiCertification(
                    course = c.course.orEmpty(),
                    institute = c.institute.orEmpty(),
                    grade = c.grade.orEmpty(),
                    startDate = c.startDate.orEmpty(),
                    endDate = c.endDate.orEmpty(),
                    isCurrentStudent = c.isCurrentStudent
                )
            },
            achievements = cv.achievementList.map { a ->
                ApiAchievement(
                    title = a.title.orEmpty(),
                    organization = a.organization.orEmpty(),
                    year = a.year.orEmpty(),
                    description = a.description.orEmpty()
                )
            },
            languages = cv.languageList.map { l ->
                ApiLanguage(
                    languageName = l.languageName.orEmpty(),
                    level = l.level.orEmpty()
                )
            }
        )
    }

    private fun formatYearRange(
        start: String?,
        end: String?,
        isCurrent: Boolean
    ): String {
        val s = start?.takeIf { it.isNotBlank() }
        val e = when {
            isCurrent -> "Present"
            !end.isNullOrBlank() -> end
            else -> null
        }
        return when {
            s != null && e != null -> "$s - $e"
            s != null -> s
            e != null -> e
            else -> ""
        }
    }
}