package com.example.drivenui.engine.generative_screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.drivenui.engine.generative_screen.models.GenerativeUiState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.renderer.ComponentRenderer
import com.example.drivenui.engine.uirender.renderer.WidgetValueSetter
import com.example.drivenui.app.theme.DrivenUITheme
import kotlinx.coroutines.flow.StateFlow


@Composable
fun GenerativeScreen(
    viewModel: GenerativeScreenViewModel,
    onExit: () -> Unit = {}
) {
    GenerativeScreenUi(
        state = viewModel.uiState.collectAsStateWithLifecycle().value,
        bottomSheetState = viewModel.bottomSheetState,
        onActions = viewModel::handleActions,
        onBack = {
            val handled = viewModel.navigateBack()
            if (!handled) {
                onExit()
            }
        },
        onWidgetValueChange = viewModel::onWidgetValueChange,
        applyBindingsForComponent = viewModel::applyBindingsToComponent,
        getSheetCornerRadiusDp = viewModel::getSheetCornerRadiusDp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerativeScreenUi(
    state: GenerativeUiState,
    bottomSheetState: StateFlow<ComponentModel?>,
    onActions: (List<UiAction>) -> Unit,
    onBack: () -> Unit,
    onWidgetValueChange: WidgetValueSetter,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
    getSheetCornerRadiusDp: (ComponentModel) -> Int? = { null }
) {
    DrivenUITheme {
        BackHandler { onBack() }
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            // Основной экран
            ScreenContent(
                state = state,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent
            )

            // Нижняя шторка поверх экрана
            BottomSheetHost(
                bottomSheetState = bottomSheetState,
                onActions = onActions,
                onBack = onBack,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent,
                getSheetCornerRadiusDp = getSheetCornerRadiusDp
            )
        }
    }
}

@Composable
private fun ScreenContent(
    state: GenerativeUiState,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null
) {
    when (state) {
        is GenerativeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is GenerativeUiState.Screen -> {
            state.model?.also {
                ComponentRenderer(
                    model = it,
                    isRoot = true,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                    applyBindingsForComponent = applyBindingsForComponent
                )
            }
        }
        is GenerativeUiState.Error -> {
            // TODO: Добавить отображение ошибки
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetHost(
    bottomSheetState: StateFlow<ComponentModel?>,
    onActions: (List<UiAction>) -> Unit,
    onBack: () -> Unit,
    onWidgetValueChange: WidgetValueSetter,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)? = null,
    getSheetCornerRadiusDp: (ComponentModel) -> Int? = { null }
) {
    val bottomSheet = bottomSheetState.collectAsStateWithLifecycle().value

    bottomSheet?.also { sheetModel ->
        val configuration = LocalConfiguration.current
        val maxSheetHeight = (configuration.screenHeightDp * 0.99f).dp

        val cornerRadiusDp = getSheetCornerRadiusDp(sheetModel) ?: 0
        val sheetShape = RoundedCornerShape(
            topStart = cornerRadiusDp.dp,
            topEnd = cornerRadiusDp.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        ModalBottomSheet(
            onDismissRequest = {
                // свайп вниз/клик вне шторки считаем действием Back
                onBack()
            },
            sheetState = sheetState,
            shape = sheetShape,
            dragHandle = null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
            ) {
                ComponentRenderer(
                    model = sheetModel,
                    isRoot = false,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                    applyBindingsForComponent = applyBindingsForComponent
                )
            }
        }
    }
}
