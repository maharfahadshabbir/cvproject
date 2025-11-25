package com.example.cvmaker.cvgenerator

import android.util.Log
import com.example.cvmaker.cv.toApiRequest
import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object CvGenerator {

    private const val BASE_URL = "https://cvmaker.fastdl.video/api/generate-cv/"
    private const val SECRET_KEY = "pJdZQ54ObSgmFPBCybSpIfCcjNFe76WgsyxT2uDWk9OHdmR2V2"

    fun generate(cv: CvModelRequestDb, templateName: String? = null, onResult: (success: Boolean, urls: List<String>) -> Unit) {
        try {
            val gson = Gson()
            val requestBody = cv.toApiRequest(templateName)
            val json = gson.toJson(requestBody)
            Log.d("CV_GENERATE", "Generating CV with template: $templateName")
            Log.d("CV_GENERATE", "Full CV data: $json")

            // Use your favorite networking library here (Retrofit / OkHttp)
            val client = OkHttpClient()
            val body = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(BASE_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Secret-Key", SECRET_KEY)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("CV_GENERATE", "Error generating CV", e)
                    onResult(false, emptyList())
                }

                override fun onResponse(call: Call, response: Response) {
                    val resp = response.body?.string()
                    Log.d("CV_GENERATE", "Response: $resp")
                    resp?.let {
                        val jsonObj = JSONObject(it)
                        val urls = mutableListOf<String>()
                        if (jsonObj.has("download_url_pdf")) {
                            urls.add(jsonObj.getString("download_url_pdf"))
                        } else if (jsonObj.has("generated_files")) {
                            val arr = jsonObj.getJSONArray("generated_files")
                            for (i in 0 until arr.length()) {
                                urls.add(arr.getJSONObject(i).getString("download_url_pdf"))
                            }
                        }
                        onResult(jsonObj.optBoolean("success", false), urls)
                    } ?: onResult(false, emptyList())
                }
            })

        } catch (e: Exception) {
            Log.e("CV_GENERATE", "Error generating CV", e)
            onResult(false, emptyList())
        }
    }
}
