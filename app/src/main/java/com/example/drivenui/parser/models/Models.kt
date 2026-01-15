package com.example.drivenui.parser.models

import android.os.Parcelable
import com.google.gson.JsonElement
import kotlinx.parcelize.Parcelize

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
@Parcelize
data class ComponentProperty(
    val code: String,
    val rawValue: String,           // Исходное значение (может содержать макросы)
    val resolvedValue: String = rawValue            // Значение после подстановки
): Parcelable

/**
 * Описание биндинга данных
 */
@Parcelize
data class DataBinding(
    val sourceType: BindingSourceType,
    val sourceName: String,          // Имя источника (например, "carriers_allCarriers")
    val path: String,                // Путь к данным (например, "[0].carrierName")
    val expression: String,          // Полное выражение (например, "${carriers_allCarriers.[0].carrierName}")
    val defaultValue: String = ""
): Parcelable

/**
 * Тип источника данных
 */
enum class BindingSourceType {
    JSON_FILE,      // JSON файл из assets
    QUERY_RESULT,   // Результат запроса
    SCREEN_QUERY_RESULT, // Результат screen query (новый тип)
    APP_STATE,      // Состояние приложения
    LOCAL_VAR,      // Локальная переменная
    SCREEN_CONTEXT  // Контекст экрана
}

/**
 * Контекст данных для биндинга
 */
data class DataContext(
    val jsonSources: Map<String, JsonElement> = emptyMap(),
    val queryResults: Map<String, Any> = emptyMap(),
    val screenQueryResults: Map<String, Any> = emptyMap(), // Добавляем отдельный тип для screenQuery
    val appState: Map<String, Any> = emptyMap(),
    val localVariables: Map<String, Any> = emptyMap()
)

/**
 * Базовый компонент UI
 */
