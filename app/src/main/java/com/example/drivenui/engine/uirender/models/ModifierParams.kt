package com.example.drivenui.engine.uirender.models

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
 * @property widthFillFraction Доля ширины родителя (0..1) при [width] = fillMax; из widthPercentage / 100
 * @property heightFillFraction Доля высоты родителя (0..1) при [height] = fillMax; из heightPercentage / 100
 * @property scrollable Скроллируемость (вычисляется автоматически для vertical+fillMax)
 */
data class ModifierParams(
    val paddingLeft: Int = 0,
    val paddingTop: Int = 0,
    val paddingRight: Int = 0,
    val paddingBottom: Int = 0,
    val height: String = "",
    val width: String = "",
    val widthFillFraction: Float? = null,
    val heightFillFraction: Float? = null,
    val scrollable: Boolean = false,
) {
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
        result = applyHeightModifier(result)
        result = applyWidthModifier(result)
        return result
    }

    /**
     * Применяет padding и width без height.
     * Используется в ColumnRenderer — fillMaxHeight конфликтует с verticalScroll.
     */
    fun applyParamsExcludingHeight(modifier: Modifier): Modifier {
        var result: Modifier = modifier
        if (paddingLeft != 0 || paddingTop != 0 || paddingRight != 0 || paddingBottom != 0) {
            result = result.padding(
                start = paddingLeft.dp,
                top = paddingTop.dp,
                end = paddingRight.dp,
                bottom = paddingBottom.dp,
            )
        }
        result = applyWidthModifier(result)
        return result
    }

    private fun applyWidthModifier(modifier: Modifier): Modifier = when (width.lowercase()) {
        "fillmax" -> widthFillFraction?.let { modifier.fillMaxWidth(it) } ?: modifier.fillMaxWidth()
        "wrapcontent" -> modifier.wrapContentWidth()
        else -> width.toIntOrNull()?.let { modifier.width(it.dp) } ?: modifier.fillMaxWidth()
    }

    private fun applyHeightModifier(modifier: Modifier): Modifier = when (height.lowercase()) {
        "fillmax" -> heightFillFraction?.let { modifier.fillMaxHeight(it) } ?: modifier.fillMaxHeight()
        "wrapcontent" -> modifier.wrapContentHeight()
        else -> height.toIntOrNull()?.let { modifier.height(it.dp) } ?: modifier.fillMaxHeight()
    }

    /**
     * Масштабирование изображения под заданные width/height.
     *
     * При `fillMax` по оси картинка заполняет выделенный размер родителя, даже если искажается пропорция.
     * По умолчанию для [androidx.compose.foundation.Image] — [ContentScale.Fit], из‑за чего fillMax не даёт растяжения.
     */
    fun imageContentScale(): ContentScale {
        val widthFillsParent = width.isFillMaxLike()
        val heightFillsParent = height.isFillMaxLike()
        val widthIsFixed = width.toIntOrNull() != null
        val heightIsFixed = height.toIntOrNull() != null

        return when {
            widthFillsParent && (heightFillsParent || heightIsFixed) -> ContentScale.FillBounds
            heightFillsParent && widthIsFixed -> ContentScale.FillBounds
            widthFillsParent -> ContentScale.FillWidth
            heightFillsParent -> ContentScale.FillHeight
            else -> ContentScale.Fit
        }
    }

    private fun String.isFillMaxLike(): Boolean =
        isBlank() || equals("fillmax", ignoreCase = true)
}
