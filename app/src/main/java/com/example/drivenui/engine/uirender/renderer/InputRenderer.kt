package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.InputModel

private val InputOutlineShape = RoundedCornerShape(4.dp)
private val InputOutlineWidth = 1.dp
private val InputOutlinePaddingHorizontal = 12.dp
private val InputOutlinePaddingVertical = 10.dp

@Composable
fun InputRenderer(
    model: InputModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter,
    modifier: Modifier = Modifier,
) {
    var text by remember(model) { mutableStateOf(model.text) }

    var isFirstValue by remember(model) { mutableStateOf(true) }

    val finishTypingActions = remember(model) { model.finishTypingActions }

    val fieldTextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
    val hintTextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    LaunchedEffect(text) {
        if (isFirstValue) {
            isFirstValue = false
            return@LaunchedEffect
        }
        if (finishTypingActions.isNotEmpty()) {
            kotlinx.coroutines.delay(800)
            onWidgetValueChange(model.widgetCode, "text", text)
            onActions(finishTypingActions)
        }
    }

    BasicTextField(
        modifier = modifier.then(model.modifierParams.applyParams(Modifier)),
        value = text,
        onValueChange = { newText ->
            text = newText
        },
        readOnly = model.readOnly,
        textStyle = fieldTextStyle,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (finishTypingActions.isNotEmpty()) {
                    onWidgetValueChange(model.widgetCode, "text", text)
                    onActions(finishTypingActions)
                }
            }
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
                if (text.isEmpty() && model.hint.isNotEmpty()) {
                    Text(
                        text = model.hint,
                        style = hintTextStyle,
                    )
                }
                innerTextField()
            }
        },
    )
}