@Parcelize
sealed class Component: Parcelable {
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

//event

/**
 * Действие, выполняемое при возникновении события
 *
 * @property title Человекочитаемое название действия (может быть пустым)
 * @property code Уникальный код действия (например, "query", "openScreen")
 * @property order Порядковый номер выполнения действия относительно других действий
 * @property properties Список свойств действия
 */
@Parcelize
data class  EventAction(
    val title: String = "",
    val code: String = "",
    val order: Int = 0,
    val properties: Map<String, String> = emptyMap()
): Parcelable

/**
 * Событие, которое может произойти на экране
 *
 * @property title Человекочитаемое название события (может быть пустым)
 * @property code Уникальный код события (например, "onCreate", "onTap")
 * @property order Порядковый номер события относительно других событий
 * @property eventActions Список действий, выполняемых при возникновении события
 */
data class Event(
    val title: String = "",
    val code: String = "",
    val order: Int = 0,
    val eventActions: List<EventAction> = emptyList()
)

/**
 * Контейнер всех событий микроаппа
 *
 * @property events Список всех событий
 */
data class AllEvents(
    val events: List<Event> = emptyList()
)

/**
 * Контейнер всех действий событий микроаппа
 *
 * @property eventActions Список всех действий событий
 */
data class AllEventActions(
    val eventActions: List<EventAction>
)

/**
 * Модель микроаппа
 *
 * @property title Человекочитаемое название микроаппа
 * @property code Уникальный код микроаппа (например, "microappVTB")
 * @property shortCode Сокращенный код для deeplinks (например, "maVTB")
 * @property deeplink Базовый deeplink микроаппа
 * @property persistents Список переменных, сохраняемых между сессиями
 */
data class Microapp(
    val title: String,
    val code: String,
    val shortCode: String,
    val deeplink: String,
    val persistents: List<String>
)

/**
 * Упрощенный экран с компонентами
 */
@Parcelize
data class ParsedScreen(
    val title: String,
    val screenCode: String,
    val screenShortCode: String,
    val deeplink: String,
    val rootComponent: Component? = null,
    val requests: List<ScreenQuery> = emptyList()
): Parcelable

//queries

/**
 * Свойство запроса к API
 *
 * @property code Тип свойства (query_parameter, query_string, query_body)
 * @property variableName Имя переменной в запросе
 * @property variableValue Значение переменной (может быть константой или ссылкой на переменную)
 */
data class QueryProperty(
    val paramType: String,
    val variableName: String,
    val variableValue: String
)

/**
 * Условие выполнения запроса
 *
 * @property code Тип условия (http_response_code, variable)
 * @property value Значение условия
 */
data class QueryCondition(
    val code: String,
    val value: String
)

/**
 * Запрос к API
 *
 * @property title Человекочитаемое название запроса
 * @property code Уникальный код запроса (например, "activeProductsForMain")
 * @property type HTTP метод (GET, POST, PUT, PATCH, DELETE)
 * @property endpoint Endpoint API
 * @property properties Список свойств запроса
 */
data class Query(
    val title: String,
    val code: String,
    val type: String,
    val endpoint: String,
    val mockFile: String?,
    val properties: List<QueryProperty>,
)

/**
 * Запрос, привязанный к конкретному экрану
 *
 * @property code Уникальный код экранного запроса
 * @property screenCode Код экрана, к которому привязан запрос
 * @property queryCode Код запроса из реестра allQueries
 * @property order Порядковый номер выполнения запроса на экране
 * @property properties Список свойств запроса с конкретными значениями
 */
@Parcelize
data class ScreenQuery(
    val code: String,
    val screenCode: String,
    val queryCode: String,
    val order: Int,
    val mockFile: String? = null,
    val properties: Map<String, String>,
): Parcelable

//screen

/**
 * Виджет, размещенный в лэйауте экрана
 *
 * @property title Человекочитаемое название виджета
 * @property screenLayoutWidgetCode Уникальный код виджета на экране
 * @property widgetCode Код виджета из реестра allWidgets
 * @property properties Список свойств виджета
 * @property styles Список стилей виджета
 * @property events Список событий виджета
 * @property bindingProperties Список свойств для байндинга данных
 */
data class ScreenLayoutWidget(
    val title: String,
    val screenLayoutWidgetCode: String,
    val widgetCode: String,
    val properties: Map<String, String> = emptyMap(),
    val styles: List<WidgetStyle> = emptyList(),
    val events: List<WidgetEvent> = emptyList(),
    val bindingProperties: List<String> = emptyList()
)

/**
 * Лэйаут на экране (может содержать вложенные лэйауты и виджеты)
 *
 * @property title Человекочитаемое название лэйаута
 * @property screenLayoutCode Уникальный код лэйаута на экране
 * @property layoutCode Код типа лэйаута ("vertical", "horizontal", "layer")
 * @property screenLayoutIndex Порядковый индекс лэйаута на экране
 * @property forIndexName Имя переменной индекса для циклов (если лэйаут в цикле)
 * @property properties Список свойств лэйаута
 * @property styles Список стилей лэйаута
 * @property children Список дочерних лэйаутов
 * @property widgets Список виджетов в лэйауте
 */
data class ScreenLayout(
    val title: String,
    val screenLayoutCode: String,
    val layoutCode: String,
    val screenLayoutIndex: Int,
    val forIndexName: String?,
    val properties: Map<String, String> = emptyMap(),
    val styles: List<WidgetStyle> = emptyList(),
    val children: List<ScreenLayout> = emptyList(),
    val widgets: List<ScreenLayoutWidget> = emptyList()
)

/**
 * Экран микроаппа
 *
 * @property title Человекочитаемое название экрана
 * @property screenCode Уникальный код экрана (например, "main", "selectedProductDetails")
 * @property screenShortCode Сокращенный код для deeplinks (например, "m", "spd")
 * @property deeplink Deeplink экрана
 * @property properties Список свойств экрана
 * @property events Список событий экрана
 * @property screenLayouts Список корневых лэйаутов экрана
 */
data class Screen(
    val title: String,
    val screenCode: String,
    val screenShortCode: String,
    val deeplink: String,
    val properties: Map<String, String> = emptyMap(),
    val events: List<WidgetEvent> = emptyList(),
    val screenLayouts: List<ScreenLayout> = emptyList()
)

//styles

/**
 * Стиль текста
 *
 * @property code Уникальный код стиля текста (например, "headlineM", "bodyS.tight.normal")
 * @property fontFamily Семейство шрифтов (headline/body/display)
 * @property fontSize Размер шрифта в sp
 * @property fontWeight Толщина шрифта (400 - normal, 500 - medium, 700 - bold)
 */
@Parcelize
data class TextStyle(
    val code: String,
    val fontFamily: String,
    val fontSize: Int,
    val fontWeight: Int
): Parcelable

/**
 * Цветовая тема для светлой или темной темы
 *
 * @property color Цвет в формате HEX (например, "#1D72FF")
 * @property opacity Прозрачность в процентах (0-100, по умолчанию 100)
 */
@Parcelize
data class ColorTheme(
    val color: String,
    val opacity: Int = 100
): Parcelable

/**
 * Стиль цвета с поддержкой светлой и темной темы
 *
 * @property code Уникальный код стиля цвета (например, "semantic/text/primary")
 * @property lightTheme Цвет для светлой темы
 * @property darkTheme Цвет для темной темы
 */
@Parcelize
data class ColorStyle(
    val code: String,
    val lightTheme: ColorTheme,
    val darkTheme: ColorTheme
): Parcelable

/**
 * Стиль выравнивания
 *
 * @property code Код выравнивания (например, "AlignLeft", "AlignCenter")
 */
@Parcelize
data class AlignmentStyle(
    val code: String
): Parcelable

/**
 * Стиль отступов
 *
 * @property code Уникальный код отступа в формате "padding[left]-[top]-[right]-[bottom]"
 * @property paddingLeft Отступ слева в пикселях
 * @property paddingTop Отступ сверху в пикселях
 * @property paddingRight Отступ справа в пикселях
 * @property paddingBottom Отступ снизу в пикселях
 */
@Parcelize
data class PaddingStyle(
    val code: String,
    val paddingLeft: Int,
    val paddingTop: Int,
    val paddingRight: Int,
    val paddingBottom: Int
): Parcelable

/**
 * Стиль скругления углов
 *
 * @property code Уникальный код скругления в формате "radius[значение]"
 * @property radiusValue Значение радиуса скругления в пикселях
 */
@Parcelize
data class RoundStyle(
    val code: String,
    val radiusValue: Int
): Parcelable

/**
 * Контейнер всех стилей микроаппа
 *
 * @property textStyles Список стилей текста
 * @property colorStyles Список стилей цвета
 * @property alignmentStyles Список стилей выравнивания
 * @property paddingStyles Список стилей отступов
 * @property roundStyles Список стилей скругления
 */
@Parcelize
data class AllStyles(
    val textStyles: List<TextStyle>,
    val colorStyles: List<ColorStyle>,
    val alignmentStyles: List<AlignmentStyle>,
    val paddingStyles: List<PaddingStyle>,
    val roundStyles: List<RoundStyle>
): Parcelable

//widgets
/**
 * Стиль виджета или лэйаута
 *
 * @property code Тип стиля (textStyle, colorStyle, alignmentStyle, paddingStyle, roundStyle)
 * @property value Значение стиля (код конкретного стиля из реестра)
 */
@Parcelize
data class WidgetStyle(
    val code: String = "",
    val value: String = ""
): Parcelable

/**
 * Событие виджета с привязанными действиями
 *
 * @property eventCode Код события (например, "onTap", "onFocus")
 * @property order Порядковый номер события
 * @property eventActions Список действий, выполняемых при событии
 */
@Parcelize
data class WidgetEvent(
    val eventCode: String = "",
    val order: Int = 0,
    val eventActions: List<EventAction> = emptyList()
): Parcelable

/**
 * Виджет - нативный или составной UI-компонент
 *
 * @property title Человекочитаемое название виджета
 * @property code Уникальный код виджета (например, "image", "label", "button")
 * @property type Тип виджета ("native" для нативных компонентов)
 * @property properties Список свойств виджета
 * @property styles Список стилей виджета
 * @property events Список событий виджета
 * @property bindingProperties Список свойств для байндинга данных (динамические значения)
 */
data class Widget(
    val title: String = "",
    val code: String = "",
    val type: String = "",
    val properties: Map<String, String> = emptyMap(),
    val styles: List<WidgetStyle> = emptyList(),
    val events: List<WidgetEvent> = emptyList(),
    val bindingProperties: List<String> = emptyList()
)

/**
 * Лэйаут - контейнер для виджетов и других лэйаутов
 *
 * @property title Человекочитаемое название лэйаута
 * @property code Уникальный код лэйаута ("vertical", "horizontal", "layer")
 * @property properties Список свойств лэйаута (видимость, приоритет и т.д.)
 */
data class Layout(
    val title: String = "",
    val code: String = "",
    val properties: Map<String, String> = emptyMap(),
)