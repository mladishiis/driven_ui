package com.example.drivenui.engine.uirender.renderer

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.mappers.parseTextAlign
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Рендерит UI-компонент кнопки.
 *
 * @param model Модель кнопки
 * @param onActions Callback при выполнении экшенов
 * @param modifier Дополнительный modifier
 */
@Composable
fun ButtonRenderer(
    model: ButtonModel,
    onActions: (List<UiAction>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val useDarkColorPalette = isSystemInDarkTheme()
    val styleRegistry = LocalStyleRegistry.current
    val resolvedTextColor =
        model.colorStyleCode?.let { styleRegistry?.getComposeColor(it, useDarkColorPalette) }
    val resolvedBgColor = model.backgroundColorStyleCode?.let {
        styleRegistry?.getComposeColor(it, useDarkColorPalette)
    }
    val effectiveTextStyle = if (resolvedTextColor != null) model.textStyle.copy(color = resolvedTextColor) else model.textStyle
    val effectiveBgColor = resolvedBgColor ?: model.backgroundColor

    val shape: Shape = model.cornerRadius.toRoundedCornerShape() ?: ButtonDefaults.shape
    var buttonModifier = modifier.then(model.modifier).then(model.modifierParams.applyParams(Modifier))
    val strokeW = model.stroke.resolvedWidthDp
    val strokeCol = model.stroke.resolvedColor
    if (strokeW != null && strokeW > 0 && strokeCol != null) {
        buttonModifier = buttonModifier.border(width = strokeW.dp, color = strokeCol, shape = shape)
    }

    Button(
        modifier = buttonModifier,
        shape = shape,
        contentPadding = PaddingValues(
            top = ButtonDefaults.ContentPadding.calculateTopPadding(),
            bottom = ButtonDefaults.ContentPadding.calculateBottomPadding(),
            start = 13.dp,
            end = 13.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = effectiveBgColor
        ),
        onClick = {
            if (model.tapActions.isNotEmpty()) {
                onActions(model.tapActions)
            }
        }
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = model.displayText ?: model.text,
            style = effectiveTextStyle,
            textAlign = parseTextAlign(model.textAlignment),
        )
    }
}

@Preview
@Composable
private fun ButtonRendererPreview() {
    ButtonRenderer(
        model = ButtonModel(
            modifier = Modifier,
            modifierParams = ModifierParams(),
            enabled = true,
            text = "Кнопка",
            tapActions = emptyList(),
            widgetCode = "btn1",
            alignment = "",
        ),
        onActions = {},
    )
}