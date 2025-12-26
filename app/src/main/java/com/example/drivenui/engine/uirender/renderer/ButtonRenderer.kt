package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ButtonModel

// Пока кнопки только с текстом внутри
@Composable
fun ButtonRenderer(
    model: ButtonModel,
    onAction: (UiAction) -> Unit,
) {
    val shape = if (model.roundedCornerSize != null) {
        RoundedCornerShape(model.roundedCornerSize.dp)
    } else {
        ButtonDefaults.shape
    }
    val action = model.tapAction.firstOrNull() ?: UiAction.Empty
    Button(
        modifier = model.modifier,
        shape = shape,
        contentPadding = PaddingValues(
            top = ButtonDefaults.ContentPadding.calculateTopPadding(),
            bottom = ButtonDefaults.ContentPadding.calculateBottomPadding(),
            start = 13.dp,
            end = 13.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = model.background
        ),
        onClick = { onAction.invoke(action) }
    ) {
        Text(
            text = model.text,
            style = model.textStyle,
        )
    }
}


fun CustomWidget(
    onAction : (UiAction) -> Unit,
){

}

//@Composable
//@Preview
//fun ButtonRendererPreview() {
//    Box(modifier= Modifier.wrapContentHeight()) {
//        ButtonRenderer(
//            ButtonModel(
//                modifier = Modifier
//                    .padding(PaddingValues(20.dp))
//                    .height(60.dp)
//                    .fillMaxWidth(),
//                enabled = true,
//                text = "+ Добавить перевозчика",
//                textStyle = TextStyle.Default.merge(
//                    fontSize = 22.sp,
//                    fontWeight = FontWeight(400),
//                    color = Color.Black,
//                ),
//                roundedCornerSize = 16,
//                alignmentStyle = "",
//            )
//        )
//    }
//}