package com.example.drivenui.app.presentation.details.model

/**
 * Модель для отображения элемента дерева компонентов.
 *
 * @property title заголовок
 * @property code код
 * @property type тип компонента
 * @property depth глубина в дереве
 * @property childrenCount количество дочерних
 * @property stylesCount количество стилей
 * @property eventsCount количество событий
 * @property propertiesCount количество свойств
 * @property children дочерние элементы
 */
data class ComponentTreeItem(
    val title: String,
    val code: String,
    val type: String,
    val depth: Int = 0,
    val childrenCount: Int = 0,
    val stylesCount: Int = 0,
    val eventsCount: Int = 0,
    val propertiesCount: Int = 0,
    val children: List<ComponentTreeItem> = emptyList()
) {
    /**
     * Получает отступ для отображения
     */
    val indent: String get() = "  ".repeat(depth)

    /**
     * Проверяет, имеет ли элемент детей
     */
    val hasChildren: Boolean get() = children.isNotEmpty() || childrenCount > 0

    /**
     * Получает информацию для отображения
     */
    val displayInfo: String get() {
        return buildString {
            append("$type: $title")
            if (propertiesCount > 0) append(" (свойств: $propertiesCount)")
            if (stylesCount > 0) append(", стилей: $stylesCount")
            if (eventsCount > 0) append(", событий: $eventsCount")
            if (childrenCount > 0) append(", детей: $childrenCount")
        }
    }
}
