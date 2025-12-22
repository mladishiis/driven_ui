package com.example.drivenui.parser.models

/**
 * Свойство запроса к API
 *
 * @property code Тип свойства (query_parameter, query_string, query_body)
 * @property variableName Имя переменной в запросе
 * @property variableValue Значение переменной (может быть константой или ссылкой на переменную)
 */
data class QueryProperty(
    val code: String,
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
 * @property conditions Список условий выполнения запроса
 */
data class Query(
    val title: String,
    val code: String,
    val type: String,
    val endpoint: String,
    val properties: List<QueryProperty>,
    val conditions: List<QueryCondition> = emptyList()
)

/**
 * Запрос, привязанный к конкретному экрану
 *
 * @property code Уникальный код экранного запроса
 * @property screenCode Код экрана, к которому привязан запрос
 * @property queryCode Код запроса из реестра allQueries
 * @property order Порядковый номер выполнения запроса на экране
 * @property properties Список свойств запроса с конкретными значениями
 * @property conditions Список условий выполнения запроса
 */
data class ScreenQuery(
    val code: String,
    val screenCode: String,
    val queryCode: String,
    val order: Int,
    val properties: List<QueryProperty>,
    val conditions: List<QueryCondition> = emptyList()
)