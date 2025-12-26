package com.example.drivenui.engine.generative_screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.drivenui.engine.generative_screen.models.GenerativeUiState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.renderer.ComponentRenderer
import com.example.drivenui.theme.DrivenUITheme


@Composable
fun GenerativeScreen(viewModel: GenerativeScreenViewModel) {
    GenerativeScreenUi(
        state = viewModel.uiState.collectAsStateWithLifecycle().value,
        onAction = viewModel::handleAction,
        onBack = viewModel::navigateBack,
    )
}

@Composable
fun GenerativeScreenUi(
    state: GenerativeUiState,
    onAction: (UiAction) -> Unit,
    onBack: () -> Unit,
) {
    DrivenUITheme {
        BackHandler { onBack() }
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            when (state) {
                is GenerativeUiState.Screen ->
                    state.model?.also {
                        ComponentRenderer(it, onAction)
                    }
                else -> {}
            }
        }
    }
}
