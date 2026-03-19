package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.drivenui.engine.generative_screen.models.UiAction

/**
 * Модель UI-компонента изображения.
 *
 * @property modifier Modifier для Compose-компонента
 * @property modifierParams Параметры модификатора (размеры, отступы)
 * @property url URL или путь к изображению
 * @property widgetCode Уникальный код виджета
 * @property tapActions Экшены при нажатии
 * @property colorStyleCode Код стиля цвета
 * @property color Цвет для tint-фильтра
 * @property alignment Выравнивание
 * @property visibility Видимость компонента
 * @property visibilityCode Код условной видимости
 */
data class ImageModel(
    val modifier: Modifier,
    val modifierParams: ModifierParams = ModifierParams(),
    val url: String?,
    val widgetCode: String,
    val tapActions: List<UiAction>,
    val colorStyleCode: String? = null,
    val color: Color = Color.Unspecified,
    override val alignment: String,
    override val visibility: Boolean = true,
    val visibilityCode: String? = null,
) : ComponentModel