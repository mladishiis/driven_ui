package com.example.drivenui.app.data

import android.content.Context
import android.util.Log
import com.example.drivenui.engine.generative_screen.binding.DataContextProvider
import com.example.drivenui.engine.generative_screen.binding.ForLayoutBinding
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.parser.models.DataContext
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Интерактор для выполнения запросов и применения биндингов к экранам.
 *
 * @property appContext контекст приложения для доступа к assets и файловой системе
 */
@Singleton
class RequestInteractor @Inject constructor(
    private val appContext: Context,
    private val client: OkHttpClient,
) {

    private val dataContextProvider = DataContextProvider(appContext)

    /**
     * Выполняет screen query и обновляет экран с учётом нового `DataContext`.
     *
     * @param screenModel экран для обновления
     * @param action параметры запроса из разметки события
     * @param resolveQueryValue резолв значений запроса перед отправкой
     * @return экран с применёнными биндингами
     */
    suspend fun executeQueryAndUpdateScreen(
        screenModel: ScreenModel,
        action: UiAction.ExecuteQuery,
        resolveQueryValue: (String) -> String = { it },
    ): ScreenModel {
        if (action.mockEnabled) {
            loadMockQuery(action)
        } else {
            executeNetworkQuery(action, resolveQueryValue)
        }

        return ForLayoutBinding.applyBindings(
            screenModel,
            dataContextProvider.getDataContext(),
        )
    }

    /**
     * Применяет биндинги к экрану без выполнения запросов.
     *
     * @param screenModel экран для применения биндингов
     * 
     * @return экран с применёнными биндингами
     */
    fun applyBindingsToScreen(screenModel: ScreenModel): ScreenModel {
        return ForLayoutBinding.applyBindings(screenModel, dataContextProvider.getDataContext())
    }

    /**
     * Возвращает `ForLayoutBinding` для обхода дерева и `resolvedMaxForIndex` на FOR-layout’ах.
     *
     * @return синглтон `ForLayoutBinding`
     */
    fun getForLayoutBinding(): ForLayoutBinding = ForLayoutBinding

    /**
     * Возвращает `DataContext` для применения биндингов в рендерерах.
     *
     * @return текущий контекст данных (JSON-источники, результаты запросов)
     */
    fun getDataContext(): DataContext = dataContextProvider.getDataContext()

    private fun loadMockQuery(action: UiAction.ExecuteQuery) {
        val fileName = action.mockFile?.trim()?.takeIf { it.isNotEmpty() } ?: return
        try {
            val jsonData = dataContextProvider.loadJsonSmart(fileName)
            if (jsonData != null) {
                dataContextProvider.addScreenQueryResult(action.queryCode, jsonData)
            } else {
                Log.e(TAG, "Не удалось загрузить mock-файл: $fileName, queryCode=${action.queryCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки mock-файла: $fileName", e)
        }
    }

    private suspend fun executeNetworkQuery(
        action: UiAction.ExecuteQuery,
        resolveQueryValue: (String) -> String,
    ) {
        if (action.endpoint.isBlank()) {
            Log.e(TAG, "Не задан endpoint для queryCode=${action.queryCode}")
            return
        }
        withContext(Dispatchers.IO) {
            try {
                val request = buildRequest(action, resolveQueryValue) ?: return@withContext
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Ошибка запроса ${action.queryCode}: HTTP ${response.code} ${response.message}")
                        return@withContext
                    }
                    val body = response.body?.string()
                    if (body.isNullOrBlank()) {
                        Log.e(TAG, "Пустой ответ запроса: queryCode=${action.queryCode}")
                        return@withContext
                    }
                    dataContextProvider.addScreenQueryResult(action.queryCode, body)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка выполнения запроса: queryCode=${action.queryCode}", e)
            }
        }
    }

    private fun buildRequest(
        action: UiAction.ExecuteQuery,
        resolveQueryValue: (String) -> String,
    ): Request? {
        val endpoint = resolveQueryValue(action.endpoint.trim())
        val url = buildUrl(endpoint, action.queryString, resolveQueryValue) ?: run {
            Log.e(TAG, "Некорректный endpoint: $endpoint")
            return null
        }
        val requestBuilder = Request.Builder().url(url)
        action.queryHeader.forEach { (name, value) ->
            requestBuilder.header(name, resolveQueryValue(value))
        }
        return requestBuilder
            .method(action.type.normalizedHttpMethod(), action.buildRequestBody(resolveQueryValue))
            .build()
    }

    private fun buildUrl(
        endpoint: String,
        queryString: Map<String, String>,
        resolveQueryValue: (String) -> String,
    ) = endpoint.toAbsoluteEndpoint()
        .toHttpUrlOrNull()
        ?.newBuilder()
        ?.apply {
            queryString.forEach { (name, value) ->
                addQueryParameter(name, resolveQueryValue(value))
            }
        }
        ?.build()

    private fun String.toAbsoluteEndpoint(): String {
        if (startsWith("http://") || startsWith("https://")) return this
        return DEFAULT_BASE_URL + if (startsWith("/")) this else "/$this"
    }

    private fun String.normalizedHttpMethod(): String = trim().uppercase().ifBlank { METHOD_GET }

    private fun UiAction.ExecuteQuery.buildRequestBody(resolveQueryValue: (String) -> String) =
        when (type.normalizedHttpMethod()) {
            METHOD_GET, METHOD_HEAD -> null
            else -> queryBody
                .toJsonObject(resolveQueryValue)
                .toString()
                .toRequestBody(JSON_MEDIA_TYPE)
        }

    private fun Map<String, String>.toJsonObject(resolveQueryValue: (String) -> String): JsonObject {
        val jsonObject = JsonObject()
        forEach { (name, value) ->
            jsonObject.addProperty(name, resolveQueryValue(value))
        }
        return jsonObject
    }

    companion object {
        private const val TAG = "RequestInteractor"
        private const val DEFAULT_BASE_URL = "http://45.8.229.106:8092"
        private const val METHOD_GET = "GET"
        private const val METHOD_HEAD = "HEAD"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}