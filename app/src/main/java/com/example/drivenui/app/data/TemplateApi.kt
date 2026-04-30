package com.example.drivenui.app.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

/**
 * API для работы с шаблонами на тестовом сервере.
 *
 * @property client общий HTTP-клиент приложения с авторизацией
 */
class TemplateApi @Inject constructor(
    private val client: OkHttpClient,
) {

    /**
     * Формирует URL для скачивания ZIP-архива, сгенерированного из шаблона.
     *
     * @param templateType тип шаблона
     * @param templateCode код шаблона
     * @return URL метода `GET /template/zip/{templateType}/{templateCode}`
     */
    fun getTemplateZipUrl(templateType: String, templateCode: String): String =
        "$BASE_URL/template/zip/$templateType/$templateCode"

    /**
     * Загружает PNG-скриншот экрана микроаппа на сервер.
     *
     * @param microappCode код микроаппа
     * @param screenCode код экрана
     * @param pngBytes содержимое PNG-файла
     * @return [Result.success] при успешной загрузке или [Result.failure] при ошибке
     */
    suspend fun uploadScreenshot(
        microappCode: String,
        screenCode: String,
        pngBytes: ByteArray,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "$screenCode.png",
                    pngBytes.toRequestBody("image/png".toMediaType()),
                )
                .build()
            val request = Request.Builder()
                .url("$BASE_URL/microapp/image/$microappCode/$screenCode")
                .post(requestBody)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("HTTP ${response.code}: ${response.message}")
                    )
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки скриншота ($screenCode)", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val BASE_URL = "http://45.8.229.106:8092"
        private const val TAG = "TemplateApi"
    }
}