package com.example.cvmaker.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CvMakerApi {
    @POST("/api/generate-cv/")
    suspend fun generateCv(@Body body: ApiCvRequest): Response<Any>
}