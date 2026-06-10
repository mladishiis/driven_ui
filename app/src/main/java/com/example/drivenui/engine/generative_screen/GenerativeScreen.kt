package com.example.drivenui.engine.generative_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.drivenui.app.theme.DrivenUITheme
import com.example.drivenui.engine.generative_screen.models.GenerativeUiState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.renderer.ComponentRenderer
import com.example.drivenui.engine.uirender.renderer.LocalIsDarkTheme
import com.example.drivenui.engine.uirender.renderer.LocalStyleRegistry
import com.example.drivenui.engine.uirender.utils.WidgetValueSetter
import kotlinx.coroutines.flow.StateFlow

/**
 * Главный экран генеративного UI с поддержкой нижней шторки.
 *
 * @param state состояние UI (Loading / Screen / Error)
 * @param bottomSheetState поток корневого компонента нижней шторки или null, если закрыта
 * @param onActions callback для списка UI-действий
 * @param onBack callback при нажатии Back или закрытии шторки
 * @param onWidgetValueChange callback при изменении значения виджета
 * @param getSheetCornerRadiusDp радиус скругления корневого layout шторки (dp) или null
 * @param styleRegistry реестр стилей для Compose (текст и цвет по коду стиля)
 */
@Composable
fun GenerativeScreen(
    state: GenerativeUiState,
    bottomSheetState: StateFlow<ComponentModel?>,
    onActions: (List<UiAction>) -> Unit,
    onBack: () -> Unit,
    onWidgetValueChange: WidgetValueSetter,
    getSheetCornerRadiusDp: (ComponentModel) -> Int?,
    styleRegistry: ComposeStyleRegistry? = null,
) {
    GenerativeScreenUi(
        state = state,
        bottomSheetState = bottomSheetState,
        onActions = onActions,
        onBack = onBack,
        onWidgetValueChange = onWidgetValueChange,
        getSheetCornerRadiusDp = getSheetCornerRadiusDp,
        styleRegistry = styleRegistry,
    )
}

/**
 * Внутренний UI-слой [GenerativeScreen] без обёртки темы вызывающей стороны.
 *
 * @param state состояние UI (Loading / Screen / Error)
 * @param bottomSheetState поток корневого компонента нижней шторки или null, если закрыта
 * @param onActions callback для списка UI-действий
 * @param onBack callback при нажатии Back или закрытии шторки
 * @param onWidgetValueChange callback при изменении значения виджета
 * @param getSheetCornerRadiusDp радиус скругления корневого layout шторки (dp) или null
 * @param styleRegistry реестр стилей для Compose (текст и цвет по коду стиля)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerativeScreenUi(
    state: GenerativeUiState,
    bottomSheetState: StateFlow<ComponentModel?>,
    onActions: (List<UiAction>) -> Unit,
    onBack: () -> Unit,
    onWidgetValueChange: WidgetValueSetter,
    getSheetCornerRadiusDp: (ComponentModel) -> Int? = { null },
    styleRegistry: ComposeStyleRegistry? = null,
) {
    DrivenUITheme {
        val systemIsDark = isSystemInDarkTheme()
        CompositionLocalProvider(
            LocalStyleRegistry provides styleRegistry,
            LocalIsDarkTheme provides systemIsDark,
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    ScreenContent(
                        state = state,
                        onActions = onActions,
                        onWidgetValueChange = onWidgetValueChange,
                    )
                }

                BottomSheetHost(
                    bottomSheetState = bottomSheetState,
                    onActions = onActions,
                    onBack = onBack,
                    onWidgetValueChange = onWidgetValueChange,
                    getSheetCornerRadiusDp = getSheetCornerRadiusDp,
                )
            }
        }
    }
}

@Composable
private fun ScreenContent(
    state: GenerativeUiState,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter,
) {
    when (state) {
        is GenerativeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is GenerativeUiState.Screen -> {
            key(state.screenId, state.dataEpoch) {
                state.model?.also { model ->
                    ComponentRenderer(
                        model = model,
                        onActions = onActions,
                        onWidgetValueChange = onWidgetValueChange,
                    )
                }
            }
        }
        is GenerativeUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize())
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
    getSheetCornerRadiusDp: (ComponentModel) -> Int? = { null },
) {
    val bottomSheet = bottomSheetState.collectAsStateWithLifecycle().value

    bottomSheet?.also { sheetModel ->
        val configuration = LocalConfiguration.current
        val maxSheetHeight = (configuration.screenHeightDp * 0.99f).dp

        val sheetShape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        ModalBottomSheet(
            onDismissRequest = { onBack() },
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
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                )
            }
        }
    }
}
