package com.example.drivenui.engine.generative_screen.binding

import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.parser.models.DataContext
import com.example.drivenui.engine.value.resolveValueExpression

/**
 * Полная подстановка для строки из модели: сначала `${...}` из [DataContext], затем выражения контекста
 * (`@{microapp.*}`, `@@{*}`, условные `*if*then*else*`).
 *
 * Шаблоны в полях модели не перезаписываются — результат кладётся в `display*` в `StyleResolver` (`resolveScreen`).
 *
 * @param template Исходная строка из XML/маппера
 * @param dataContext JSON и результаты запросов
 * @param contextManager Переменные микроаппа и движка
 * @return Строка для отображения или null, если [template] был null
 */
fun resolveTemplateString(
    template: String?,
    dataContext: DataContext,
    contextManager: IContextManager,
): String? {
    if (template == null) return null
    if (template.isEmpty()) return template
    val bindings = DataBindingParser.parseBindings(template)
    val afterJson = if (bindings.isEmpty()) {
        template
    } else {
        DataBindingParser.replaceBindings(template, bindings, dataContext)
    }
    return resolveValueExpression(afterJson, contextManager)
}
