package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.InputModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

private val InputOutlineShape = RoundedCornerShape(4.dp)
private val InputOutlineWidth = 1.dp
private val InputOutlinePaddingHorizontal = 12.dp
private val InputOutlinePaddingVertical = 10.dp

private const val FINISH_TYPING_DEBOUNCE_MS = 800L

@Composable
fun InputRenderer(
    model: InputModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter,
    modifier: Modifier = Modifier,
) {
    val resolvedText = model.displayText ?: model.text
    val resolvedHint = model.displayHint ?: model.hint

    var text by remember(model.widgetCode) { mutableStateOf(resolvedText) }

    val scope = rememberCoroutineScope()
    val finishTypingDebounce = remember(model.widgetCode) { FinishTypingDebounce() }

    LaunchedEffect(resolvedText) {
        if (text != resolvedText) {
            finishTypingDebounce.cancel()
            text = resolvedText
        }
    }

    DisposableEffect(model.widgetCode) {
        onDispose {
            finishTypingDebounce.cancel()
        }
    }

    val fieldTextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
    val hintTextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    BasicTextField(
        modifier = modifier.then(model.modifierParams.applyParams(Modifier)),
        value = text,
        onValueChange = { newText ->
            text = newText
            onWidgetValueChange(model.widgetCode, "text", newText)

            val actions = model.finishTypingActions
            if (actions.isEmpty()) return@BasicTextField

            finishTypingDebounce.cancel()
            finishTypingDebounce.job = scope.launch {
                delay(FINISH_TYPING_DEBOUNCE_MS)
                ensureActive()
                finishTypingDebounce.job = null
                onActions(actions)
            }
        },
        readOnly = model.readOnly,
        textStyle = fieldTextStyle,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = InputOutlineWidth,
                        color = MaterialTheme.colorScheme.outline,
                        shape = InputOutlineShape,
                    )
                    .padding(
                        horizontal = InputOutlinePaddingHorizontal,
                        vertical = InputOutlinePaddingVertical,
                    ),
            ) {
                if (text.isEmpty() && resolvedHint.isNotEmpty()) {
                    Text(
                        text = resolvedHint,
                        style = hintTextStyle,
                    )
                }
                innerTextField()
            }
        },
    )
}

private class FinishTypingDebounce {
    var job: Job? = null

    fun cancel() {
        job?.cancel()
        job = null
    }
}
