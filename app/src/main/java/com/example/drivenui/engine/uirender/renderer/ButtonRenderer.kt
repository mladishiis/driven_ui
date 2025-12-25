package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.drivenui.engine.uirender.models.ButtonModel

// Пока кнопки только с текстом внутри
@Composable
fun ButtonRenderer(model: ButtonModel) {
    val shape = if (model.roundedCornerSize != null) {
        RoundedCornerShape(model.roundedCornerSize.dp)
    } else {
        ButtonDefaults.shape
    }
    Button(
        modifier = model.modifier,
        shape = shape,
        contentPadding = PaddingValues(
            top = ButtonDefaults.ContentPadding.calculateTopPadding(),
            bottom = ButtonDefaults.ContentPadding.calculateBottomPadding(),
            start = 13.dp,
            end = 13.dp,
        ),
        onClick = {}) {
        Text(
            text = model.text,
            style = model.textStyle,
        )
    }
}

@Composable
@Preview
fun ButtonRendererPreview() {
    Box(modifier= Modifier.wrapContentHeight()) {
        ButtonRenderer(
            ButtonModel(
                modifier = Modifier
                    .padding(PaddingValues(20.dp))
                    .height(60.dp)
                    .fillMaxWidth(),
                enabled = true,
                text = "+ Добавить перевозчика",
                textStyle = TextStyle.Default.merge(
                    fontSize = 22.sp,
                    fontWeight = FontWeight(400),
                    color = Color.Black,
                ),
                roundedCornerSize = 16,
                alignmentStyle = "",
            )
        )
    }
}