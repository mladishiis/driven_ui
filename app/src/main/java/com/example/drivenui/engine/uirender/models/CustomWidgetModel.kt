package com.example.drivenui.engine.uirender.models

data class CustomWidgetModel(
    override val alignmentStyle: String,
    override val visibility: Boolean = true
) : ComponentModel
