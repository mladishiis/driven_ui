package com.example.drivenui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.drivenui.parser.SDUIParserNew
import com.example.drivenui.parser.models.AllStyles
import com.example.drivenui.parser.models.Component
import com.example.drivenui.parser.models.Layout
import com.example.drivenui.parser.models.LayoutComponent
import com.example.drivenui.parser.models.ParsedScreen
import com.example.drivenui.parser.models.Widget
import com.example.drivenui.parser.models.WidgetComponent
import com.example.drivenui.parser.models.WidgetEvent
import com.example.drivenui.parser.models.WidgetStyle

/**
 * Рендерер компонентов Driven UI для Compose
 */
@Composable
fun ComponentRenderer(
    screen: ParsedScreen,
    styleRegistry: StyleRegistry? = null,
    widgetRegistry: WidgetRegistry? = null,
    eventHandler: EventHandler? = null,
    onEvent: (String, WidgetEvent) -> Unit = { _, _ -> }
) {
    screen.rootComponent?.let { component ->
        RenderComponent(
            component = component,
            styleRegistry = styleRegistry,
            widgetRegistry = widgetRegistry,
            eventHandler = eventHandler,
            onEvent = onEvent
        )
    }
}

@Composable
private fun RenderComponent(
    component: Component,
    styleRegistry: StyleRegistry? = null,
    widgetRegistry: WidgetRegistry? = null,
    eventHandler: EventHandler? = null,
    onEvent: (String, WidgetEvent) -> Unit = { _, _ -> },
    depth: Int = 0
) {
    when (component) {
        is LayoutComponent -> RenderLayoutComponent(
            layout = component,
            styleRegistry = styleRegistry,
            widgetRegistry = widgetRegistry,
            eventHandler = eventHandler,
            onEvent = onEvent,
            depth = depth
        )
        is WidgetComponent -> RenderWidgetComponent(
            widget = component,
            styleRegistry = styleRegistry,
            widgetRegistry = widgetRegistry,
            eventHandler = eventHandler,
            onEvent = onEvent
        )
    }
}

