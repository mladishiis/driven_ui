package com.example.drivenui.engine.generative_screen.models

data class ScreenState(
    val id: String,
    val definition: ScreenModel? = null,
    val sourceDefinition: ScreenModel? = definition,
    val data: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
) {
    companion object {
        fun fromDefinition(
            definition: ScreenModel,
            sourceDefinition: ScreenModel = definition,
        ): ScreenState {
            return ScreenState(
                id = definition.id,
                definition = definition,
                sourceDefinition = sourceDefinition,
            )
        }
    }
}