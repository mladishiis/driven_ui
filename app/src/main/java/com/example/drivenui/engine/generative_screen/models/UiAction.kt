package com.example.drivenui.engine.generative_screen.models

sealed interface UiAction {
    data class OpenScreen(val screenCode: String) : UiAction
    data class OpenBottomSheet(val screenCode: String) : UiAction
    data class RefreshScreen(val screenCode: String) : UiAction
    data class RefreshWidget(val widgetCode: String) : UiAction
    data class RefreshLayout(val layoutCode: String) : UiAction
    data class OpenDeeplink(val deeplink: String) : UiAction
    data class ExecuteQuery(val queryCode: String) : UiAction
    data class DataTransform(val variableName: String, val newValue: String) : UiAction
    data class SaveToContext(val valueTo: String, val valueFrom: String) : UiAction
    data class NativeCode(
        val actionCode: String,
        val parameters: Map<String, String> = emptyMap(),
    ) : UiAction
    data object Back : UiAction
    data object Empty : UiAction
}