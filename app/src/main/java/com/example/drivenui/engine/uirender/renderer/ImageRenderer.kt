package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.drivenui.R
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ImageModel

@Composable
fun ImageRenderer(
    model: ImageModel,
    onActions: (List<UiAction>) -> Unit,
) {
    val imageModifier = if (model.tapActions.isNotEmpty()) {
        model.modifier.then(
            Modifier.clickable {
                onActions(model.tapActions)
            }
        )
    } else {
        model.modifier
    }


    Image(
        modifier = imageModifier,
        painter = painterResource(id = R.drawable.ic_24_close),
        contentDescription = null,
    )
}