package com.example.drivenui.app.presentation.details.model

import com.example.drivenui.engine.uirender.models.ComponentModel

/**
 * Данные для вкладок экрана деталей (вычисляются из parsedResult).
 * Хранятся в state, чтобы не передавать ViewModel в composable.
 *
 * @property screens список экранов
 * @property textStyles стили текста
 * @property colorStyles стили цвета
 * @property events события
 * @property widgets виджеты
 * @property layouts лэйауты
 * @property eventActions действия событий
 * @property componentModelForRender модель первого экрана для рендеринга
 */
internal data class DetailsTabData(
    val screens: List<ScreenItem> = emptyList(),
    val textStyles: List<TextStyleItem> = emptyList(),
    val colorStyles: List<ColorStyleItem> = emptyList(),
    val events: List<EventItem> = emptyList(),
    val widgets: List<WidgetItem> = emptyList(),
    val layouts: List<LayoutItem> = emptyList(),
    val eventActions: List<EventActionItem> = emptyList(),
    val componentModelForRender: ComponentModel? = null,
)
