package com.example.cvmaker.model.workingmodels


data class CvModelRequestDb(
    var personalDetails: PersonalDetailModel? = null,
    var educationList: MutableList<Education> = mutableListOf()/*,
    var experienceList: MutableList<ExperienceModel> = mutableListOf(),
    var skillsList: MutableList<SkillsModel> = mutableListOf(),
    var projectList: MutableList<ProjectModel> = mutableListOf(),
    var referenceList: MutableList<ReferenceModel> = mutableListOf(),
    var objective: ObjectiveModel? = null,
    var languageList: MutableList<LanguageModel> = mutableListOf(),
    var certificationList: MutableList<CertificationModel> = mutableListOf(),
    var achievementList: MutableList<AchievementModel> = mutableListOf(),
    var interestList: MutableList<InterestModel> = mutableListOf(),
    var portfolioList: MutableList<PortfolioModel> = mutableListOf()*/
)
