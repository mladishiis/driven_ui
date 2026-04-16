package com.example.drivenui.engine.uirender.utils

import androidx.compose.ui.Alignment

/**
 * Парсит значение property alignment в Compose [Alignment] для Box (layers).
 *
 * Поддерживает: topLeft, topCenter, topRight, left, center, right,
 * bottomLeft, bottomCenter, bottomRight; и legacy alignLeft, alignCenter и т.д.
 *
 * @param value значение property alignment
 * @return Alignment для Modifier.align внутри Box
 */
fun parseBoxAlignment(value: String): Alignment =
    when (value.trim().lowercase()) {
        "topleft" -> Alignment.TopStart
        "topcenter" -> Alignment.TopCenter
        "topright" -> Alignment.TopEnd
        "left" -> Alignment.CenterStart
        "center" -> Alignment.Center
        "right" -> Alignment.CenterEnd
        "bottomleft" -> Alignment.BottomStart
        "bottomcenter" -> Alignment.BottomCenter
        "bottomright" -> Alignment.BottomEnd
        else -> Alignment.Center
    }

/**
 * Парсит значение alignment в [Alignment.Horizontal] для детей Column.
 *
 * @param value значение property alignment
 * @return Alignment.Horizontal для Modifier.align внутри Column
 */
fun parseColumnAlignment(value: String): Alignment.Horizontal =
    when (value.trim().lowercase()) {
        "left" -> Alignment.Start
        "right" -> Alignment.End
        "center" -> Alignment.CenterHorizontally
        else -> Alignment.CenterHorizontally
    }

/**
 * Парсит значение alignment в [Alignment.Vertical] для детей Row.
 *
 * @param value значение property alignment
 * @return Alignment.Vertical для Modifier.align внутри Row
 */
fun parseRowAlignment(value: String): Alignment.Vertical =
    when (value.trim().lowercase()) {
        "topcenter" -> Alignment.Top
        "bottomcenter" -> Alignment.Bottom
        "center" -> Alignment.CenterVertically
        else -> Alignment.CenterVertically
    }
