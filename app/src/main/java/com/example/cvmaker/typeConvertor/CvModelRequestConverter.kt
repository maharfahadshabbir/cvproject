package com.example.cvmaker.typeConvertor

import androidx.room.TypeConverter
import com.example.cvmaker.cv.CvModelRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CvModelRequestConverter {

    @TypeConverter
    fun fromJson(json: String): CvModelRequest {
        val type = object : TypeToken<CvModelRequest>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun toJson(cvModelRequest: CvModelRequest): String {
        return Gson().toJson(cvModelRequest)
    }
}
