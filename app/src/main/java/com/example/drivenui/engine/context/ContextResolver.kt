package com.example.drivenui.engine.context

import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel

/**
 * Подставляет значения из контекста в экранную модель.
 */
fun resolveScreen(
    screenModel: ScreenModel,
    contextManager: IContextManager
): ScreenModel {
    val resolvedRoot = resolveComponent(screenModel.rootComponent, contextManager)
    return screenModel.copy(rootComponent = resolvedRoot)
}

// TODO: может переписать посимпатичнее
fun resolveComponent(
    component: ComponentModel?,
    contextManager: IContextManager
): ComponentModel? {
    return when (component) {
        null -> null
        is LayoutModel -> component.copy(
            children = component.children.mapNotNull { child ->
                resolveComponent(child, contextManager)
            }
        )
        is LabelModel -> component.copy(
            text = resolveString(component.text, contextManager)
        )
        is ButtonModel -> component.copy(
            text = resolveString(component.text, contextManager)
        )
        is AppBarModel -> component.copy(
            title = component.title?.let { resolveString(it, contextManager) }
        )
        is InputModel -> component.copy(
            text = resolveString(component.text, contextManager),
            hint = resolveString(component.hint, contextManager)
        )
        is ImageModel -> component.copy(
            url = component.url?.let { resolveString(it, contextManager) }
        )
        else -> component
    }
}

private fun resolveString(
    value: String,
    contextManager: IContextManager
): String {
    if (value.startsWith("@{") && value.endsWith("}")) {
        val content = value.substring(2, value.length - 1)
        val parts = content.split(".", limit = 2)
        if (parts.size == 2) {
            val microappCode = parts[0].trim()
            val variableName = parts[1].trim()
            if (microappCode.isNotEmpty() && variableName.isNotEmpty()) {
                val resolved = contextManager.getMicroappVariable(microappCode, variableName)
                return resolved?.toString() ?: value
            }
        }
    }
    if (value.startsWith("@@{") && value.endsWith("}")) {
        val content = value.substring(3, value.length - 1).trim()
        if (content.isNotEmpty()) {
            val resolved = contextManager.getEngineVariable(content)
            return resolved?.toString() ?: value
        }
    }

    return value
}

