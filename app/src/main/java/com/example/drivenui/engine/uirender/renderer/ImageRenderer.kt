package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
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

    AsyncImage(
        modifier = imageModifier,
        // TODO доставать из hcms
        model = painterResource(id = R.drawable.ic_launcher_background),
        contentDescription = null,
    )
}