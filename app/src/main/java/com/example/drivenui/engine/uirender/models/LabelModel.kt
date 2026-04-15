package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.UiAction

/**
 * Модель UI-компонента текстовой метки.
 *
 * @property modifier Modifier для Compose
 * @property modifierParams параметры модификатора
 * @property text шаблон текста из XML (может содержать `${}`, `@{...}`, `@@{...}`)
 * @property displayText результат полного резолва для отрисовки; null до первого `resolveScreen`
 * @property widgetCode уникальный код виджета
 * @property textStyle стиль текста
 * @property textStyleCode код стиля текста
 * @property colorStyleCode код стиля цвета
 * @property tapActions экшены при нажатии
 * @property alignment выравнивание виджета
 * @property textAlignment выравнивание текста (alignLeft, alignCenter, alignRight)
 * @property visibility видимость
 * @property visibilityCode код условной видимости
 */
data class LabelModel(
    val modifier: Modifier,
    val modifierParams: ModifierParams = ModifierParams(),
    val text: String,
    val displayText: String? = null,
    val widgetCode: String,
    val textStyle: TextStyle = TextStyle.Default,
    val textStyleCode: String? = null,
    val colorStyleCode: String? = null,
    val tapActions: List<UiAction>,
    override val alignment: String,
    val textAlignment: String = "alignLeft",
    override val visibility: Boolean = true,
    val visibilityCode: String? = null,
) : ComponentModel