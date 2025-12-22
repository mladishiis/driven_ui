package com.example.drivenui.parser.models

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
    val properties: List<EventProperty> = emptyList(),
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
    val properties: List<EventProperty> = emptyList(),
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
    val properties: List<EventProperty> = emptyList(),
    val events: List<WidgetEvent> = emptyList(),
    val screenLayouts: List<ScreenLayout> = emptyList()
)