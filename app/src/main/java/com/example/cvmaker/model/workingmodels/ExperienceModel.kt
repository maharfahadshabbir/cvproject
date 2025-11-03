package com.example.cvmaker.model.workingmodels

data class ExperienceModel(
    var companyName: String? =null,
    var designation: String? =null,
    var startDate: String? = null,
    var endDate: String? = null,
    var isCurrentWorking: Boolean = false,
    var expanded: Boolean = true,
    var detail: String? = null,
)
