package com.example.cvmaker.model.workingmodels

// A simple item for the list
data class CvProfileItem(
    val id: Long?,                    // Room entity id
    val data: CvModelRequestDb,      // Full CV data
    val updatedAt: String?           // For display
)
