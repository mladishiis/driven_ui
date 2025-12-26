package com.example.drivenui.engine.generative_screen.models

sealed interface UiAction {
    data class OpenScreen(val screenCode: String) : UiAction
    data object Back : UiAction
    data object Empty: UiAction
}