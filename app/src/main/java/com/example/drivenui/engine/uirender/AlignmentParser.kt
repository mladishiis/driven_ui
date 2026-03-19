package com.example.drivenui.engine.uirender

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
        "aligncenter" -> Alignment.Center
        "alignleft", "alignstart" -> Alignment.CenterStart
        "alignright", "alignend" -> Alignment.CenterEnd
        "aligntop" -> Alignment.TopCenter
        "alignbottom" -> Alignment.BottomCenter
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
        "alignleft", "alignstart", "left" -> Alignment.Start
        "alignright", "alignend", "right" -> Alignment.End
        "aligncenter", "center" -> Alignment.CenterHorizontally
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
        "aligntop", "top" -> Alignment.Top
        "alignbottom", "bottom" -> Alignment.Bottom
        "aligncenter", "center" -> Alignment.CenterVertically
        else -> Alignment.CenterVertically
    }
