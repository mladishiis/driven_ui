package com.example.drivenui.engine.uirender.models

import androidx.compose.runtime.Stable

@Stable
sealed interface ComponentModel {
    val alignmentStyle: String
    val visibility: Boolean get() = true
}