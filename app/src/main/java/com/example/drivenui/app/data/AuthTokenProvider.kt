package com.example.drivenui.app.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Получает и хранит токен авторизации для сетевых запросов приложения.
 *
 * Токен запрашивается один раз на запуск приложения и переиспользуется
 * общим [AuthInterceptor] для добавления заголовка `Authorization`.
 *
 * @property gson парсер JSON-ответа авторизации
 */
@Singleton
class AuthTokenProvider @Inject constructor(
    private val gson: Gson,
) {

    private val mutex = Mutex()
    private val authClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var token: String? = null

    /**
     * Запрашивает токен при старте приложения.
     *
     * Ошибка получения токена только логируется: последующие запросы смогут
     * повторить получение токена через [getToken].
     */
    suspend fun prefetchToken() {
        if (getToken() == null) {
            Log.e(AUTH_TAG, "Не удалось получить токен при запуске")
        }
    }

    /**
     * Возвращает текущий токен или запрашивает новый, если токен ещё не получен.
     *
     * @return строка токена или `null`, если авторизация не удалась
     */
    suspend fun getToken(): String? {
        token?.let { return it }

        return mutex.withLock {
            token ?: refreshToken().getOrNull()
        }
    }

    private suspend fun refreshToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = gson.toJson(
                mapOf(
                    "username" to AUTH_USERNAME,
                    "password" to AUTH_PASSWORD,
                )
            ).toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(AUTH_URL)
                .post(requestBody)
                .build()

            authClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("HTTP ${response.code}: ${response.message}")
                    )
                }

                val body = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty auth response"))
                val parsedToken = parseToken(body)
                    ?: return@withContext Result.failure(Exception("Token field not found"))

                token = parsedToken
                Result.success(parsedToken)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseToken(body: String): String? {
        val json = runCatching { JsonParser.parseString(body) }.getOrNull() ?: return null
        if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            return json.asString.takeIf { it.isNotBlank() }
        }
        return findToken(json)
    }

    private fun findToken(element: JsonElement): String? {
        if (!element.isJsonObject) return null

        val obj = element.asJsonObject
        TOKEN_FIELDS.firstNotNullOfOrNull { field ->
            obj.get(field)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
                ?.asString
                ?.takeIf { it.isNotBlank() }
        }?.let { return it }

        return obj.entrySet()
            .asSequence()
            .map { (_, value) -> value }
            .filter { it.isJsonObject }
            .mapNotNull(::findToken)
            .firstOrNull()
    }

    companion object {
        private const val AUTH_URL = "http://45.8.229.106:8092/auth/sing-in"
        private const val AUTH_USERNAME = "admin"
        private const val AUTH_PASSWORD = "pass"
        private const val AUTH_TAG = "AuthTokenProvider"

        private val TOKEN_FIELDS = listOf(
            "token",
            "accessToken",
            "access_token",
            "jwt",
            "idToken",
        )
    }
}