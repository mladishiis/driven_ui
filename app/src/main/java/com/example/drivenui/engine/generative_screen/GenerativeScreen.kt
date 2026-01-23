package com.example.drivenui.engine.generative_screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.drivenui.engine.generative_screen.models.GenerativeUiState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.renderer.ComponentRenderer
import com.example.drivenui.engine.uirender.renderer.WidgetValueSetter
import com.example.drivenui.theme.DrivenUITheme


@Composable
fun GenerativeScreen(viewModel: GenerativeScreenViewModel) {
    GenerativeScreenUi(
        state = viewModel.uiState.collectAsStateWithLifecycle().value,
        onActions = viewModel::handleActions,
        onBack = viewModel::navigateBack,
        onWidgetValueChange = viewModel::onWidgetValueChange
    )
}

@Composable
fun GenerativeScreenUi(
    state: GenerativeUiState,
    onActions: (List<UiAction>) -> Unit,
    onBack: () -> Unit,
    onWidgetValueChange: WidgetValueSetter
) {
    DrivenUITheme {
        BackHandler { onBack() }
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            when (state) {
                is GenerativeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is GenerativeUiState.Screen ->
                    state.model?.also {
                        ComponentRenderer(
                            model = it,
                            isRoot = true,
                            onActions = onActions,
                            onWidgetValueChange = onWidgetValueChange
                        )
                    }
                is GenerativeUiState.Error -> {
                    // TODO: Добавить отображение ошибки
                }
            }
        }
    }
}
