package com.example.drivenui.parser.binding

import android.util.Log
import com.example.drivenui.parser.models.*
import org.json.JSONArray

/**
 * Движок биндинга данных - применяет макросы к компонентам
 */
class BindingEngine {

    companion object {
        private const val TAG = "BindingEngine"
    }

    private val bindingParser = BindingParser()
    private val pathResolver = DataPathResolver

    /**
     * Применяет биндинги к компоненту и всем его дочерним компонентам
     */
    fun bindComponent(
        component: Component,
        context: DataContext
    ): Component {
        return try {
            Log.d(TAG, "Биндинг компонента: ${component.title} (${component.code})")

            when (component) {
                is LayoutComponent -> bindLayoutComponent(component, context)
                is WidgetComponent -> bindWidgetComponent(component, context)
                else -> {
                    Log.w(TAG, "Неизвестный тип компонента: ${component.javaClass.simpleName}")
                    component
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при биндинге компонента ${component.code}", e)
            component
        }
    }

    /**
     * Биндит данные к LayoutComponent
     */
    private fun bindLayoutComponent(
        component: LayoutComponent,
        context: DataContext
    ): LayoutComponent {
        // Применяем биндинги к свойствам
        val boundProperties = bindProperties(component.properties, context)

        // Рекурсивно биндим дочерние компоненты
        val boundChildren = component.children.map { child ->
            bindComponent(child, context)
        }

        // Логируем результат
        val bindingsCount = boundProperties.sumOf { it.bindings.size }
        if (bindingsCount > 0) {
            Log.d(TAG, "LayoutComponent ${component.code}: применено $bindingsCount биндингов")
        }

        return component.copy(
            properties = boundProperties,
            children = boundChildren
        )
    }

    /**
     * Биндит данные к WidgetComponent
     */
    private fun bindWidgetComponent(
        component: WidgetComponent,
        context: DataContext
    ): WidgetComponent {
        val boundProperties = bindProperties(component.properties, context)

        val bindingsCount = boundProperties.sumOf { it.bindings.size }
        if (bindingsCount > 0) {
            Log.d(TAG, "WidgetComponent ${component.code}: применено $bindingsCount биндингов")

            // Логируем разрешенные значения для отладки
            boundProperties.forEach { property ->
                if (property.hasBindings && property.resolvedValue != property.rawValue) {
                    Log.d(TAG, "  ${property.code}: '${property.rawValue}' → '${property.resolvedValue}'")
                }
            }
        }

        return component.copy(properties = boundProperties)
    }

    /**
     * Биндит данные к списку свойств
     */
    private fun bindProperties(
        properties: List<ComponentProperty>,
        context: DataContext
    ): List<ComponentProperty> {
        return properties.map { property ->
            if (property.hasBindings) {
                resolvePropertyBindings(property, context)
            } else {
                // Проверяем, есть ли биндинги, которые не были распарсены ранее
                if (bindingParser.hasBindings(property.rawValue)) {
                    // Перепарсим свойства на случай, если они были пропущены
                    val newBindings = bindingParser.parseBindings(property.rawValue)
                    if (newBindings.isNotEmpty()) {
                        val updatedProperty = property.copy(bindings = newBindings)
                        return@map resolvePropertyBindings(updatedProperty, context)
                    }
                }
                property
            }
        }
    }

    /**
     * Разрешает биндинги в свойстве
     */
    private fun resolvePropertyBindings(
        property: ComponentProperty,
        context: DataContext
    ): ComponentProperty {
        if (!property.hasBindings) return property

        var resolvedValue = property.rawValue

        // Для каждого биндинга в свойстве
        property.bindings.forEachIndexed { index, binding ->
            try {
                // Разрешаем путь данных
                val resolvedData = pathResolver.resolve(
                    context,
                    "${binding.sourceName}.${binding.path}"
                )

                // Получаем строковое значение
                val replacement = pathResolver.valueToString(resolvedData)

                if (replacement.isNotEmpty()) {
                    // Заменяем макрос на значение
                    resolvedValue = resolvedValue.replace(binding.expression, replacement)
                    Log.d(TAG, "Биндинг $index разрешен: ${binding.expression} → '$replacement'")
                } else {
                    Log.w(TAG, "Биндинг $index: пустое значение для ${binding.expression}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при разрешении биндинга: ${binding.expression}", e)
            }
        }

        return property.copy(resolvedValue = resolvedValue)
    }

    /**
     * Применяет биндинги к экрану
     */
    fun bindScreen(screen: ParsedScreen, context: DataContext): ParsedScreen {
        return try {
            screen.rootComponent?.let { rootComponent ->
                val boundComponent = bindComponent(rootComponent, context)
                return screen.copy(rootComponent = boundComponent)
            }
            screen
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при биндинге экрана ${screen.screenCode}", e)
            screen
        }
    }

    /**
     * Применяет биндинги к списку экранов
     */
    fun bindScreens(screens: List<ParsedScreen>, context: DataContext): List<ParsedScreen> {
        return screens.map { screen ->
            bindScreen(screen, context)
        }
    }

    /**
     * Создает контекст данных для экрана
     */
    fun createScreenContext(
        jsonFiles: Map<String, JSONArray> = emptyMap(),
        queryResults: Map<String, Any> = emptyMap(),
        screenVariables: Map<String, Any> = emptyMap()
    ): DataContext {
        return DataContext(
            jsonSources = jsonFiles,
            queryResults = queryResults,
            localVariables = screenVariables
        )
    }

    /**
     * Обновляет свойство компонента с новыми данными
     */
    fun updatePropertyWithNewData(
        component: Component,
        propertyCode: String,
        newValue: String
    ): Component {
        return when (component) {
            is LayoutComponent -> {
                val updatedProperties = component.properties.map { property ->
                    if (property.code == propertyCode) {
                        property.copy(resolvedValue = newValue)
                    } else {
                        property
                    }
                }
                component.copy(properties = updatedProperties)
            }

            is WidgetComponent -> {
                val updatedProperties = component.properties.map { property ->
                    if (property.code == propertyCode) {
                        property.copy(resolvedValue = newValue)
                    } else {
                        property
                    }
                }
                component.copy(properties = updatedProperties)
            }

            else -> component
        }
    }

    /**
     * Находит все источники данных, используемые в компоненте
     */
    fun findUsedSources(component: Component): Set<String> {
        val sources = mutableSetOf<String>()

        fun collectSources(comp: Component) {
            comp.properties.forEach { property ->
                property.bindings.forEach { binding ->
                    sources.add(binding.sourceName)
                }
            }

            comp.children.forEach { child ->
                collectSources(child)
            }
        }

        collectSources(component)
        return sources
    }

    /**
     * Проверяет, все ли биндинги компонента разрешены
     */
    fun allBindingsResolved(component: Component): Boolean {
        return component.properties.all { property ->
            if (property.hasBindings) {
                // Проверяем, изменилось ли значение после разрешения
                property.resolvedValue != property.rawValue
            } else {
                true
            }
        } && component.children.all { child ->
            allBindingsResolved(child)
        }
    }

    /**
     * Получает статистику по биндингам компонента
     */
    fun getBindingStats(component: Component): BindingStats {
        var totalBindings = 0
        var resolvedBindings = 0
        val usedSources = mutableSetOf<String>()

        fun collectStats(comp: Component) {
            comp.properties.forEach { property ->
                totalBindings += property.bindings.size
                property.bindings.forEach { binding ->
                    usedSources.add(binding.sourceName)
                    if (property.resolvedValue != property.rawValue) {
                        resolvedBindings++
                    }
                }
            }

            comp.children.forEach { child ->
                collectStats(child)
            }
        }

        collectStats(component)

        return BindingStats(
            totalBindings = totalBindings,
            resolvedBindings = resolvedBindings,
            usedSources = usedSources
        )
    }
}

/**
 * Статистика по биндингам
 */
data class BindingStats(
    val totalBindings: Int = 0,
    val resolvedBindings: Int = 0,
    val usedSources: Set<String> = emptySet()
) {
    val resolutionRate: Float
        get() = if (totalBindings > 0) resolvedBindings.toFloat() / totalBindings else 1.0f

    val unresolvedBindings: Int
        get() = totalBindings - resolvedBindings
}