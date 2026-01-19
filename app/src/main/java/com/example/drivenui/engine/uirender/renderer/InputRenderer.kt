package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.InputModel

@Composable
fun InputRenderer(
    model: InputModel,
    onAction: (UiAction) -> Unit,
    onWidgetValueChange: WidgetValueSetter
) {
    var text by remember { mutableStateOf(model.text) }

    var isFirstValue by remember { mutableStateOf(true) }

    val finishTypingAction = remember(model) {
        model.finishTypingActions.firstOrNull() ?: UiAction.RefreshWidget(model.widgetCode)
    }

    LaunchedEffect(text) {
        if (isFirstValue) {
            isFirstValue = false
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(800)
        onWidgetValueChange(model.widgetCode, "text", text)
        onAction(finishTypingAction)
    }

    BasicTextField(
        modifier = model.modifier,
        value = text,
        onValueChange = { newText ->
            text = newText
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onWidgetValueChange(model.widgetCode, "text", text)
                onAction(finishTypingAction)
            }
        )
    )
}