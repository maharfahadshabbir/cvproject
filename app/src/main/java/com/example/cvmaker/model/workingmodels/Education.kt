package com.example.cvmaker.model.workingmodels

import java.util.UUID

data class EducationModel(
    var institute: String? = null,                   // from institute_edittext
    var course: String? = null,                      // from course_edittext
    var grade: String? = null,                       // from grade_edittext
    var startDate: String? = null,                   // from start_date_edittext
    var endDate: String? = null,                     // from end_date_edittext
    var isCurrentStudent: Boolean = false,           // from checkboxfordate
    var expanded: Boolean = true                     // for expand/collapse UI state
)
