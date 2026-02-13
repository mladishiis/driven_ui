package com.example.drivenui.engine.generative_screen.models

import androidx.compose.runtime.Stable
import com.example.drivenui.engine.uirender.models.ComponentModel

@Stable
sealed interface GenerativeUiState {
    data object Loading : GenerativeUiState
    data class Screen(val model: ComponentModel?) : GenerativeUiState
    data class Error(val message: String) : GenerativeUiState
}