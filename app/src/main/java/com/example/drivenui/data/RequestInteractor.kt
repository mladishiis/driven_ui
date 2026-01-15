package com.example.drivenui.data

import android.content.Context
import android.util.Log
import com.example.drivenui.engine.generative_screen.binding.DataBinder
import com.example.drivenui.engine.generative_screen.binding.DataBindingParser
import com.example.drivenui.engine.generative_screen.binding.DataContextProvider
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.parser.models.Component
import com.example.drivenui.parser.models.ComponentProperty
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestInteractor @Inject constructor(
    private val appContext: Context
) {

    private val dataContextProvider = DataContextProvider(appContext)
    private val dataBinder = DataBinder()

    /**
     * Обрабатывает экран, загружает данные и применяет биндинги
     */
    fun processScreen(
        screenModel: ScreenModel,
        preloadedData: Map<String, Any> = emptyMap()
    ): ScreenModel {
        Log.d(TAG, "Processing screen: ${screenModel.id}")

        // 1. Загружаем необходимые JSON файлы из assets
        loadRequiredJsonFiles(screenModel)

        // 2. Добавляем предзагруженные данные
        preloadedData.forEach { (key, value) ->
            dataContextProvider.addQueryResult(key, value)
        }

        // 3. Получаем контекст и логируем его
        val dataContext = dataContextProvider.getDataContext()
        Log.d(TAG, "DataContext: ${dataContext.jsonSources.keys}")

        // 4. Применяем биндинги данных
        return dataBinder.applyBindings(screenModel, dataContext)
    }

    /**
     * Обрабатывает компонент парсера для извлечения биндингов
     */
    fun processParsedComponent(component: Component): Component {
        return when (component) {
            is com.example.drivenui.parser.models.LayoutComponent -> {
                processParsedLayoutComponent(component)
            }
            is com.example.drivenui.parser.models.WidgetComponent -> {
                processParsedWidgetComponent(component)
            }
            else -> component
        }
    }

    /**
     * Загружает необходимые JSON файлы, упомянутые в биндингах
     */
    private fun loadRequiredJsonFiles(screenModel: ScreenModel) {
        Log.d(TAG, "Loading required JSON files")

        val jsonSources = extractJsonSourceNames(screenModel.rootComponent)
        Log.d(TAG, "Found JSON sources: $jsonSources")

        jsonSources.forEach { sourceName ->
            Log.d(TAG, "Processing source: $sourceName")

            // Если источник заканчивается на _list (например, carriers_list)
            // ищем базовое имя (carriers) для загрузки файла
            val baseSourceName = if (sourceName.endsWith("_list")) {
                sourceName.removeSuffix("_list")
            } else {
                sourceName
            }

            Log.d(TAG, "Base source name: $baseSourceName")

            // Пробуем загрузить как JSON файл
            val fileName = "$baseSourceName.json"
            Log.d(TAG, "Looking for file: $fileName")

            val jsonData = dataContextProvider.loadJsonFromAssets(fileName)
            if (jsonData != null) {
                Log.d(TAG, "Successfully loaded JSON data for: $baseSourceName")
                dataContextProvider.addJsonSource(baseSourceName, jsonData)
            } else {
                Log.e(TAG, "Failed to load JSON file: $fileName")
            }
        }
    }

    /**
     * Рекурсивно извлекает имена JSON источников из компонентов
     */
    private fun extractJsonSourceNames(component: ComponentModel?): Set<String> {
        val sources = mutableSetOf<String>()

        fun extractFromText(text: String) {
            val bindings = DataBindingParser.parseBindings(text)
            bindings.forEach { binding ->
                if (binding.sourceType == com.example.drivenui.parser.models.BindingSourceType.JSON_FILE) {
                    sources.add(binding.sourceName)
                }
            }
        }

        fun processComponent(comp: ComponentModel?) {
            when (comp) {
                is com.example.drivenui.engine.uirender.models.LabelModel -> {
                    extractFromText(comp.text)
                }
                is com.example.drivenui.engine.uirender.models.ButtonModel -> {
                    extractFromText(comp.text)
                }
                is com.example.drivenui.engine.uirender.models.AppBarModel -> {
                    comp.title?.let { extractFromText(it) }
                }
                is com.example.drivenui.engine.uirender.models.InputModel -> {
                    extractFromText(comp.text)
                    extractFromText(comp.hint)
                }
                is com.example.drivenui.engine.uirender.models.ImageModel -> {
                    comp.url?.let { extractFromText(it) }
                }
                is com.example.drivenui.engine.uirender.models.LayoutModel -> {
                    comp.children.forEach { child ->
                        processComponent(child)
                    }
                }
                else -> {}
            }
        }

        processComponent(component)
        return sources
    }

    private fun processParsedLayoutComponent(
        component: com.example.drivenui.parser.models.LayoutComponent
    ): com.example.drivenui.parser.models.LayoutComponent {
        val processedProperties = component.properties.map { property ->
            processComponentProperty(property)
        }

        val processedChildren = component.children.map { child ->
            processParsedComponent(child)
        }

        return component.copy(
            properties = processedProperties,
            children = processedChildren
        )
    }

    private fun processParsedWidgetComponent(
        component: com.example.drivenui.parser.models.WidgetComponent
    ): com.example.drivenui.parser.models.WidgetComponent {
        val processedProperties = component.properties.map { property ->
            processComponentProperty(property)
        }

        return component.copy(
            properties = processedProperties
        )
    }

    private fun processComponentProperty(property: ComponentProperty): ComponentProperty {
        val bindings = com.example.drivenui.engine.generative_screen.binding.DataBindingParser.parseBindings(property.rawValue)

        return if (bindings.isNotEmpty()) {
            property.copy(bindings = bindings)
        } else {
            property
        }
    }

    /**
     * Очищает кэш данных
     */
    fun clearCache() {
        dataContextProvider.clear()
    }

    /**
     * Получает данные по имени источника
     */
    fun getData(sourceName: String): Any? {
        val context = dataContextProvider.getDataContext()
        return context.jsonSources[sourceName]
            ?: context.queryResults[sourceName]
            ?: context.screenQueryResults[sourceName]
    }

    companion object {
        private const val TAG = "RequestInteractor"
    }
}