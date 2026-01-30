package com.example.drivenui.engine.generative_screen.binding

import android.content.Context
import android.util.Log
import com.example.drivenui.parser.models.DataContext
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.File

class DataContextProvider(private val appContext: Context) {

    private var dataContext = DataContext()

    /**
     * Загружает JSON файл из assets
     */
    fun loadJsonSmart(fileName: String): JsonElement? {
        // 1. runtime
        val runtimeFile = File(appContext.filesDir, "assets_simulation/microappTavrida/mocks/$fileName")
        if (runtimeFile.exists()) {
            return runtimeFile.readText().let(JsonParser::parseString)
        }

        // 2. assets
        return try {
            appContext.assets.open("mocks/$fileName")
                .bufferedReader()
                .use { JsonParser.parseString(it.readText()) }
        } catch (e: Exception) {
            Log.e(TAG, "JSON not found in runtime or assets: $fileName", e)
            null
        }
    }

    /**
     * Добавляет JSON источник данных
     */
    fun addJsonSource(name: String, jsonData: JsonElement) {
        val currentSources = dataContext.jsonSources.toMutableMap()
        currentSources[name] = jsonData
        dataContext = dataContext.copy(jsonSources = currentSources)
        Log.d(TAG, "Added JSON source: $name")
    }

    /**
     * Добавляет результат screen query
     */
    fun addScreenQueryResult(name: String, jsonData: JsonElement) {
        val currentResults = dataContext.screenQueryResults.toMutableMap()
        currentResults[name] = jsonData
        dataContext = dataContext.copy(screenQueryResults = currentResults)
        Log.d(TAG, "Added screen query result: $name")
    }

    /**
     * Добавляет результат запроса
     */
    fun addQueryResult(name: String, result: Any) {
        val currentResults = dataContext.queryResults.toMutableMap()
        currentResults[name] = result
        dataContext = dataContext.copy(queryResults = currentResults)
        Log.d(TAG, "Added query result: $name")
    }

    /**
     * Добавляет результат screen query как строку
     */
    fun addScreenQueryResult(name: String, jsonString: String) {
        try {
            val jsonData = JsonParser.parseString(jsonString)
            addScreenQueryResult(name, jsonData)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse screen query result as JSON: $jsonString", e)
        }
    }

    /**
     * Очищает контекст
     */
    fun clear() {
        dataContext = DataContext()
        Log.d(TAG, "Data context cleared")
    }

    /**
     * Получает текущий контекст данных
     */
    fun getDataContext(): DataContext = dataContext

    /**
     * Печатает отладочную информацию о контексте
     */
    fun debugInfo() {
        Log.d(TAG, "=== Data Context Debug ===")
        Log.d(TAG, "JSON Sources (${dataContext.jsonSources.size}): ${dataContext.jsonSources.keys}")
        Log.d(TAG, "Screen Query Results (${dataContext.screenQueryResults.size}): ${dataContext.screenQueryResults.keys}")
        Log.d(TAG, "Query Results (${dataContext.queryResults.size}): ${dataContext.queryResults.keys}")
        Log.d(TAG, "Local Variables (${dataContext.localVariables.size}): ${dataContext.localVariables.keys}")

        dataContext.screenQueryResults.forEach { (key, value) ->
            Log.d(TAG, "  Screen Query '$key': ${value::class.simpleName}")
            if (value is JsonElement) {
                val preview = value.toString().take(100)
                Log.d(TAG, "    Value preview: $preview...")
            }
        }

        dataContext.jsonSources.forEach { (key, value) ->
            Log.d(TAG, "  JSON Source '$key': ${value::class.simpleName}")
            if (value is JsonElement) {
                val preview = value.toString().take(100)
                Log.d(TAG, "    Value preview: $preview...")
            }
        }

        Log.d(TAG, "=== End Debug Info ===")
    }

    companion object {
        private const val TAG = "DataContextProvider"
    }
}