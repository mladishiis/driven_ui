package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.UiAction

/**
 * Модель UI-компонента AppBar (шапка экрана).
 *
 * @property modifier Modifier для Compose
 * @property modifierParams параметры модификатора
 * @property title заголовок
 * @property iconLeftUrl URL иконки слева
 * @property textStyle стиль текста
 * @property textStyleCode код стиля текста
 * @property colorStyleCode код стиля цвета
 * @property tapActions экшены при нажатии
 * @property widgetCode уникальный код виджета
 * @property alignment выравнивание
 * @property visibility видимость
 * @property visibilityCode код условной видимости
 */
data class AppBarModel(
    val modifier: Modifier,
    val modifierParams: ModifierParams = ModifierParams(),
    val title: String?,
    val iconLeftUrl: String?,
    val textStyle: TextStyle = TextStyle.Default,
    val textStyleCode: String? = null,
    val colorStyleCode: String? = null,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignment: String,
    override val visibility: Boolean = true,
    val visibilityCode: String? = null,
) : ComponentModel