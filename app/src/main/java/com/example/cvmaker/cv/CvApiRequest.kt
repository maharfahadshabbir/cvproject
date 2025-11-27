package com.example.cvmaker.cv

import com.example.cvmaker.model.workingmodels.CvModelRequestDb

// Root API Request
data class CvApiRequest(
    val template_name: String? = null,
    val personal_info: PersonalInfo,
    val educations: List<Education> = emptyList(),
    val experiences: List<Experience> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val projects: List<Project> = emptyList(),
    val references: List<Reference> = emptyList(),
    val interests: List<Interest> = emptyList(),
    val certifications: List<Certification> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val languages: List<Language> = emptyList()
)

// Personal Info
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
    val website_link: String = "",
    val linkedin_link: String = "",
    val twitter_handle: String = ""
)

// Education
data class Education(
    val institute: String,
    val course_degree: String,
    val grades: String? = null,
    val start_date: String? = null,
    val end_date: String? = null
)

// Experience
data class Experience(
    val company_name: String,
    val designation: String,
    val start_date: String? = null,
    val end_date: String? = null,
    val currently_working: Boolean = false,
    val details: String? = null
)

// Skill
data class Skill(
    val skill_name: String,
    val skill_level: Int = 0
)

// Project
data class Project(
    val project_title: String,
    val description: String
)

// Reference
data class Reference(
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val designation: String? = null,
    val company_name: String? = null
)

// Certification
data class Certification(
    val course_degree: String,
    val institute: String,
    val grades: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val currently_student: Boolean = false
)

// Achievement
data class Achievement(
    val title: String,
    val organization: String,
    val year: String,
    val description: String? = null
)

// Language
data class Language(
    val language_name: String,
    val language_level: String
)

// Interest
data class Interest(
    val interest: String
)

// Mapping Extension
fun CvModelRequestDb.toApiRequest(templateName: String? = null): CvApiRequest {
    return CvApiRequest(
        template_name = templateName,
        personal_info = PersonalInfo(
            name = personalDetails?.name ?: "",
            email = personalDetails?.email ?: "",
            phone = personalDetails?.phone ?: "",
            alternate_phone = personalDetails?.phone2 ?: "",
            dob = personalDetails?.dateOfBirth ?: "",
            address = personalDetails?.address ?: "",
            id_card_number = personalDetails?.idCard ?: "",
            passport_number = personalDetails?.passport ?: "",
            nationality = personalDetails?.nationality ?: "",
            gender = personalDetails?.gender ?: "",
            marital_status = personalDetails?.maritalStatus ?: "",

            // Generate a VIP-style About Me dynamically
            aboutme = buildString {
                val name = personalDetails?.name ?: "This person"

                // Determine main skill or fallback
                val mainSkill = skillsList.firstOrNull()?.skillName ?: "professional"

                // Current company (if exists)
                val currentCompany = experienceList.firstOrNull()?.companyName

                // Years of experience (based on first experience start_date)
                val firstExpStartYear = experienceList.firstOrNull()?.startDate?.take(4)?.toIntOrNull()
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                val yearsExp = if (firstExpStartYear != null && currentYear >= firstExpStartYear) {
                    currentYear - firstExpStartYear
                } else null

                // Start sentence with name + role
                append("$name is a skilled $mainSkill")
                if (!currentCompany.isNullOrEmpty()) append(" at $currentCompany")

                // Add years of experience if available
                if (yearsExp != null && yearsExp > 0) append(", with $yearsExp years of experience")

                // Add top 3 skills gracefully
                val topSkills = skillsList.take(3).mapNotNull { it.skillName }.joinToString(", ")
                if (topSkills.isNotEmpty()) append(". Expertise includes $topSkills")

                append(".") // End with period
            },


            github_link = portfolioList.getOrNull(0)?.github ?: "",
            dribbble_link = portfolioList.getOrNull(0)?.dribble ?: "",
            behance_link = portfolioList.getOrNull(0)?.behance ?: "",
            website_name = portfolioList.getOrNull(0)?.websiteName ?: "",
            website_link = portfolioList.getOrNull(0)?.websiteLink ?: ""
        ),
        educations = educationList.map {
            Education(
                institute = it.institute ?: "",
                course_degree = it.course ?: "",
                grades = it.grade ?: "",
                start_date = it.startDate ?: "",
                end_date = it.endDate ?: ""
            )
        },
        experiences = experienceList.map {
            Experience(
                company_name = it.companyName ?: "",
                designation = it.designation ?: "",
                start_date = it.startDate ?: "",
                end_date = it.endDate ?: "",
                currently_working = it.isCurrentWorking,
                details = it.detail ?: ""
            )
        },
        skills = skillsList.map { Skill(it.skillName ?: "", it.skillLevel ?: 0) },
        projects = projectList.map { Project(it.projectTitle ?: "", it.description ?: "") },
        references = referenceList.map {
            Reference(
                name = it.name ?: "",
                phone = it.phone ?: "",
                email = it.email ?: "",
                designation = it.designation ?: "",
                company_name = it.companyName ?: ""
            )
        },
        interests = interestList.map { Interest(it.interestName ?: "") },
        certifications = certificationList.map {
            Certification(
                course_degree = it.course ?: "",
                institute = it.institute ?: "",
                grades = it.grade ?: "",
                start_date = it.startDate ?: "",
                end_date = it.endDate ?: "",
                currently_student = it.isCurrentStudent
            )
        },
        achievements = achievementList.map {
            Achievement(
                title = it.title ?: "",
                organization = it.organization ?: "",
                year = it.year ?: "",
                description = it.description ?: ""
            )
        },
        languages = languageList.map { Language(it.languageName ?: "", it.level ?: "") }
    )
}

