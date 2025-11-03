package com.example.cvmaker.data.api

import com.example.cvmaker.model.workingmodels.CvModelRequestDb
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class CvMakerRepository(
    private val gson: Gson = Gson()
) {
    private val api = CvMakerService.api()

    suspend fun generateCv(
        cv: CvModelRequestDb,
        templateName: String? // null/"" => all; "ats1"/"style3" => single
    ): GenerateCvResult = withContext(Dispatchers.IO) {
        try {
            val body = CvMakerMapper.toApiRequest(cv, templateName = templateName)
            val res: Response<Any> = api.generateCv(body)

            if (!res.isSuccessful) {
                return@withContext GenerateCvResult.Error("HTTP ${res.code()}: ${res.errorBody()?.string()?.take(400)}")
            }

            val rawObj = gson.toJsonTree(res.body())?.asJsonObject ?: JsonObject()
            val success = rawObj["success"]?.asBoolean == true
            if (!success) return@withContext GenerateCvResult.Error("API returned success=false")

            // Single-template shape
            if (rawObj.has("download_url_pdf")) {
                val url = rawObj["download_url_pdf"]?.asString.orEmpty()
                return@withContext if (url.isNotBlank()) {
                    GenerateCvResult.Single(url)
                } else GenerateCvResult.Error("Missing download_url_pdf")
            }

            // All-templates shape
            if (rawObj.has("generated_files")) {
                val multi = gson.fromJson(rawObj, MultiTemplateResponse::class.java)
                val list = multi.generated_files.mapNotNull { gf ->
                    val t = gf.template ?: return@mapNotNull null
                    val u = gf.download_url_pdf ?: return@mapNotNull null
                    t to u
                }
                return@withContext if (list.isNotEmpty()) {
                    GenerateCvResult.Multiple(list)
                } else GenerateCvResult.Error("No generated files")
            }

            GenerateCvResult.Error("Unknown response shape")
        } catch (e: Exception) {
            GenerateCvResult.Error("Exception: ${e.message}")
        }
    }
}