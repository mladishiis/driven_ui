package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.LayoutType

@Composable
fun LayoutRenderer(model: LayoutModel) {
    when (model.type) {
        LayoutType.VERTICAL_LAYOUT -> ColumnRenderer(model)
        LayoutType.HORIZONTAL_LAYOUT -> RowRenderer(model)
        LayoutType.LAYER -> BoxRenderer(model)
    }
}

@Composable
private fun ColumnRenderer(model: LayoutModel) {
    Column(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(model = child)
        }
    }
}

@Composable
private fun RowRenderer(model: LayoutModel) {
    Row(modifier = model.modifier) {
        model.children.forEach { child ->
            ComponentRenderer(model = child)
        }
    }
}

@Composable
private fun BoxRenderer(model: LayoutModel) {
    Box(
        modifier = model.modifier
    ) {
        model.children.forEach { child ->
            val modifier = when (child.alignmentStyle.lowercase()) {
                "aligncenter" -> Modifier
                    .align(Alignment.Center)

                "alignleft", "alignstart" -> Modifier
                    .align(Alignment.CenterStart)

                "alignright", "alignend" -> Modifier
                    .align(Alignment.CenterEnd)

                "aligntop" -> Modifier
                    .align(Alignment.TopCenter)

                "alignbottom" -> Modifier
                    .align(Alignment.BottomCenter)

                else -> Modifier
            }
            Box(modifier = modifier) {
                ComponentRenderer( child)
            }
        }
    }
}

