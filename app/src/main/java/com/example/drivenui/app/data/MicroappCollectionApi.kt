package com.example.drivenui.app.data

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

private const val BASE_URL = "http://45.8.229.106:8091"
private const val TAG = "MicroappCollectionApi"
private const val HEADER_MDM_ID = "X-MDM-ID"

data class MicroappCodeItem(val microappCode: String)

private data class MicroappsResponse(val microapps: List<MicroappCodeItem> = emptyList())

class MicroappCollectionApi @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson,
) {

    suspend fun fetchMicroappCodes(collectionId: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL/microapps"
                Log.d(TAG, "Fetching microapp codes from: $url (X-MDM-ID: $collectionId)")
                val request = Request.Builder()
                    .url(url)
                    .header(HEADER_MDM_ID, collectionId)
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("HTTP ${response.code}: ${response.message}")
                        )
                    }
                    val body = response.body?.string() ?: return@withContext Result.failure(
                        Exception("Empty response")
                    )
                    val response = gson.fromJson(body, MicroappsResponse::class.java)
                        ?: return@withContext Result.failure(Exception("Invalid JSON"))
                    val codes = response.microapps.map { it.microappCode }.filter { it.isNotBlank() }
                    Log.d(TAG, "Fetched ${codes.size} microapp codes: $codes")
                    Result.success(codes)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fetch failed", e)
                Result.failure(e)
            }
        }

    fun getMicroappZipUrl(microappCode: String): String =
        "$BASE_URL/microapp/zip/$microappCode"
}
