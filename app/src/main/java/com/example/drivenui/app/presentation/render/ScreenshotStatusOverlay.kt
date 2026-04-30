package com.example.drivenui.app.presentation.render

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Небольшой overlay-индикатор состояния автоматического скриншота.
 *
 * @param message текст статуса или `null`, если индикатор не нужно показывать
 * @param modifier внешний модификатор позиционирования
 */
@Composable
internal fun ScreenshotStatusOverlay(
    message: String?,
    modifier: Modifier = Modifier,
) {
    if (message == null) return

    Surface(
        modifier = modifier
            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
            .widthIn(max = 320.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shape = MaterialTheme.shapes.small,
        shadowElevation = 6.dp,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}