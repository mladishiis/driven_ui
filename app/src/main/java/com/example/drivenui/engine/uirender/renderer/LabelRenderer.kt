package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.ModifierParams

@Composable
fun LabelRenderer(
    model: LabelModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val baseModifier = modifier.then(model.modifierParams.applyParams(Modifier))
    val labelModifier = if (model.tapActions.isNotEmpty()) {
        baseModifier.then(
            Modifier.clickable {
                onActions(model.tapActions)
            }
        )
    } else {
        baseModifier
    }

    Text(
        text = model.text,
        modifier = labelModifier,
        style = model.textStyle,
    )
}

@Preview
@Composable
private fun LabelRendererPreview() {
    LabelRenderer(
        model = LabelModel(
            modifier = Modifier,
            modifierParams = ModifierParams(),
            text = "Подпись",
            widgetCode = "lbl1",
            tapActions = emptyList(),
            alignmentStyle = "",
        ),
        onActions = {},
    )
}