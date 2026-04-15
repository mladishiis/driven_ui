package com.example.drivenui.engine.generative_screen.binding

import android.content.Context
import android.util.Log
import com.example.drivenui.app.data.MicroappRootFinder
import com.example.drivenui.engine.parser.models.DataContext
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.File

/**
 * Провайдер контекста данных для биндингов.
 * Загружает JSON из assets или папки микроаппа, хранит результаты screen query.
 *
 * @property appContext контекст приложения для доступа к assets и файловой системе
 */
class DataContextProvider(private val appContext: Context) {

    private var dataContext = DataContext()

    /**
     * Загружает JSON-файл из папки микроаппа или assets.
     *
     * @param fileName Имя файла (например, "carriers.json")
     * @return JsonElement или null, если файл не найден
     */
    fun loadJsonSmart(fileName: String): JsonElement? {
        val microappRoot = MicroappRootFinder.findMicroappRoot(appContext)
        if (microappRoot != null) {
            val runtimeFile = File(microappRoot, "$MOCKS_PATH/$fileName")
            if (runtimeFile.exists()) {
                return runtimeFile.readText().let(JsonParser::parseString)
            }
        }

        return try {
            appContext.assets.open("$MOCKS_PATH/$fileName")
                .bufferedReader()
                .use { JsonParser.parseString(it.readText()) }
        } catch (e: Exception) {
            Log.e(TAG, "JSON не найден ни в каталоге микроаппа, ни в assets: $fileName", e)
            null
        }
    }

    /**
     * Добавляет JSON-источник данных в контекст.
     *
     * @param name Имя источника
     * @param jsonData JSON-данные
     */
    fun addJsonSource(name: String, jsonData: JsonElement) {
        val currentSources = dataContext.jsonSources.toMutableMap()
        currentSources[name] = jsonData
        dataContext = dataContext.copy(jsonSources = currentSources)
    }

    /**
     * Добавляет результат screen query в контекст.
     *
     * @param name Имя результата
     * @param jsonData JSON-данные результата
     */
    fun addScreenQueryResult(name: String, jsonData: JsonElement) {
        val currentResults = dataContext.screenQueryResults.toMutableMap()
        currentResults[name] = jsonData
        dataContext = dataContext.copy(screenQueryResults = currentResults)
    }


    /**
     * Добавляет результат screen query как JSON-строку.
     *
     * @param name Имя результата
     * @param jsonString JSON-строка
     */
    fun addScreenQueryResult(name: String, jsonString: String) {
        try {
            val jsonData = JsonParser.parseString(jsonString)
            addScreenQueryResult(name, jsonData)
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось разобрать результат запроса экрана как JSON: $jsonString", e)
        }
    }

    /** Очищает контекст данных. */
    fun clear() {
        dataContext = DataContext()
    }

    /**
     * Возвращает текущий контекст данных.
     *
     * @return Текущий [DataContext]
     */
    fun getDataContext(): DataContext = dataContext

    companion object {
        private const val TAG = "DataContextProvider"
        private const val MOCKS_PATH = "resources/mocks"
    }
}