package com.example.drivenui.engine.generative_screen.binding

import android.content.Context
import android.util.Log
import com.example.drivenui.parser.models.DataContext
import com.google.gson.JsonElement
import com.google.gson.JsonParser

class DataContextProvider(private val appContext: Context) {

    private var dataContext = DataContext()

    /**
     * Загружает JSON файл из assets
     */
    fun loadJsonFromAssets(fileName: String): JsonElement? {
        return try {
            val jsonString = appContext.assets.open(fileName).bufferedReader().use { it.readText() }
            JsonParser.parseString(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading JSON from assets: $fileName", e)
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
     * Загружает и добавляет JSON файл по имени
     */
    fun loadAndAddJsonFile(fileName: String) {
        loadJsonFromAssets(fileName)?.let { jsonData ->
            val baseName = fileName.removeSuffix(".json")
            addJsonSource(baseName, jsonData)
        }
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