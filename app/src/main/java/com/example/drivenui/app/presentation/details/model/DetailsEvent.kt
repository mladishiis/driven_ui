package com.example.drivenui.app.presentation.details.model

import com.example.drivenui.utile.VtbEvent

/** События на экране */
internal sealed interface DetailsEvent : VtbEvent {

    /** Жмак по кнопке назад */
    data object OnBackClick : DetailsEvent

    /** Жмак по кнопке обновления */
    data object OnRefreshClick : DetailsEvent

    /** Выбор вкладки */
    data class OnTabSelected(val tabIndex: Int) : DetailsEvent

    /** Раскрытие/сворачивание секции */
    data class OnSectionExpanded(val sectionId: String, val isExpanded: Boolean) : DetailsEvent

    /** Копирование в буфер обмена */
    data class OnCopyToClipboard(val text: String) : DetailsEvent

    /** Экспорт данных */
    data object OnExportData : DetailsEvent

    /** Показать структуру компонентов */
    data object OnShowComponentStructure : DetailsEvent

    /** Показать компоненты экрана */
    data class OnShowScreenComponents(val screenCode: String) : DetailsEvent
}
