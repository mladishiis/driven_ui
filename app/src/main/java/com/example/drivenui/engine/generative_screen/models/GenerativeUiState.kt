package com.example.drivenui.engine.generative_screen.models

import androidx.compose.runtime.Stable
import com.example.drivenui.engine.uirender.models.ComponentModel

@Stable
sealed interface GenerativeUiState {
    data object Loading : GenerativeUiState

    /**
     * @property screenId ключ экрана для сброса Compose-composition при навигации назад
     * @property model корневой компонент экрана
     */
    data class Screen(
        val screenId: String,
        val model: ComponentModel?,
    ) : GenerativeUiState

    data class Error(val message: String) : GenerativeUiState
}