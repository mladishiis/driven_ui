package com.example.drivenui.engine.uirender.models

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Сериализуемые параметры для восстановления Modifier при загрузке из кэша.
 */
data class ModifierParams(
    val paddingLeft: Int = 0,
    val paddingTop: Int = 0,
    val paddingRight: Int = 0,
    val paddingBottom: Int = 0,
    val height: String = "",
    val width: String = "",
) {
    fun toModifier(): Modifier {
        var mod = Modifier
        if (paddingLeft != 0 || paddingTop != 0 || paddingRight != 0 || paddingBottom != 0) {
            mod = mod.padding(
                start = paddingLeft.dp,
                top = paddingTop.dp,
                end = paddingRight.dp,
                bottom = paddingBottom.dp,
            )
        }
        mod = when (height.lowercase()) {
            "fillmax" -> mod.fillMaxHeight()
            "wrapcontent" -> mod.wrapContentHeight()
            else -> height.toIntOrNull()?.let { mod.height(it.dp) } ?: mod.fillMaxHeight(),
        }
        mod = when (width.lowercase()) {
            "fillmax" -> mod.fillMaxWidth()
            "wrapcontent" -> mod.wrapContentWidth()
            else -> width.toIntOrNull()?.let { mod.width(it.dp) } ?: mod.fillMaxWidth(),
        }
        return mod
    }
}