val dummyCvApiRequest = CvApiRequest(
    template_name = "style3",
    personal_info = PersonalInfo(
        name = "Elon Musk",
        email = "elon.musk@tesla.com",
        phone = "+1-310-555-1234",
        alternate_phone = "+1-310-555-5678",
        dob = "1971-06-28",
        address = "3500 Deer Creek Road, Palo Alto, California, USA",
        id_card_number = "N/A",
        passport_number = "US987654321",
        nationality = "American",
        gender = "Male",
        marital_status = "Married",
        aboutme = "Visionary entrepreneur and engineer with a track record of building and scaling transformative companies across multiple industries including aerospace, automotive, energy, and artificial intelligence. Known for driving innovation in space travel, sustainable energy, and next-generation technologies.",
        github_link = "https://github.com/elonmusk",
        dribbble_link = "",
        behance_link = "",
        website_name = "X (formerly Twitter)",
        website_link = "https://x.com/elonmusk",
        linkedin_link = "https://www.linkedin.com/in/elonmusk",
        twitter_handle = "@elonmusk"
    ),
    educations = listOf(
        Education(
            institute = "University of Pennsylvania",
            course_degree = "BSc Physics, BSc Economics (Wharton School)",
            grades = "Graduated",
            start_date = "1992",
            end_date = "1995"
        ),
        Education(
            institute = "Stanford University",
            course_degree = "PhD in Applied Physics (Dropped Out)",
            grades = "N/A",
            start_date = "1995",
            end_date = "1995"
        )
    ),
    experiences = listOf(
        Experience(
            company_name = "Tesla, Inc.",
            designation = "CEO & Product Architect",
            start_date = "2004",
            end_date = null,
            currently_working = true,
            details = "Leading the global electric vehicle revolution, overseeing production, innovation, and scaling of EVs, solar energy, and battery technology."
        ),
        Experience(
            company_name = "SpaceX",
            designation = "CEO & Lead Designer",
            start_date = "2002",
            end_date = null,
            currently_working = true,
            details = "Founded SpaceX to reduce space transportation costs and enable Mars colonization. Oversaw development of Falcon rockets, Dragon spacecraft, and Starship."
        ),
        Experience(
            company_name = "PayPal (X.com)",
            designation = "Co-Founder",
            start_date = "1999",
            end_date = "2002",
            currently_working = false,
            details = "Founded X.com, an online payment company which later became PayPal after a merger. Acquired by eBay in 2002."
        ),
        Experience(
            company_name = "OpenAI",
            designation = "Co-Founder",
            start_date = "2015",
            end_date = "2018",
            currently_working = false,
            details = "Helped establish OpenAI to ensure artificial general intelligence benefits humanity."
        ),
        Experience(
            company_name = "SolarCity (acquired by Tesla)",
            designation = "Chairman",
            start_date = "2006",
            end_date = "2016",
            currently_working = false,
            details = "Led strategy and vision for solar energy adoption through SolarCity, later merged with Tesla Energy."
        )
    ),
    skills = listOf(
        Skill("Entrepreneurship", 5),
        Skill("Engineering Leadership", 3),
        Skill("Space Technology", 1),
        Skill("Electric Vehicles", 2),
        Skill("Artificial Intelligence", 4),
        Skill("Renewable Energy", 5),
        Skill("Strategic Vision", 5),
        Skill("Innovation Management", 5)
    ),
    projects = listOf(
        Project("Starship", "Fully reusable spacecraft designed for missions to Mars and beyond."),
        Project("Tesla Model S", "Luxury electric sedan that redefined the EV market."),
        Project("Neuralink", "Brain–computer interface company developing implantable devices to enable direct communication between humans and machines."),
        Project("The Boring Company", "Infrastructure and tunnel construction services company aiming to reduce traffic congestion with underground transportation systems."),
        Project("Hyperloop Concept", "Proposed high-speed transportation system using vacuum tubes for near-supersonic travel."),
        Project("Tesla Gigafactories", "Large-scale battery and EV production plants designed to accelerate global transition to sustainable energy.")
    ),
    references = listOf(
        Reference("Larry Page", "N/A", "larry@google.com", "Co-Founder", "Google"),
        Reference("Richard Branson", "N/A", "richard@virgin.com", "Founder", "Virgin Group"),
        Reference("Bill Gates", "N/A", "bill@gatesfoundation.org", "Co-Founder", "Microsoft")
    ),
    interests = listOf(
        Interest("Space Exploration"),
        Interest("Artificial Intelligence"),
        Interest("Sustainable Energy"),
        Interest("Futurism"),
        Interest("Philanthropy"),
        Interest("Physics & Engineering")
    ),
    certifications = listOf(
        Certification("Honorary Doctorate of Engineering", "Yale University", "N/A", "2015", "2015", false),
        Certification("Honorary Doctorate in Technology", "Technion – Israel Institute of Technology", "N/A", "2018", "2018", false)
    ),
    achievements = listOf(
        Achievement("Time Person of the Year", "TIME Magazine", "2021", "Recognized for groundbreaking impact in space exploration, electric vehicles, and global innovation."),
        Achievement("Royal Aeronautical Society Gold Medal", "Royal Aeronautical Society", "2012", "Awarded for outstanding contributions to aerospace engineering."),
        Achievement("Forbes Most Powerful People", "Forbes", "2016", "Listed among the most influential leaders worldwide.")
    ),
    languages = listOf(
        Language("English", "Expert"),
        Language("Afrikaans", "Beginner"),
        Language("French", "Intermediate")
    )
)

/*
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
        */
/*    aboutme = this.personalDetails?.aboutMe ?: "",
            github_link = this.personalDetails?.githubLink ?: "",
            dribbble_link = this.personalDetails?.dribbbleLink ?: "",
            behance_link = this.personalDetails?.behanceLink ?: "",
            website_name = this.personalDetails?.websiteName ?: "",
            website_link = this.personalDetails?.websiteLink ?: ""*//*

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

*/
