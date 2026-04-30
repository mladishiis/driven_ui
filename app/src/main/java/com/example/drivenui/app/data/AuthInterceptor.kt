package com.example.drivenui.app.data

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor, добавляющий Bearer-токен в исходящие запросы.
 *
 * @property authTokenProvider источник токена авторизации
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val authTokenProvider: AuthTokenProvider,
) : Interceptor {

    /**
     * Добавляет заголовок `Authorization`, если он не был установлен вручную.
     *
     * @param chain цепочка OkHttp interceptor'ов
     * @return HTTP-ответ на исходный или авторизованный запрос
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header("Authorization") != null) {
            return chain.proceed(request)
        }

        val token = runBlocking { authTokenProvider.getToken() }
            ?: return chain.proceed(request)

        val authorizedRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authorizedRequest)
    }
}