package com.example.drivenui.data

import android.content.Context
import android.util.Log
import com.example.drivenui.engine.generative_screen.binding.DataBinder
import com.example.drivenui.engine.generative_screen.binding.DataBindingParser
import com.example.drivenui.engine.generative_screen.binding.DataContextProvider
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.parser.models.BindingSourceType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestInteractor @Inject constructor(
    private val appContext: Context
) {

    private val dataContextProvider = DataContextProvider(appContext)
    private val dataBinder = DataBinder()

    fun executeQueryAndUpdateScreen(
        screenModel: ScreenModel,
        queryCode: String
    ): ScreenModel {
        Log.d(TAG, "Executing query: $queryCode for screen: ${screenModel.id}")

        val screenQuery = screenModel.requests.find { it.code == queryCode }
        if (screenQuery == null) {
            Log.w(TAG, "Query not found: $queryCode")
            return screenModel
        }

        screenQuery.mockFile?.let { fileName ->
            Log.d(TAG, "Loading mock file from ScreenQuery: $fileName for query: ${screenQuery.code}")

            try {
                // Попробуем загрузить файл через "умный" метод, который ищет и в папке mocks
                val jsonData = dataContextProvider.loadJsonSmart(fileName)

                if (jsonData != null) {
                    dataContextProvider.addScreenQueryResult(screenQuery.code, jsonData)
                    Log.d(TAG, "Successfully loaded mock data for: ${screenQuery.code}")
                } else {
                    Log.e(TAG, "Failed to load mock file: $fileName for query: ${screenQuery.code}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading mock file: $fileName", e)
            }
        }

        // 2. Убеждаемся, что JSON из биндингов компонентов загружены
        loadComponentBindingsJsonFiles(screenModel)

        // 3. Отладочная информация
        dataContextProvider.debugInfo()

        // 4. Применяем биндинги (как в processScreen)
        return dataBinder.applyBindings(
            screenModel,
            dataContextProvider.getDataContext()
        )
    }

    /**
     * Временный метод для того, чтобы имитировать применение ответов из запроса отдельно от запроса,
     * с помощью экшена рефреш
     * В будующем будет переписано как-то по-другому, когда появятся реальные запросы
     */
    fun applyBindingsToScreen(screenModel: ScreenModel): ScreenModel {
        Log.d(TAG, "Applying bindings to screen: ${screenModel.id} (without executing queries)")
        return dataBinder.applyBindings(screenModel, dataContextProvider.getDataContext())
    }

    /**
     * Загружает JSON файлы из ScreenQuery
     */
    private fun loadScreenQueryJsonFiles(screenModel: ScreenModel) {
        screenModel.requests.forEach { screenQuery ->
            // Используем mockFile из ScreenQuery для загрузки данных
            screenQuery.mockFile?.let { fileName ->
                Log.d(TAG, "Loading mock file from ScreenQuery: $fileName for query: ${screenQuery.code}")

                try {
                    val jsonString = appContext.assets.open(fileName).bufferedReader().use { it.readText() }
                    Log.d(TAG, "Loaded JSON string (${jsonString.length} chars) for: ${screenQuery.code}")

                    // Парсим JSON
                    val jsonData = com.google.gson.JsonParser.parseString(jsonString)

                    // Добавляем как SCREEN_QUERY_RESULT
                    dataContextProvider.addScreenQueryResult(screenQuery.code, jsonData)

                    Log.d(TAG, "Successfully loaded mock data for: ${screenQuery.code}")
                    Log.d(TAG, "Data structure: $jsonData (${jsonData::class.simpleName})")

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load mock file: $fileName for query: ${screenQuery.code}", e)
                }
            }
        }
    }

    /**
     * Загружает JSON файлы из биндингов компонентов
     */
    private fun loadComponentBindingsJsonFiles(screenModel: ScreenModel) {
        val jsonSources = extractJsonSourceNamesFromBindings(screenModel.rootComponent)
        Log.d(TAG, "Found JSON sources from bindings: $jsonSources")

        jsonSources.forEach { sourceName ->
            // Проверяем, не загрузили ли мы уже этот источник
            val dataContext = dataContextProvider.getDataContext()
            val alreadyLoaded = dataContext.jsonSources.containsKey(sourceName) ||
                    dataContext.screenQueryResults.containsKey(sourceName)

            if (!alreadyLoaded) {
                // Пробуем загрузить как JSON файл
                val fileName = "$sourceName.json"
                Log.d(TAG, "Looking for binding JSON file: $fileName")

                val jsonData = dataContextProvider.loadJsonSmart(fileName)
                if (jsonData != null) {
                    dataContextProvider.addJsonSource(sourceName, jsonData)
                    Log.d(TAG, "Successfully loaded JSON data for: $sourceName")
                } else {
                    Log.w(TAG, "Failed to load JSON file for binding: $fileName")
                }
            } else {
                Log.d(TAG, "Source $sourceName already loaded from ScreenQuery or other source")
            }
        }
    }

    /**
     * Извлекает имена JSON источников из биндингов компонентов
     */
    private fun extractJsonSourceNamesFromBindings(component: ComponentModel?): Set<String> {
        val sources = mutableSetOf<String>()

        fun extractFromText(text: String) {
            val bindings = DataBindingParser.parseBindings(text)
            bindings.forEach { binding ->
                when (binding.sourceType) {
                    BindingSourceType.JSON_FILE -> {
                        sources.add(binding.sourceName)
                        Log.d(TAG, "Found JSON_FILE binding: ${binding.sourceName}")
                    }
                    BindingSourceType.SCREEN_QUERY_RESULT -> {
                        Log.d(TAG, "Found SCREEN_QUERY_RESULT binding: ${binding.sourceName}")
                        // Эти источники уже должны быть загружены из ScreenQuery
                    }
                    else -> {
                        Log.d(TAG, "Found binding type: ${binding.sourceType} for source: ${binding.sourceName}")
                    }
                }
            }
        }

        fun processComponent(comp: ComponentModel?) {
            when (comp) {
                is LabelModel -> extractFromText(comp.text)
                is ButtonModel -> extractFromText(comp.text)
                is AppBarModel -> comp.title?.let { extractFromText(it) }
                is InputModel -> {
                    comp.text?.let { extractFromText(it) }
                    comp.hint?.let { extractFromText(it) }
                }
                is ImageModel -> comp.url?.let { extractFromText(it) }
                is LayoutModel -> comp.children.forEach { child -> processComponent(child) }
                else -> Unit
            }
        }

        processComponent(component)
        return sources
    }

    companion object {
        private const val TAG = "RequestInteractor"
    }
}