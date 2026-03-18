package com.example.drivenui.engine.uirender.renderer

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.drivenui.R
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.utils.resolveImageData

/**
 * Рендерит UI-компонент верхней панели приложения (AppBar).
 * Подстановка иконки работает как в ImageRenderer: http(s), файлы из microapp, assets.
 *
 * @param model Модель AppBar
 * @param onActions Callback при выполнении экшенов
 * @param modifier Дополнительный modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarRenderer(
    model: AppBarModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    CenterAlignedTopAppBar(
        modifier = modifier.then(model.modifierParams.applyParams(Modifier)),
        title = {
            if (model.title != null) {
                Text(
                    text = model.title,
                    style = model.textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            if (model.iconLeftUrl != null) {
                AppBarIcon(
                    context = context,
                    iconUrl = model.iconLeftUrl,
                    onTap = { if (model.tapActions.isNotEmpty()) onActions(model.tapActions) },
                )
            }
        },
    )
}

@Composable
private fun AppBarIcon(
    context: Context,
    iconUrl: String,
    onTap: () -> Unit,
) {
    val data = remember(iconUrl) { resolveImageData(context, iconUrl) }

    IconButton(onClick = onTap) {
        if (data == null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
            )
        } else {
            AsyncImage(
                modifier = Modifier.size(24.dp),
                model = ImageRequest.Builder(context)
                    .data(data)
                    .crossfade(true)
                    .build(),
                error = painterResource(id = R.drawable.ic_24_close),
                contentDescription = null,
            )
        }
    }
}