package com.example.drivenui.engine.uirender.renderer

import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutForParams
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.uirender.models.RadiusValues

/**
 * Заменяет `{#forIndexName}` на конкретный индекс во всех строках компонента.
 * Используется в LazyColumn/LazyRow для рендеринга шаблонов с переменным индексом.
 */
internal fun expandComponentWithIndex(
    component: ComponentModel,
    forIndexName: String,
    index: String,
): ComponentModel {
    val pattern = "{#${forIndexName}}"
    fun String?.replaceIndex(): String? = this?.replace(pattern, index)
    fun String.replaceIndex(): String = this.replace(pattern, index)

    return when (component) {
        is LayoutModel -> {
            component.copy(
                children = component.children.map { child ->
                    expandComponentWithIndex(child, forIndexName, index)
                },
                backgroundColorStyleCode = component.backgroundColorStyleCode.replaceIndex(),
                radiusValues = component.radiusValues.let { rv ->
                    RadiusValues(
                        radius = rv.radius.replaceIndex(),
                        radiusTop = rv.radiusTop.replaceIndex(),
                        radiusBottom = rv.radiusBottom.replaceIndex(),
                    )
                },
                forParams = component.forParams.let { forParams ->
                    LayoutForParams(
                        forIndexName = forParams.forIndexName,
                        maxForIndex = forParams.maxForIndex.replaceIndex(),
                    )
                },
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        is LabelModel -> {
            component.copy(
                text = component.text.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                textStyleCode = component.textStyleCode.replaceIndex(),
                colorStyleCode = component.colorStyleCode.replaceIndex(),
                textAlignment = component.textAlignment.replaceIndex(),
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        is ButtonModel -> {
            component.copy(
                text = component.text.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                radiusValues = component.radiusValues.let { rv ->
                    RadiusValues(
                        radius = rv.radius.replaceIndex(),
                        radiusTop = rv.radiusTop.replaceIndex(),
                        radiusBottom = rv.radiusBottom.replaceIndex(),
                    )
                },
                textStyleCode = component.textStyleCode.replaceIndex(),
                colorStyleCode = component.colorStyleCode.replaceIndex(),
                backgroundColorStyleCode = component.backgroundColorStyleCode.replaceIndex(),
                textAlignment = component.textAlignment.replaceIndex(),
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        is AppBarModel -> {
            component.copy(
                title = component.title.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                textStyleCode = component.textStyleCode.replaceIndex(),
                colorStyleCode = component.colorStyleCode.replaceIndex(),
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        is InputModel -> {
            component.copy(
                text = component.text.replaceIndex(),
                hint = component.hint.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        is ImageModel -> {
            component.copy(
                url = component.url.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                colorStyleCode = component.colorStyleCode.replaceIndex(),
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        else -> component
    }
}
