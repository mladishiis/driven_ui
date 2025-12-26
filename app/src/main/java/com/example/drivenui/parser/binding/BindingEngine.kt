package com.example.drivenui.parser.binding

import android.util.Log
import com.example.drivenui.parser.models.*
import org.json.JSONArray

/**
 * Binding Engine для применения биндингов к компонентам
 */
class BindingEngine {
    private val dataPathResolver = DataPathResolver()

    fun bindComponent(component: Component, context: DataContext): Component {
        Log.d("BindingEngine", "Биндинг компонента: ${component.title} (${component.code})")

        // Обновляем свойства компонента
        val boundProperties = component.properties.map { property ->
            if (property.hasBindings) {
                var resolvedValue = property.rawValue

                property.bindings.forEach { binding ->
                    val value = dataPathResolver.resolvePath(binding.expression, context)
                    if (value != null) {
                        // Заменяем биндинг на значение
                        resolvedValue = resolvedValue.replace(binding.expression, value)
                        Log.d("BindingEngine", "Биндинг ${binding.expression} -> $value")
                    } else {
                        Log.w("BindingEngine", "Биндинг ${binding.expression}: не удалось разрешить")
                    }
                }

                property.copy(resolvedValue = resolvedValue)
            } else {
                property.copy(resolvedValue = property.rawValue)
            }
        }

        // Рекурсивно биндим детей
        val boundChildren = component.children.map { child ->
            bindComponent(child, context)
        }

        // Возвращаем обновленный компонент
        return when (component) {
            is LayoutComponent -> component.copy(
                properties = boundProperties,
                children = boundChildren
            )
            is WidgetComponent -> component.copy(
                properties = boundProperties,
                children = boundChildren
            )
            else -> component
        }
    }
}