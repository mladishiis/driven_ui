package com.example.drivenui.engine.generative_screen.models

import com.example.drivenui.engine.uirender.models.ComponentModel

data class ScreenModel(
    val id: String,
    // TODO: нормальный список запросов
    val requests: List<String>,
    val rootComponent: ComponentModel?
)