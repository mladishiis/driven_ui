package com.example.drivenui.engine.uirender.renderer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.AppBarModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarRenderer(
    model: AppBarModel,
    onActions: (List<UiAction>) -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = model.modifierParams.applyParams(Modifier),
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
                IconButton(onClick = {
                    if (model.tapActions.isNotEmpty()) {
                        onActions(model.tapActions)
                    }
                }) {
                    // TODO загружать из hcms
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}