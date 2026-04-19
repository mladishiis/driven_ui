package com.example.drivenui.engine.uirender.renderer

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
    val isDarkTheme = LocalIsDarkTheme.current
    val styleRegistry = LocalStyleRegistry.current
    val resolvedTitleColor = model.colorStyleCode?.let { styleRegistry?.getComposeColor(it, isDarkTheme) }
    val resolvedIconTint = model.leftIconColorStyleCode?.let { styleRegistry?.getComposeColor(it, isDarkTheme) }
    val resolvedContainerColor = model.backgroundColorStyleCode?.let { styleRegistry?.getComposeColor(it, isDarkTheme) }
    val effectiveTitleStyle = if (resolvedTitleColor != null) model.textStyle.copy(color = resolvedTitleColor) else model.textStyle
    val effectiveIconTint = resolvedIconTint ?: model.leftIconTint
    val effectiveContainerColor = resolvedContainerColor ?: model.containerColor

    val defaultColors = TopAppBarDefaults.topAppBarColors()
    val barColors = TopAppBarDefaults.topAppBarColors(
        containerColor = if (effectiveContainerColor != Color.Unspecified) {
            effectiveContainerColor
        } else {
            defaultColors.containerColor
        },
        scrolledContainerColor = defaultColors.scrolledContainerColor,
        navigationIconContentColor = if (effectiveIconTint != Color.Unspecified) {
            effectiveIconTint
        } else {
            defaultColors.navigationIconContentColor
        },
        titleContentColor = defaultColors.titleContentColor,
        actionIconContentColor = defaultColors.actionIconContentColor,
    )

    CenterAlignedTopAppBar(
        colors = barColors,
        title = {
            val titleText = model.displayTitle ?: model.title
            if (titleText != null) {
                Text(
                    text = titleText,
                    style = effectiveTitleStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            val iconUrl = model.displayIconLeftUrl ?: model.iconLeftUrl
            if (iconUrl != null) {
                AppBarIcon(
                    context = context,
                    iconUrl = iconUrl,
                    iconTint = effectiveIconTint,
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
    iconTint: Color,
    onTap: () -> Unit,
) {
    val data = remember(iconUrl) { resolveImageData(context, iconUrl) }
    val colorFilter = if (iconTint != Color.Unspecified) {
        ColorFilter.tint(iconTint)
    } else {
        null
    }

    IconButton(onClick = onTap) {
        if (data == null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = if (iconTint != Color.Unspecified) iconTint else MaterialTheme.colorScheme.onSurface,
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
                colorFilter = colorFilter,
            )
        }
    }
}