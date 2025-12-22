package com.example.drivenui.parser.models

/**
 * Стиль виджета или лэйаута
 *
 * @property code Тип стиля (textStyle, colorStyle, alignmentStyle, paddingStyle, roundStyle)
 * @property value Значение стиля (код конкретного стиля из реестра)
 */
data class WidgetStyle(
    val code: String = "",
    val value: String = ""
)

/**
 * Событие виджета с привязанными действиями
 *
 * @property eventCode Код события (например, "onTap", "onFocus")
 * @property order Порядковый номер события
 * @property eventActions Список действий, выполняемых при событии
 */
data class WidgetEvent(
    val eventCode: String = "",
    val order: Int = 0,
    val eventActions: List<EventAction> = emptyList()
)

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
    val properties: List<EventProperty> = emptyList(),
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
    val properties: List<EventProperty> = emptyList()
)