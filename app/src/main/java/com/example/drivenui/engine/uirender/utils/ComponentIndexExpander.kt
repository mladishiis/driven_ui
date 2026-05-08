package com.example.drivenui.engine.uirender.utils

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
 * Подставляет в строковые поля компонента плейсхолдер цикла `{#<имя>}` на фактическое значение [index].
 *
 * Паттерн строится как `"{#" + forIndexName + "}"` (например при `forIndexName == "carrier_index"`
 * ищется подстрока `{#carrier_index}` и заменяется на [index]).
 *
 * Рекурсивно обходит дочерние элементы у [LayoutModel]. Для остальных поддерживаемых типов
 * (Label, Button, AppBar, Input, Image) заменяются только перечисленные в реализации поля.
 * Неподдерживаемые подтипы [ComponentModel] возвращаются без изменений.
 *
 * Используется в шаблонах `verticalFor` / `horizontalFor` (LazyColumn/LazyRow).
 *
 * @param component корень или узел дерева компонентов
 * @param forIndexName имя переменной индекса из разметки (как в `<forIndexName>`, без фигурных скобок)
 * @param index строковое значение текущей итерации (обычно `"0"`, `"1"`, …)
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
                strokeWidth = component.strokeWidth.replaceIndex(),
                strokeColorStyleCode = component.strokeColorStyleCode.replaceIndex(),
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
                        resolvedMaxForIndex = forParams.resolvedMaxForIndex.replaceIndex(),
                    )
                },
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        is LabelModel -> {
            component.copy(
                text = component.text.replaceIndex(),
                displayText = component.displayText.replaceIndex(),
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
                displayText = component.displayText.replaceIndex(),
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
                stroke = component.stroke.copy(
                    width = component.stroke.width.replaceIndex(),
                    colorStyleCode = component.stroke.colorStyleCode.replaceIndex(),
                    resolvedWidthDp = null,
                    resolvedColor = null,
                ),
                textAlignment = component.textAlignment.replaceIndex(),
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        is AppBarModel -> {
            component.copy(
                title = component.title.replaceIndex(),
                displayTitle = component.displayTitle.replaceIndex(),
                iconLeftUrl = component.iconLeftUrl.replaceIndex(),
                displayIconLeftUrl = component.displayIconLeftUrl.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                textStyleCode = component.textStyleCode.replaceIndex(),
                colorStyleCode = component.colorStyleCode.replaceIndex(),
                leftIconColorStyleCode = component.leftIconColorStyleCode.replaceIndex(),
                backgroundColorStyleCode = component.backgroundColorStyleCode.replaceIndex(),
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        is InputModel -> {
            component.copy(
                text = component.text.replaceIndex(),
                hint = component.hint.replaceIndex(),
                displayText = component.displayText.replaceIndex(),
                displayHint = component.displayHint.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                tapActions = component.tapActions,
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        is ImageModel -> {
            component.copy(
                url = component.url.replaceIndex(),
                displayUrl = component.displayUrl.replaceIndex(),
                widgetCode = component.widgetCode.replaceIndex(),
                colorStyleCode = component.colorStyleCode.replaceIndex(),
                visibilityCode = component.visibilityCode.replaceIndex(),
            )
        }
        else -> component
    }
}
