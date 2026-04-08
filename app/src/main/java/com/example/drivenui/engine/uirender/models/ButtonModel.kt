package com.example.drivenui.engine.uirender.models

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.example.drivenui.engine.generative_screen.models.UiAction

/**
 * Модель UI-компонента кнопки.
 *
 * @property modifier Modifier для Compose
 * @property modifierParams параметры модификатора
 * @property enabled активна ли кнопка
 * @property text шаблон текста из XML
 * @property displayText результат полного резолва для отрисовки
 * @property cornerRadius резолвенные значения радиуса скругления (заполняет StyleResolver)
 * @property textStyle стиль текста
 * @property backgroundColor цвет фона
 * @property radiusValues значения radius, radiusTop, radiusBottom из properties (резолвятся в cornerRadius)
 * @property textStyleCode код стиля текста
 * @property colorStyleCode код стиля цвета текста
 * @property backgroundColorStyleCode код стиля фона
 * @property tapActions экшены при нажатии
 * @property widgetCode уникальный код виджета
 * @property alignment выравнивание виджета
 * @property textAlignment выравнивание текста (alignLeft, alignCenter, alignRight)
 * @property visibility видимость
 * @property visibilityCode код условной видимости
 */
data class ButtonModel(
    val modifier: Modifier,
    val modifierParams: ModifierParams = ModifierParams(),
    val enabled: Boolean,
    val text: String,
    val displayText: String? = null,
    val cornerRadius: CornerRadius = CornerRadius(),
    val textStyle: TextStyle = TextStyle.Default,
    val backgroundColor: Color = Color.Black,
    val radiusValues: RadiusValues = RadiusValues(),
    val textStyleCode: String? = null,
    val colorStyleCode: String? = null,
    val backgroundColorStyleCode: String? = null,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignment: String,
    val textAlignment: String = "alignCenter",
    override val visibility: Boolean = true,
    val visibilityCode: String? = null,
) : ComponentModel
