package com.example.drivenui.engine.generative_screen.models

data class ScreenState(
    val id: String,
    val definition: ScreenModel? = null,
    val data: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromDefinition(definition: ScreenModel): ScreenState {
            return ScreenState(
                id = definition.id,
                definition = definition
            )
        }
    }
}