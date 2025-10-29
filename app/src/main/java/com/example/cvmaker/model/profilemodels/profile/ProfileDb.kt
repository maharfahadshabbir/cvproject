package com.example.cvmaker.model.profilemodels.profile

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Profile_model_requests")

data class ProfileDb(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val profileId: Int
)
