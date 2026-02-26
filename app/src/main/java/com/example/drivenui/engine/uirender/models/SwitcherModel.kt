package com.example.drivenui.engine.uirender.models

data class SwitcherModel(
    val checked: Boolean,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
) : ComponentModel