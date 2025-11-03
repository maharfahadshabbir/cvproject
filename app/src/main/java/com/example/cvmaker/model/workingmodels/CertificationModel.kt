package com.example.cvmaker.model.workingmodels

data class CertificationModel(
    var course: String? = null,
    var institute: String? = null,
    var grade: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var isCurrentStudent: Boolean = false,
    var expanded: Boolean = true
)
