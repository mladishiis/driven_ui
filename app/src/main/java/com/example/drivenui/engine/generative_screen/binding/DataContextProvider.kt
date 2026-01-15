// DataContextProvider.kt (исправленная версия)
package com.example.drivenui.engine.generative_screen.binding

import android.content.Context
import com.example.drivenui.parser.models.DataContext
import org.json.JSONArray
import org.json.JSONObject

class DataContextProvider(private val appContext: Context) {

    private var dataContext = DataContext()

    /**
     * Загружает JSON файл из assets
     */
    fun loadJsonFromAssets(fileName: String): Any? {
        return try {
            val jsonString = appContext.assets.open(fileName).bufferedReader().use { it.readText() }
            // Пробуем как JSONObject
            try {
                JSONObject(jsonString)
            } catch (e: Exception) {
                // Если не объект, пробуем как массив
                try {
                    JSONArray(jsonString)
                } catch (e2: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Добавляет JSON источник данных
     */
    fun addJsonSource(name: String, jsonData: Any) {
        val currentSources = dataContext.jsonSources.toMutableMap()

        when (jsonData) {
            is JSONObject -> {
                // Для JSONObject нужно преобразовать его поля в JSONArray
                jsonData.keys().asSequence().forEach { key ->
                    val value = jsonData.opt(key)
                    when (value) {
                        is JSONArray -> {
                            // Сохраняем массив
                            currentSources[key] = value
                            currentSources["${key}_list"] = value
                        }
                        is JSONObject -> {
                            // Преобразуем объект в массив с одним элементом
                            currentSources[key] = JSONArray().put(value)
                        }
                        else -> {
                            // Примитивные значения тоже сохраняем в массиве
                            currentSources[key] = JSONArray().put(value)
                        }
                    }
                }
            }
            is JSONArray -> {
                currentSources[name] = jsonData
                currentSources["${name}_list"] = jsonData
            }
            else -> return
        }

        dataContext = dataContext.copy(jsonSources = currentSources)
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
     * Получает значение из JSON источника
     */
    fun getJsonValue(sourceName: String, path: String = ""): Any? {
        val source = dataContext.jsonSources[sourceName]
        return if (path.isNotEmpty() && source != null) {
            DataBindingParser.extractValue(source, path)
        } else {
            source
        }
    }

    /**
     * Добавляет результат запроса
     */
    fun addQueryResult(name: String, result: Any) {
        val currentResults = dataContext.queryResults.toMutableMap()
        currentResults[name] = result
        dataContext = dataContext.copy(queryResults = currentResults)
    }

    /**
     * Добавляет результат screen query
     */
    fun addScreenQueryResult(name: String, result: Any) {
        val currentResults = dataContext.screenQueryResults.toMutableMap()
        currentResults[name] = result
        dataContext = dataContext.copy(screenQueryResults = currentResults)
    }

    /**
     * Очищает контекст
     */
    fun clear() {
        dataContext = DataContext()
    }

    /**
     * Получает текущий контекст данных
     */
    fun getDataContext(): DataContext = dataContext
}