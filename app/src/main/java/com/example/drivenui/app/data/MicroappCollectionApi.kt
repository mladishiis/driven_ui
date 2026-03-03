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

data class MicroappCodeItem(val microappCode: String)

class MicroappCollectionApi @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson,
) {

    suspend fun fetchMicroappCodes(collectionId: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL/microapps/$collectionId"
                Log.d(TAG, "Fetching microapp codes from: $url")
                val request = Request.Builder().url(url).get().build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("HTTP ${response.code}: ${response.message}")
                        )
                    }
                    val body = response.body?.string() ?: return@withContext Result.failure(
                        Exception("Empty response")
                    )
                    val items = gson.fromJson(body, Array<MicroappCodeItem>::class.java)
                        ?: return@withContext Result.failure(Exception("Invalid JSON"))
                    val codes = items.map { it.microappCode }.filter { it.isNotBlank() }
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
