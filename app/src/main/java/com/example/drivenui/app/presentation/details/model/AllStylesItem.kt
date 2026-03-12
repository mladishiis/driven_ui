package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения всех стилей.
 *
 * @property textStylesCount количество текстовых стилей
 * @property colorStylesCount количество цветовых стилей
 * @property roundStylesCount количество стилей скругления
 * @property paddingStylesCount количество стилей отступов
 * @property alignmentStylesCount количество стилей выравнивания
 */
data class AllStylesItem(
    val textStylesCount: Int,
    val colorStylesCount: Int,
    val roundStylesCount: Int,
    val paddingStylesCount: Int,
    val alignmentStylesCount: Int
)
