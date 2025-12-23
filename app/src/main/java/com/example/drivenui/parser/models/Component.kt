package com.example.drivenui.parser.models

/**
 * Базовый компонент UI, который может быть:
 * - Layout (вертикальный, горизонтальный, слои)
 * - Widget (текст, кнопка, изображение и т.д.)
 * - ScreenLayout (контейнер на экране)
 *
 * Все компоненты наследуют общие свойства и могут содержать дочерние компоненты.
 */
sealed class Component {
    abstract val title: String
    abstract val code: String
    abstract val properties: List<EventProperty>
    abstract val styles: List<WidgetStyle>
    abstract val events: List<WidgetEvent>
    abstract val children: List<Component>
    abstract val bindingProperties: List<String>
    abstract val type: ComponentType
    abstract val index: Int
}

/**
 * Тип компонента
 */
enum class ComponentType {
    SCREEN_LAYOUT,  // Контейнерный лэйаут на экране
    LAYOUT,         // Базовый лэйаут (vertical, horizontal, layers)
    WIDGET,         // Виджет (button, label, image и т.д.)
    SCREEN          // Экран
}

/**
 * Компонент лэйаута (контейнер)
 */
data class LayoutComponent(
    override val title: String,
    override val code: String,
    val layoutCode: String,  // "vertical", "horizontal", "layers"
    override val properties: List<EventProperty> = emptyList(),
    override val styles: List<WidgetStyle> = emptyList(),
    override val events: List<WidgetEvent> = emptyList(),
    override val children: List<Component> = emptyList(),
    override val bindingProperties: List<String> = emptyList(),
    override val type: ComponentType = ComponentType.LAYOUT,
    override val index: Int = 0,
    val forIndexName: String? = null
) : Component()

/**
 * Компонент виджета (листовой элемент)
 */
data class WidgetComponent(
    override val title: String,
    override val code: String,
    val widgetCode: String,  // "button", "label", "image" и т.д.
    val widgetType: String,  // "native"
    override val properties: List<EventProperty> = emptyList(),
    override val styles: List<WidgetStyle> = emptyList(),
    override val events: List<WidgetEvent> = emptyList(),
    override val children: List<Component> = emptyList(),
    override val bindingProperties: List<String> = emptyList(),
    override val type: ComponentType = ComponentType.WIDGET,
    override val index: Int = 0,
    val forIndexName: String? = null
) : Component()