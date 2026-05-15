package com.example.drivenui.engine.parser.models

import android.os.Parcelable
import com.google.gson.JsonElement
import kotlinx.parcelize.Parcelize

/**
 * Тип компонента в дереве UI.
 *
 * @property LAYOUT Базовый лэйаут (vertical, horizontal, layers)
 * @property WIDGET Виджет (button, label, image и т.д.)
 * @property SCREEN Экран
 */
enum class ComponentType {
    LAYOUT,
    WIDGET,
    SCREEN,
}

/**
 * Описание биндинга данных.
 *
 * @property sourceType Тип источника
 * @property sourceName Имя источника (например, "carriers_allCarriers")
 * @property path Путь к данным (например, "[0].carrierName")
 * @property expression Полное выражение (например, "${carriers_allCarriers.[0].carrierName}")
 * @property defaultValue Значение по умолчанию
 */
@Parcelize
data class DataBinding(
    val sourceType: BindingSourceType,
    val sourceName: String,
    val path: String,
    val expression: String,
    val defaultValue: String = "",
): Parcelable

/**
 * Тип источника данных для биндинга.
 *
 * @property JSON_FILE JSON-файл из assets
 * @property QUERY_RESULT Результат запроса
 * @property SCREEN_QUERY_RESULT Результат screen query
 * @property APP_STATE Состояние приложения
 * @property LOCAL_VAR Локальная переменная
 * @property SCREEN_CONTEXT Контекст экрана
 */
enum class BindingSourceType {
    JSON_FILE,
    QUERY_RESULT,
    SCREEN_QUERY_RESULT,
    APP_STATE,
    LOCAL_VAR,
    SCREEN_CONTEXT,
}

/**
 * Контекст данных для биндинга.
 *
 * @property jsonSources JSON-источники
 * @property queryResults Результаты запросов
 * @property screenQueryResults Результаты screen query
 * @property appState Состояние приложения
 * @property localVariables Локальные переменные
 */
data class DataContext(
    val jsonSources: Map<String, JsonElement> = emptyMap(),
    val queryResults: Map<String, Any> = emptyMap(),
    val screenQueryResults: Map<String, Any> = emptyMap(),
    val appState: Map<String, Any> = emptyMap(),
    val localVariables: Map<String, Any> = emptyMap(),
)

/**
 * Базовый компонент UI в дереве парсинга.
 */
@Parcelize
sealed class Component : Parcelable {
    abstract val title: String
    abstract val code: String
    abstract val properties: Map<String, String>
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
    val maxForIndex: String? = null,
    override val properties: Map<String, String> = emptyMap(),
    override val styles: List<WidgetStyle> = emptyList(),
    override val events: List<WidgetEvent> = emptyList(),
    override val children: List<Component> = emptyList(),
    override val bindingProperties: List<String> = emptyList(),
    override val type: ComponentType = ComponentType.LAYOUT,
    override val index: Int = 0,
    override val forIndexName: String? = null,
) : Component()

/**
 * Компонент виджета (листовой элемент)
 */
data class WidgetComponent(
    override val title: String,
    override val code: String,
    val widgetCode: String,
    val widgetType: String,
    override val properties: Map<String, String> = emptyMap(),
    override val styles: List<WidgetStyle> = emptyList(),
    override val events: List<WidgetEvent> = emptyList(),
    override val children: List<Component> = emptyList(),
    override val bindingProperties: List<String> = emptyList(),
    override val type: ComponentType = ComponentType.WIDGET,
    override val index: Int = 0,
    override val forIndexName: String? = null,
) : Component()

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
    val properties: Map<String, String> = emptyMap(),
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
    val eventActions: List<EventAction> = emptyList(),
)

/**
 * Контейнер всех событий микроаппа
 *
 * @property events Список всех событий
 */
data class AllEvents(
    val events: List<Event> = emptyList(),
)

/**
 * Контейнер всех действий событий микроаппа
 *
 * @property eventActions Список всех действий событий
 */
data class AllEventActions(
    val eventActions: List<EventAction>,
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
    val persistents: List<String>,
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
    val events: List<WidgetEvent> = emptyList(),
): Parcelable

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
    val fontWeight: Int,
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
    val opacity: Int = 100,
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
    val darkTheme: ColorTheme,
): Parcelable

/**
 * Стиль выравнивания
 *
 * @property code Код выравнивания (например, "AlignLeft", "AlignCenter")
 */
@Parcelize
data class AlignmentStyle(
    val code: String,
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
    val paddingBottom: Int,
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
    val radiusValue: Int,
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
    val roundStyles: List<RoundStyle>,
): Parcelable

/**
 * Стиль виджета или лэйаута
 *
 * @property code Тип стиля (textStyle, colorStyle, alignmentStyle, paddingStyle, roundStyle, roundStyleTop, roundStyleBottom)
 * @property value Значение стиля (код конкретного стиля из реестра)
 */
@Parcelize
data class WidgetStyle(
    val code: String = "",
    val value: String = "",
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
    val eventActions: List<EventAction> = emptyList(),
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
    val bindingProperties: List<String> = emptyList(),
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
