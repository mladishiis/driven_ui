package com.example.drivenui.parser.models

/**
 * Свойство события или действия
 *
 * @property code Код свойства (ключ)
 * @property value Значение свойства
 */
data class EventProperty(
    val code: String,
    val value: String
)

/**
 * Действие, выполняемое при возникновении события
 *
 * @property title Человекочитаемое название действия (может быть пустым)
 * @property code Уникальный код действия (например, "query", "openScreen")
 * @property order Порядковый номер выполнения действия относительно других действий
 * @property properties Список свойств действия
 */
data class  EventAction(
    val title: String = "",
    val code: String = "",
    val order: Int = 0,
    val properties: List<EventProperty> = emptyList()
)

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