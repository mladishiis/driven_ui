package com.example.drivenui.engine.uirender.renderer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.example.drivenui.R
import com.example.drivenui.engine.uirender.models.ImageModel

@Composable
fun ImageRenderer(model: ImageModel) {
    AsyncImage(
        modifier = model.modifier,
        // TODO доставать из hcms
        model = painterResource(id = R.drawable.ic_launcher_background),
        contentDescription = null,
    )
}