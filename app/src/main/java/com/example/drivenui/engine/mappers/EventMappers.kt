package com.example.drivenui.engine.mappers

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.parser.models.EventAction
import com.example.drivenui.engine.parser.models.WidgetEvent

/**
 * Соответствие кодов событий (`eventCode`) целям в дереве UI:
 * - **Экран** — `onCreate`, `onDestroy` ([getOnCreateEvents], [getOnDestroyEvents])
 * - **Layout, button, label, image, appBar** — `onTap` ([getOnTapEvents]); у прочих виджетов onTap не маппится
 * - **Input** — задуманы `onTyping`, `onFinishTyping`, `onFocus`, `onFinishFocus`; в рендере пока обрабатывается только [getOnFinishTypingEvents]
 */

/**
 * Извлекает экшены события onTap
 *
 * @param events Список событий компонента
 * @return Список UiAction для onTap или пустой список
 */
fun getOnTapEvents(events: List<WidgetEvent>): List<UiAction> =
    events.find { it.eventCode == "onTap" }?.eventActions?.mapToUiActionsList()
        ?: emptyList()

/**
 * Извлекает экшены события onFinishTyping для input
 *
 * @param events Список событий виджета input
 * @return Список UiAction для onFinishTyping или пустой список
 */
fun getOnFinishTypingEvents(events: List<WidgetEvent>): List<UiAction> =
    events.find { it.eventCode == "onFinishTyping" }?.eventActions?.mapToUiActionsList()
        ?: emptyList()

/**
 * Извлекает экшены onCreate
 *
 * @param events Список событий экрана
 * @return Список UiAction для onCreate или пустой список
 */
fun getOnCreateEvents(events: List<WidgetEvent>): List<UiAction> =
    events.find { it.eventCode == "onCreate" }?.eventActions?.mapToUiActionsList()
        ?: emptyList()

/**
 * Извлекает экшены onDestroy
 *
 * @param events Список событий экрана
 * @return Список UiAction для onDestroy или пустой список
 */
fun getOnDestroyEvents(events: List<WidgetEvent>): List<UiAction> =
    events.find { it.eventCode == "onDestroy" }?.eventActions?.mapToUiActionsList()
        ?: emptyList()

/**
 * Преобразует список EventAction в список UiAction.
 *
 * @receiver Список EventAction
 * @return Список UiAction
 */
fun List<EventAction>.mapToUiActionsList(): List<UiAction> =
    map { it.mapToUiAction() }

/**
 * Преобразует EventAction в UiAction.
 *
 * @receiver EventAction
 * @return Соответствующий UiAction
 */
fun EventAction.mapToUiAction(): UiAction {
    return when (code.lowercase()) {
        "openscreen" -> {
            val screenCode = properties["screenCode"]
            if (!screenCode.isNullOrEmpty()) {
                UiAction.OpenScreen(screenCode)
            } else {
                UiAction.Empty
            }
        }
        "openbottomsheet" -> {
            val screenCode = properties["screenCode"]
            if (!screenCode.isNullOrEmpty()) {
                UiAction.OpenBottomSheet(screenCode)
            } else {
                UiAction.Empty
            }
        }
        "refreshscreen" -> {
            val screenCode = properties["screenCode"]
            if (!screenCode.isNullOrEmpty()) {
                UiAction.RefreshScreen(screenCode)
            } else {
                UiAction.Empty
            }
        }
        "refreshwidget" -> {
            val widgetCode = properties["screenLayoutWidgetCode"]
            if (!widgetCode.isNullOrEmpty()) {
                UiAction.RefreshWidget(widgetCode)
            } else {
                UiAction.Empty
            }
        }
        "refreshlayout" -> {
            val layoutCode = properties["screenLayoutCode"]
            if (!layoutCode.isNullOrEmpty()) {
                UiAction.RefreshLayout(layoutCode)
            } else {
                UiAction.Empty
            }
        }
        "deeplink" -> {
            val deeplink = properties["deeplink"]
            if (!deeplink.isNullOrEmpty()) {
                UiAction.OpenDeeplink(deeplink)
            } else {
                UiAction.Empty
            }
        }
        "query" -> {
            val queryCode = properties["queryCode"]?.trim().orEmpty()
            if (queryCode.isEmpty()) {
                UiAction.Empty
            } else {
                val type = properties["type"]?.trim()?.takeIf { it.isNotEmpty() } ?: "GET"
                val endpoint = properties["endpoint"]?.trim().orEmpty()
                val mockEnabled = properties["mockEnabled"]?.trim()?.takeIf { it.isNotEmpty() }?.toBoolean()
                    ?: true
                val mockFile = properties["mockFile"]?.trim()?.takeIf { it.isNotEmpty() }
                UiAction.ExecuteQuery(
                    queryCode = queryCode,
                    type = type,
                    endpoint = endpoint,
                    mockEnabled = mockEnabled,
                    mockFile = mockFile,
                )
            }
        }
        "datatransform" -> {
            val variableName = properties["variableName"]
            val newValue = properties["newVariableValue"]
            if (!variableName.isNullOrEmpty() && !newValue.isNullOrEmpty()) {
                UiAction.DataTransform(variableName, newValue)
            } else {
                UiAction.Empty
            }
        }
        "savetocontext" -> {
            val valueTo = properties["valueTo"]
            val valueFrom = properties["valueFrom"]
            if (!valueTo.isNullOrEmpty() && !valueFrom.isNullOrEmpty()) {
                UiAction.SaveToContext(valueTo, valueFrom)
            } else {
                UiAction.Empty
            }
        }
        "nativecode" -> {
            val actionCode = properties["actionCode"] ?: ""
            if (actionCode.isNotEmpty()) {
                val parameters = properties.filterKeys {
                    it != "actionCode"
                }
                UiAction.NativeCode(actionCode, parameters)
            } else {
                UiAction.Empty
            }
        }
        "navigateback" -> UiAction.Back
        else -> UiAction.Empty
    }
}