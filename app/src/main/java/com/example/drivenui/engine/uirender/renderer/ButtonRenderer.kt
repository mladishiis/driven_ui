package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ModifierParams

// Пока кнопки только с текстом внутри
@Composable
fun ButtonRenderer(
    model: ButtonModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = if (model.roundedCornerSize != null) {
        RoundedCornerShape(model.roundedCornerSize.dp)
    } else {
        ButtonDefaults.shape
    }
    Button(
        modifier = modifier.then(model.modifierParams.applyParams(Modifier)),
        shape = shape,
        contentPadding = PaddingValues(
            top = ButtonDefaults.ContentPadding.calculateTopPadding(),
            bottom = ButtonDefaults.ContentPadding.calculateBottomPadding(),
            start = 13.dp,
            end = 13.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = model.backgroundColor
        ),
        onClick = {
            if (model.tapActions.isNotEmpty()) {
                onActions(model.tapActions)
            }
        }
    ) {
        Text(
            text = model.text,
            style = model.textStyle,
        )
    }
}

@Preview
@Composable
private fun ButtonRendererPreview() {
    ButtonRenderer(
        model = ButtonModel(
            modifier = Modifier,
            modifierParams = ModifierParams(),
            enabled = true,
            text = "Кнопка",
            roundedCornerSize = null,
            tapActions = emptyList(),
            widgetCode = "btn1",
            alignmentStyle = "",
        ),
        onActions = {},
    )
}