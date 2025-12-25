package com.example.drivenui.parser.models

import org.json.JSONArray

/**
 * Тип компонента
 */
enum class ComponentType {
    SCREEN_LAYOUT,  // Контейнерный лэйаут на экране
    LAYOUT,         // Базовый лэйаут (vertical, horizontal, layers)
    WIDGET,         // Виджет (button, label, image и т.д.)
    SCREEN          // Экран
}

/**
 * Свойство компонента с поддержкой биндингов
 */
data class ComponentProperty(
    val code: String,
    val rawValue: String,           // Исходное значение (может содержать макросы)
    val bindings: List<DataBinding> = emptyList(),  // Извлеченные биндинги
    val resolvedValue: String = rawValue            // Значение после подстановки
) {
    val hasBindings: Boolean get() = bindings.isNotEmpty()
}

/**
 * Описание биндинга данных
 */
data class DataBinding(
    val sourceType: BindingSourceType,
    val sourceName: String,          // Имя источника (например, "carriers_allCarriers")
    val path: String,                // Путь к данным (например, "[0].carrierName")
    val expression: String,          // Полное выражение (например, "${carriers_allCarriers.[0].carrierName}")
    val defaultValue: String = ""
)

/**
 * Тип источника данных
 */
enum class BindingSourceType {
    JSON_FILE,      // JSON файл из assets
    QUERY_RESULT,   // Результат запроса
    APP_STATE,      // Состояние приложения
    LOCAL_VAR,      // Локальная переменная
    SCREEN_CONTEXT  // Контекст экрана
}

/**
 * Контекст данных для биндинга
 */
data class DataContext(
    val jsonSources: Map<String, JSONArray> = emptyMap(),
    val queryResults: Map<String, Any> = emptyMap(),
    val appState: Map<String, Any> = emptyMap(),
    val localVariables: Map<String, Any> = emptyMap()
)

/**
 * Базовый компонент UI
 */
sealed class Component {
    abstract val title: String
    abstract val code: String
    abstract val properties: List<ComponentProperty>  // Теперь ComponentProperty
    abstract val styles: List<WidgetStyle>
    abstract val events: List<WidgetEvent>
    abstract val children: List<Component>
    abstract val bindingProperties: List<String>
    abstract val type: ComponentType
    abstract val index: Int
    abstract val forIndexName: String?
}

/**
 * Компонент лэйаута (контейнер)
 */
data class LayoutComponent(
    override val title: String,
    override val code: String,
    val layoutCode: String,
    override val properties: List<ComponentProperty> = emptyList(),  // ComponentProperty
    override val styles: List<WidgetStyle> = emptyList(),
    override val events: List<WidgetEvent> = emptyList(),
    override val children: List<Component> = emptyList(),
    override val bindingProperties: List<String> = emptyList(),
    override val type: ComponentType = ComponentType.LAYOUT,
    override val index: Int = 0,
    override val forIndexName: String? = null
) : Component()

/**
 * Компонент виджета (листовой элемент)
 */
data class WidgetComponent(
    override val title: String,
    override val code: String,
    val widgetCode: String,
    val widgetType: String,
    override val properties: List<ComponentProperty> = emptyList(),  // ComponentProperty
    override val styles: List<WidgetStyle> = emptyList(),
    override val events: List<WidgetEvent> = emptyList(),
    override val children: List<Component> = emptyList(),
    override val bindingProperties: List<String> = emptyList(),
    override val type: ComponentType = ComponentType.WIDGET,
    override val index: Int = 0,
    override val forIndexName: String? = null
) : Component()