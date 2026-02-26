package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.LabelModel

@Composable
fun LabelRenderer(
    model: LabelModel,
    onActions: (List<UiAction>) -> Unit,
) {
    val baseModifier = model.modifierParams.applyParams(Modifier)
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