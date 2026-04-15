package com.example.drivenui.engine.uirender.models

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Сериализуемые параметры для восстановления Modifier при загрузке из кэша.
 * Применяются на этапе отрисовки через [applyParams].
 *
 * @property paddingLeft Отступ слева (dp)
 * @property paddingTop Отступ сверху (dp)
 * @property paddingRight Отступ справа (dp)
 * @property paddingBottom Отступ снизу (dp)
 * @property height Высота: "fillmax", "wrapcontent" или число
 * @property width Ширина: "fillmax", "wrapcontent" или число
 */
data class ModifierParams(
    val paddingLeft: Int = 0,
    val paddingTop: Int = 0,
    val paddingRight: Int = 0,
    val paddingBottom: Int = 0,
    val height: String = "",
    val width: String = "",
) {
    /**
     * Применяет параметры (padding, height, width) к переданному Modifier.
     * Вызывается на этапе отрисовки в рендерерах.
     *
     * @param modifier Базовый Modifier
     * @return Modifier с применёнными параметрами
     */
    fun applyParams(modifier: Modifier): Modifier {
        var result: Modifier = modifier
        if (paddingLeft != 0 || paddingTop != 0 || paddingRight != 0 || paddingBottom != 0) {
            result = result.padding(
                start = paddingLeft.dp,
                top = paddingTop.dp,
                end = paddingRight.dp,
                bottom = paddingBottom.dp,
            )
        }
        result = when (height.lowercase()) {
            "fillmax" -> result.fillMaxHeight()
            "wrapcontent" -> result.wrapContentHeight()
            else -> height.toIntOrNull()?.let { result.height(it.dp) } ?: result.fillMaxHeight()
        }
        result = when (width.lowercase()) {
            "fillmax" -> result.fillMaxWidth()
            "wrapcontent" -> result.wrapContentWidth()
            else -> width.toIntOrNull()?.let { result.width(it.dp) } ?: result.fillMaxWidth()
        }
        return result
    }
}
