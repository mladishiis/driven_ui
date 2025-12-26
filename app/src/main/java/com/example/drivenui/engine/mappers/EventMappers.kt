package com.example.drivenui.engine.mappers

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.parser.models.EventAction
import com.example.drivenui.parser.models.WidgetEvent


fun getOnTapEvents(events: List<WidgetEvent>): List<UiAction> =
//    listOf(
//        UiAction.OpenScreen("cardActivate")
//    )
    events.find { it.eventCode == "onTap" }?.eventActions?.mapToUiActionsList()
        ?: emptyList()


fun List<EventAction>.mapToUiActionsList(): List<UiAction> =
    map {
        it.mapToUiAction()
    }

fun EventAction.mapToUiAction(): UiAction {
    return when (code.lowercase()) {
        "openscreen" -> {
            val screenCode = properties.find { it.code == "screenCode" }
            if (screenCode != null) {
                UiAction.OpenScreen(screenCode.value)
            } else {
                UiAction.Empty
            }
        }
        // TODO остальные события
        else -> UiAction.Empty
    }
}