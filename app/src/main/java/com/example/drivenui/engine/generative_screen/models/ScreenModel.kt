package com.example.drivenui.engine.generative_screen.models

import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.parser.models.ScreenQuery

data class ScreenModel(
    val id: String,
    val requests: List<ScreenQuery>,
    val rootComponent: ComponentModel?
)