package com.example.drivenui.app.presentation.details.model

import com.example.drivenui.utile.VtbEffect

/** События с вью-модели на экран */
internal sealed interface DetailsEffect : VtbEffect {

    /** Жмак по кнопке назад */
    data object GoBack : DetailsEffect

    /** Показать сообщение */
    data class ShowMessage(val message: String) : DetailsEffect

    /** Показать сообщение о копировании */
    data class ShowCopiedMessage(val text: String) : DetailsEffect

    /** Успешный экспорт */
    data class ShowExportSuccess(val filePath: String) : DetailsEffect

    /** Показать структуру компонентов */
    data class ShowComponentStructure(
        val title: String,
        val structureInfo: String
    ) : DetailsEffect

    /** Показать компоненты экрана */
    data class ShowScreenComponents(
        val screenTitle: String,
        val components: List<ComponentTreeItem>
    ) : DetailsEffect
}
