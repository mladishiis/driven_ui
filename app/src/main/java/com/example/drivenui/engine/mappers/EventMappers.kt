package com.example.drivenui.engine.mappers

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.parser.models.EventAction
import com.example.drivenui.parser.models.WidgetEvent

fun getOnTapEvents(events: List<WidgetEvent>): List<UiAction> =
    events.find { it.eventCode == "onTap" }?.eventActions?.mapToUiActionsList()
        ?: emptyList()

fun getOnFinishTypingEvents(events: List<WidgetEvent>): List<UiAction> =
    events.find { it.eventCode == "onFinishTyping" }?.eventActions?.mapToUiActionsList()
        ?: emptyList()

fun List<EventAction>.mapToUiActionsList(): List<UiAction> =
    map { it.mapToUiAction() }

fun EventAction.mapToUiAction(): UiAction {
    return when (code.lowercase()) {
        "openscreen" -> {
            val screenCode = properties["screenCode"] // Используем Map
            if (!screenCode.isNullOrEmpty()) {
                UiAction.OpenScreen(screenCode)
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
            val queryCode = properties["queryCode"]
            if (!queryCode.isNullOrEmpty()) {
                UiAction.ExecuteQuery(queryCode)
            } else {
                UiAction.Empty
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
        "previousscreen" -> UiAction.Back
        else -> UiAction.Empty
    }
}