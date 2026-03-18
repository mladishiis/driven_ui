package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.drivenui.R
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.utils.resolveImageData

/**
 * Рендерит UI-компонент изображения.
 *
 * @param model Модель изображения
 * @param onActions Callback при выполнении экшенов
 * @param modifier Дополнительный modifier
 */
@Composable
fun ImageRenderer(
    model: ImageModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val baseModifier = modifier.then(model.modifierParams.applyParams(Modifier))
    val imageModifier = if (model.tapActions.isNotEmpty()) {
        baseModifier.then(
            Modifier.clickable {
                onActions(model.tapActions)
            }
        )
    } else {
        baseModifier
    }

    val data = remember(model.url) {
        resolveImageData(context, model.url)
    }

    val colorFilter = if (model.color != Color.Unspecified) {
        ColorFilter.tint(model.color)
    } else {
        null
    }

    if (data == null) {
        Image(
            modifier = imageModifier,
            painter = painterResource(id = R.drawable.ic_24_close),
            contentDescription = null,
            colorFilter = colorFilter,
        )
    } else {
        AsyncImage(
            modifier = imageModifier,
            model = ImageRequest.Builder(context)
                .data(data)
                .crossfade(true)
                .build(),
            placeholder = painterResource(id = R.drawable.ic_24_close),
            error = painterResource(id = R.drawable.ic_24_close),
            contentDescription = null,
            colorFilter = colorFilter,
        )
    }
}