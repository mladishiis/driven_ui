package com.example.drivenui.parser.binding

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Загрузчик JSON данных
 */
class JsonDataLoader(private val context: Context) {

    /**
     * Загружает несколько JSON файлов
     */
    fun loadJsonFiles(fileNames: List<String>): Map<String, Any> {
        val jsonData = mutableMapOf<String, Any>()

        fileNames.forEach { fileName ->
            try {
                val data = loadJsonData(fileName)
                if (data != null) {
                    // Используем имя файла без расширения как ключ
                    val key = fileName.removeSuffix(".json")
                    jsonData[key] = data
                    Log.d("JsonDataLoader", "Загружен JSON файл: $fileName -> $key")
                }
            } catch (e: Exception) {
                Log.e("JsonDataLoader", "Ошибка загрузки файла $fileName", e)
            }
        }

        return jsonData
    }

    /**
     * Загружает один JSON файл (поддерживает и JSONObject и JSONArray)
     */
    fun loadJsonData(fileName: String): Any? {
        return try {
            val inputStream = context.assets.open(fileName)
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // Удаляем пробелы и проверяем, с чего начинается
            val trimmed = jsonString.trim()

            return if (trimmed.startsWith("[")) {
                // Это JSON массив
                JSONArray(jsonString)
            } else if (trimmed.startsWith("{")) {
                // Это JSON объект
                JSONObject(jsonString)
            } else {
                Log.e("JsonDataLoader", "Некорректный JSON формат в файле: $fileName")
                null
            }
        } catch (e: Exception) {
            Log.e("JsonDataLoader", "Ошибка загрузки JSON файла: $fileName", e)
            null
        }
    }
}