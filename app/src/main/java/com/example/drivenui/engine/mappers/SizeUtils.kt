package com.example.drivenui.engine.mappers

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun Modifier.applyWidth(width: String): Modifier {
    return when (width.lowercase()) {
        "fillmax" -> fillMaxWidth()

        "wrapcontent" -> wrapContentWidth()

        else -> {
            val widthInDp = width.toIntOrNull()
            if (widthInDp != null) {
                width(widthInDp.dp)
            } else {
                fillMaxWidth()
            }
        }
    }
}

fun Modifier.applyHeight(height: String): Modifier {
    return when (height.lowercase()) {
        "fillmax" -> fillMaxHeight()

        "wrapcontent" -> wrapContentHeight()

        else -> {
            val heightInDp = height.toIntOrNull()
            if (heightInDp != null) {
                height(heightInDp.dp)
            } else {
                fillMaxHeight()
            }
        }
    }
}