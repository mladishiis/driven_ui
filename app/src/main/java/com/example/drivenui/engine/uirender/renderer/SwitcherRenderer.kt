package com.example.drivenui.engine.uirender.renderer

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.SwitcherModel

/**
 * Рендерит UI-компонент переключателя (Switch).
 *
 * @param model Модель переключателя
 * @param onActions Callback при выполнении экшенов
 * @param modifier Дополнительный modifier
 */
@Composable
fun SwitcherRenderer(
    model: SwitcherModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        modifier = modifier,
        checked = model.checked,
        onCheckedChange = {}
    )
}