package com.example.drivenui.engine.generative_screen.mapper

import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.mappers.mapParsedScreenToUI
import com.example.drivenui.parser.models.AllStyles
import com.example.drivenui.parser.models.ParsedScreen

class ScreenMapper(
    private val styleRegistry: ComposeStyleRegistry
) {

    fun mapToScreenModel(
        parsedScreen: ParsedScreen,
        requests: List<String> = emptyList()
    ): ScreenModel {
        return ScreenModel(
            id = parsedScreen.screenCode,
            requests = parsedScreen.requests,
            rootComponent = mapParsedScreenToUI(parsedScreen, styleRegistry)
        )
    }

    companion object {
        fun create(
            styles: AllStyles?
        ): ScreenMapper {
            return ScreenMapper(
                ComposeStyleRegistry(styles)
            )
        }
    }
}
