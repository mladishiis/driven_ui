package com.example.drivenui.engine.uirender.renderer

import android.content.Context
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
import com.example.drivenui.app.data.MicroappRootFinder
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ImageModel
import java.io.File

@Composable
fun ImageRenderer(
    model: ImageModel,
    onActions: (List<UiAction>) -> Unit,
) {
    val context = LocalContext.current

    val imageModifier = if (model.tapActions.isNotEmpty()) {
        model.modifier.then(
            Modifier.clickable {
                onActions(model.tapActions)
            }
        )
    } else {
        model.modifier
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

/**
 * Резолвит значение из url в источник данных для Coil:
 * - если это полный URL, возвращает его как есть;
 * - если это имя файла (например, "close.svg"), ищет:
 *   1) во временной папке microapp: microapps/{microappName}/resources/images
 *   2) в assets: resources/images.
 */
private fun resolveImageData(context: Context, url: String?): Any? {
    if (url.isNullOrBlank()) return null

    if (url.startsWith("http://") || url.startsWith("https://")) {
        return url
    }

    // Ищем в динамически определённой папке микроаппа
    val microappRoot = MicroappRootFinder.findMicroappRoot(context)
    if (microappRoot != null) {
        val runtimeFile = File(microappRoot, url)
        if (runtimeFile.exists()) {
            return runtimeFile
        }
    }

    return "file:///android_asset/$url"
}