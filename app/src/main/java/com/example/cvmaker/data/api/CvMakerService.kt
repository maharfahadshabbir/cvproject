package com.example.cvmaker.data.api


import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CvMakerService {

    // Replace with your real base + key from the doc
    private const val BASE_URL = "https://cvmaker.fastdl.video/"
    private const val SECRET_KEY = "pJdZQ54ObSgmFPBCybSpIfCcjNFe76WgsyxT2uDWk9OHdmR2V2" // put in BuildConfig if you prefer

    fun api(): CvMakerApi {
        val headerInterceptor = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Secret-Key", SECRET_KEY)
                .build()
            chain.proceed(req)
        }

        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logger)
            .build()

        val gson = GsonBuilder()
            .serializeNulls()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(CvMakerApi::class.java)
    }
}