@Composable
private fun RenderLayoutComponent(
    layout: LayoutComponent,
    styleRegistry: StyleRegistry?,
    widgetRegistry: WidgetRegistry?,
    eventHandler: EventHandler?,
    onEvent: (String, WidgetEvent) -> Unit,
    depth: Int
) {
    // Получаем модификаторы из стилей
    val modifier = buildModifierFromStyles(layout.styles, styleRegistry)

    when (layout.layoutCode.lowercase()) {
        "vertical" -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                layout.children.forEach { child ->
                    RenderComponent(
                        component = child,
                        styleRegistry = styleRegistry,
                        widgetRegistry = widgetRegistry,
                        eventHandler = eventHandler,
                        onEvent = onEvent,
                        depth = depth + 1
                    )
                }
            }
        }
        "horizontal" -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                layout.children.forEach { child ->
                    RenderComponent(
                        component = child,
                        styleRegistry = styleRegistry,
                        widgetRegistry = widgetRegistry,
                        eventHandler = eventHandler,
                        onEvent = onEvent,
                        depth = depth + 1
                    )
                }
            }
        }
        "layers" -> {
            Box(
                modifier = modifier
            ) {
                layout.children.forEach { child ->
                    RenderComponent(
                        component = child,
                        styleRegistry = styleRegistry,
                        widgetRegistry = widgetRegistry,
                        eventHandler = eventHandler,
                        onEvent = onEvent,
                        depth = depth + 1
                    )
                }
            }
        }
        else -> {
            // Дефолтный лэйаут
            Box(
                modifier = modifier
            ) {
                layout.children.forEach { child ->
                    RenderComponent(
                        component = child,
                        styleRegistry = styleRegistry,
                        widgetRegistry = widgetRegistry,
                        eventHandler = eventHandler,
                        onEvent = onEvent,
                        depth = depth + 1
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderWidgetComponent(
    widget: WidgetComponent,
    styleRegistry: StyleRegistry?,
    widgetRegistry: WidgetRegistry?,
    eventHandler: EventHandler?,
    onEvent: (String, WidgetEvent) -> Unit
) {
    // Получаем модификаторы из стилей
    val modifier = buildModifierFromStyles(widget.styles, styleRegistry)

    // Получаем текст из bindingProperties
    val bindingText = widget.bindingProperties.firstOrNull() ?: ""

    // Получаем свойства
    val properties = widget.properties.associate { it.code to it.value }

    when (widget.widgetCode) {
        "label" -> {
            RenderLabel(
                text = bindingText,
                modifier = modifier,
                styles = widget.styles,
                styleRegistry = styleRegistry,
                properties = properties,
                events = widget.events,
                onEvent = { eventCode, event ->
                    onEvent(eventCode, event)
                    eventHandler?.onEvent(eventCode, event)
                }
            )
        }
        "button" -> {
            RenderButton(
                text = bindingText,
                modifier = modifier,
                styles = widget.styles,
                styleRegistry = styleRegistry,
                properties = properties,
                events = widget.events,
                onEvent = { eventCode, event ->
                    onEvent(eventCode, event)
                    eventHandler?.onEvent(eventCode, event)
                }
            )
        }
        "image" -> {
            RenderImage(
                imageRef = bindingText,
                modifier = modifier,
                styles = widget.styles,
                styleRegistry = styleRegistry,
                properties = properties,
                events = widget.events,
                onEvent = { eventCode, event ->
                    onEvent(eventCode, event)
                    eventHandler?.onEvent(eventCode, event)
                }
            )
        }
        "input" -> {
            RenderInput(
                modifier = modifier,
                styles = widget.styles,
                styleRegistry = styleRegistry,
                properties = properties,
                events = widget.events,
                onEvent = { eventCode, event ->
                    onEvent(eventCode, event)
                    eventHandler?.onEvent(eventCode, event)
                }
            )
        }
        "checkbox" -> {
            RenderCheckbox(
                modifier = modifier,
                styles = widget.styles,
                styleRegistry = styleRegistry,
                properties = properties,
                events = widget.events,
                onEvent = { eventCode, event ->
                    onEvent(eventCode, event)
                    eventHandler?.onEvent(eventCode, event)
                }
            )
        }
        "switcher" -> {
            RenderSwitch(
                modifier = modifier,
                styles = widget.styles,
                styleRegistry = styleRegistry,
                properties = properties,
                events = widget.events,
                onEvent = { eventCode, event ->
                    onEvent(eventCode, event)
                    eventHandler?.onEvent(eventCode, event)
                }
            )
        }
        else -> {
            // Пытаемся найти в реестре
            widgetRegistry?.getWidgetDefinition(widget.widgetCode)?.let { widgetDef ->
                RenderCustomWidget(
                    widgetDef = widgetDef,
                    modifier = modifier,
                    properties = properties,
                    events = widget.events,
                    onEvent = { eventCode, event ->
                        onEvent(eventCode, event)
                        eventHandler?.onEvent(eventCode, event)
                    }
                )
            } ?: run {
                // Неизвестный виджет
                Text(
                    text = "Неизвестный виджет: ${widget.widgetCode}",
                    color = Color.Red,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun RenderLabel(
    text: String,
    modifier: Modifier,
    styles: List<WidgetStyle>,
    styleRegistry: StyleRegistry?,
    properties: Map<String, String>,
    events: List<WidgetEvent>,
    onEvent: (String, WidgetEvent) -> Unit
) {
    val textStyle = buildTextStyleFromStyles(styles, styleRegistry)

    // Обработка шаблонных строк
    val displayText = if (text.contains("\${") || text.contains("@{")) {
        // Здесь можно реализовать подстановку значений
        text
    } else {
        text
    }

    val clickableModifier = if (events.any { it.eventCode == "onTap" }) {
        modifier.clickable {
            events.firstOrNull { it.eventCode == "onTap" }?.let { event ->
                onEvent("onTap", event)
                // Выполняем действия события
                event.eventActions.forEach { action ->
                    // Здесь можно выполнять действия
                }
            }
        }
    } else {
        modifier
    }

    Text(
        text = displayText,
        style = textStyle,
        modifier = clickableModifier
    )
}

@Composable
private fun RenderButton(
    text: String,
    modifier: Modifier,
    styles: List<WidgetStyle>,
    styleRegistry: StyleRegistry?,
    properties: Map<String, String>,
    events: List<WidgetEvent>,
    onEvent: (String, WidgetEvent) -> Unit
) {
    val textStyle = buildTextStyleFromStyles(styles, styleRegistry)
    val enabled = properties["enabled"]?.toBooleanStrictOrNull() ?: true

    Button(
        onClick = {
            events.firstOrNull { it.eventCode == "onTap" }?.let { event ->
                onEvent("onTap", event)
                // Выполняем действия события
                event.eventActions.forEach { action ->
                    // Здесь можно выполнять действия
                }
            }
        },
        modifier = modifier,
        enabled = enabled
    ) {
        Text(text = text, style = textStyle)
    }
}

@Composable
private fun RenderImage(
    imageRef: String,
    modifier: Modifier,
    styles: List<WidgetStyle>,
    styleRegistry: StyleRegistry?,
    properties: Map<String, String>,
    events: List<WidgetEvent>,
    onEvent: (String, WidgetEvent) -> Unit
) {
    val context = LocalContext.current
    val clickableModifier = if (events.any { it.eventCode == "onTap" }) {
        modifier.clickable {
            events.firstOrNull { it.eventCode == "onTap" }?.let { event ->
                onEvent("onTap", event)
            }
        }
    } else {
        modifier
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageRef)
            .crossfade(true)
            .build(),
        contentDescription = properties["alt"] ?: "Image",
        modifier = clickableModifier
    )
}

@Composable
private fun RenderInput(
    modifier: Modifier,
    styles: List<WidgetStyle>,
    styleRegistry: StyleRegistry?,
    properties: Map<String, String>,
    events: List<WidgetEvent>,
    onEvent: (String, WidgetEvent) -> Unit
) {
    var text by remember { mutableStateOf(properties["text"] ?: "") }
    val hint = properties["hint"] ?: ""
    val readOnly = properties["readOnly"]?.toBooleanStrictOrNull() ?: false

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            events.firstOrNull { it.eventCode == "onChange" }?.let { event ->
                onEvent("onChange", event)
            }
        },
        label = { if (hint.isNotEmpty()) Text(hint) },
        modifier = modifier,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                events.firstOrNull { it.eventCode == "onSubmit" }?.let { event ->
                    onEvent("onSubmit", event)
                }
            }
        )
    )
}

@Composable
private fun RenderCheckbox(
    modifier: Modifier,
    styles: List<WidgetStyle>,
    styleRegistry: StyleRegistry?,
    properties: Map<String, String>,
    events: List<WidgetEvent>,
    onEvent: (String, WidgetEvent) -> Unit
) {
    var checked by remember {
        mutableStateOf(properties["checked"]?.toBooleanStrictOrNull() ?: false)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { isChecked ->
                checked = isChecked
                events.firstOrNull { it.eventCode == "onChange" }?.let { event ->
                    onEvent("onChange", event)
                }
            }
        )
    }
}

@Composable
private fun RenderSwitch(
    modifier: Modifier,
    styles: List<WidgetStyle>,
    styleRegistry: StyleRegistry?,
    properties: Map<String, String>,
    events: List<WidgetEvent>,
    onEvent: (String, WidgetEvent) -> Unit
) {
    var checked by remember {
        mutableStateOf(properties["checked"]?.toBooleanStrictOrNull() ?: false)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked,
            onCheckedChange = { isChecked ->
                checked = isChecked
                events.firstOrNull { it.eventCode == "onChange" }?.let { event ->
                    onEvent("onChange", event)
                }
            }
        )
    }
}

@Composable
private fun RenderCustomWidget(
    widgetDef: Widget,
    modifier: Modifier,
    properties: Map<String, String>,
    events: List<WidgetEvent>,
    onEvent: (String, WidgetEvent) -> Unit
) {
    // Здесь можно реализовать рендеринг кастомных виджетов
    Text(
        text = widgetDef.title,
        modifier = modifier
    )
}

/**
 * Строит модификатор из стилей
 */
private fun buildModifierFromStyles(
    styles: List<WidgetStyle>,
    styleRegistry: StyleRegistry?
): Modifier {
    return styles.fold(Modifier) { acc, style ->
        when (style.code) {
            "colorStyle" -> ({
                val color = getColorFromCode(style.value, styleRegistry)
                acc.background(color)
            }) as Modifier.Companion
            "roundStyle" -> ({
                val radius = getRadiusFromCode(style.value)
                acc.clip(RoundedCornerShape(radius))
            }) as Modifier.Companion
            "paddingStyle" -> ({
                val padding = getPaddingFromCode(style.value)
                acc.padding(padding)
            }) as Modifier.Companion
            "alignmentStyle" -> ({
                applyAlignmentStyle(style.value, acc)
            }) as Modifier.Companion
            else -> acc
        }
    }
}

/**
 * Применяет стиль выравнивания к модификатору
 */
private fun applyAlignmentStyle(alignmentCode: String, modifier: Modifier): Modifier {
    return when (alignmentCode.lowercase()) {
        "aligncenter" -> modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
        "alignleft", "alignstart" -> modifier.fillMaxWidth().wrapContentWidth(Alignment.Start)
        "alignright", "alignend" -> modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
        "aligntop" -> modifier.fillMaxHeight().wrapContentHeight(Alignment.Top)
        "alignbottom" -> modifier.fillMaxHeight().wrapContentHeight(Alignment.Bottom)
        else -> modifier
    }
}

/**
 * Строит TextStyle из стилей
 */
private fun buildTextStyleFromStyles(
    styles: List<WidgetStyle>,
    styleRegistry: StyleRegistry?
): TextStyle {
    return styles.fold(TextStyle.Default) { acc, style ->
        when (style.code) {
            "textStyle" -> {
                // Получаем стиль из реестра или дефолтный
                val fontStyle = styleRegistry?.getTextStyle(style.value)
                    ?: getDefaultTextStyle(style.value)

                // Извлекаем размер шрифта из кода стиля
                val fontSize = extractFontSizeFromStyleCode(style.value)

                acc.copy(
                    fontSize = fontSize.sp,
                    fontWeight = when (fontStyle.fontWeight) {
                        500, 600, 700 -> FontWeight.Bold
                        else -> FontWeight.Normal
                    }
                )
            }
            "colorStyle" -> {
                val color = getColorFromCode(style.value, styleRegistry)
                acc.copy(color = color)
            }
            else -> acc
        }
    }
}

/**
 * Извлекает размер шрифта из кода стиля
 */
private fun extractFontSizeFromStyleCode(styleCode: String): Float {
    return when (styleCode) {
        "headlineLarge32" -> 32f
        "headlineSmall24" -> 24f
        "titleLarge22" -> 22f
        "titleLarge19" -> 19f
        "titleMedium16" -> 16f
        "bodyMedium14" -> 14f
        "bodySmall12" -> 12f
        else -> 16f // Дефолтный размер
    }
}

/**
 * Получает цвет из кода стиля
 */
private fun getColorFromCode(colorCode: String, styleRegistry: StyleRegistry?): Color {
    return styleRegistry?.getColorStyle(colorCode)?.let { colorStyle ->
        // Здесь можно обрабатывать light/dark темы
        Color(android.graphics.Color.parseColor(colorStyle.lightTheme.color))
    } ?: when (colorCode) {
        "surface" -> Color.White
        "on-surface" -> Color(0xFF1D1B20)
        "primary" -> Color(0xFF485E92)
        "on-primary" -> Color.White
        "secondary-container" -> Color(0xFFD9DFF6)
        "on-secondary-container" -> Color(0xFF4A4459)
        "on-surface-variant" -> Color(0xFF49454F)
        else -> Color.Gray
    }
}

/**
 * Получает радиус из кода стиля
 */
private fun getRadiusFromCode(roundCode: String): Dp {
    return when (roundCode) {
        "radius16" -> 16.dp
        "radius8" -> 8.dp
        "radius4" -> 4.dp
        else -> 0.dp
    }
}

/**
 * Получает отступы из кода стиля
 */
private fun getPaddingFromCode(paddingCode: String): PaddingValues {
    return when (paddingCode) {
        "padding4-6-4-6" -> PaddingValues(4.dp, 6.dp, 4.dp, 6.dp)
        "padding8-16-8-16" -> PaddingValues(8.dp, 16.dp, 8.dp, 16.dp)
        else -> PaddingValues(16.dp)
    }
}

/**
 * Получает дефолтный стиль текста
 */
private fun getDefaultTextStyle(textStyleCode: String): com.example.drivenui.parser.models.TextStyle {
    return when (textStyleCode) {
        "headlineLarge32" -> com.example.drivenui.parser.models.TextStyle(
            code = "headlineLarge32",
            fontFamily = "headline",
            fontSize = 32,
            fontWeight = 500
        )
        "headlineSmall24" -> com.example.drivenui.parser.models.TextStyle(
            code = "headlineSmall24",
            fontFamily = "headline",
            fontSize = 24,
            fontWeight = 400
        )
        "titleLarge22" -> com.example.drivenui.parser.models.TextStyle(
            code = "titleLarge22",
            fontFamily = "title",
            fontSize = 22,
            fontWeight = 400
        )
        "titleLarge19" -> com.example.drivenui.parser.models.TextStyle(
            code = "titleLarge19",
            fontFamily = "title",
            fontSize = 19,
            fontWeight = 400
        )
        else -> com.example.drivenui.parser.models.TextStyle(
            code = "default",
            fontFamily = "default",
            fontSize = 16,
            fontWeight = 400
        )
    }
}

/**
 * Интерфейсы (можно вынести в отдельный файл)
 */
interface EventHandler {
    fun onEvent(eventCode: String, event: WidgetEvent)
}

interface StyleRegistry {
    fun getTextStyle(code: String): com.example.drivenui.parser.models.TextStyle?
    fun getColorStyle(code: String): com.example.drivenui.parser.models.ColorStyle?
    fun getAlignmentStyle(code: String): com.example.drivenui.parser.models.AlignmentStyle?
    fun getPaddingStyle(code: String): com.example.drivenui.parser.models.PaddingStyle?
    fun getRoundStyle(code: String): com.example.drivenui.parser.models.RoundStyle?
}

interface WidgetRegistry {
    fun getWidgetDefinition(code: String): Widget?
    fun getLayoutDefinition(code: String): Layout?
}

/**
 * Простая реализация реестра стилей для Compose
 */
class ComposeStyleRegistry(
    private val allStyles: AllStyles?
) : StyleRegistry {
    override fun getTextStyle(code: String): com.example.drivenui.parser.models.TextStyle? {
        return allStyles?.textStyles?.firstOrNull { it.code == code }
    }

    override fun getColorStyle(code: String): com.example.drivenui.parser.models.ColorStyle? {
        return allStyles?.colorStyles?.firstOrNull { it.code == code }
    }

    override fun getAlignmentStyle(code: String): com.example.drivenui.parser.models.AlignmentStyle? {
        return allStyles?.alignmentStyles?.firstOrNull { it.code == code }
    }

    override fun getPaddingStyle(code: String): com.example.drivenui.parser.models.PaddingStyle? {
        return allStyles?.paddingStyles?.firstOrNull { it.code == code }
    }

    override fun getRoundStyle(code: String): com.example.drivenui.parser.models.RoundStyle? {
        return allStyles?.roundStyles?.firstOrNull { it.code == code }
    }
}

/**
 * Пример использования в Compose
 */
@Composable
fun DrivenUIScreen(
    parsedResult: SDUIParserNew.ParsedMicroappResult,
    onEvent: (String, WidgetEvent) -> Unit = { _, _ -> }
) {
    val styleRegistry = remember(parsedResult.styles) {
        ComposeStyleRegistry(parsedResult.styles)
    }

    val widgetRegistry = remember(parsedResult.widgets, parsedResult.layouts) {
        object : WidgetRegistry {
            override fun getWidgetDefinition(code: String): Widget? {
                return parsedResult.widgets.firstOrNull { it.code == code }
            }

            override fun getLayoutDefinition(code: String): Layout? {
                return parsedResult.layouts.firstOrNull { it.code == code }
            }
        }
    }

    val screen = parsedResult.screens.firstOrNull()

    if (screen != null) {
        ComponentRenderer(
            screen = screen,
            styleRegistry = styleRegistry,
            widgetRegistry = widgetRegistry,
            onEvent = onEvent
        )
    } else {
        Text("Нет доступных экранов")
    }
}