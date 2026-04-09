package com.example.drivenui.app.presentation.openFile.model

import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.parser.SDUIParser
import com.example.drivenui.utile.VtbEffect

/** События с вью-модели на экран */
internal sealed interface OpenFileEffect : VtbEffect {

    /** Жмак по кнопке назад */
    data object GoBack : OpenFileEffect

    /** Навигация к деталям парсинга */
    data class NavigateToParsingDetails(val result: SDUIParser.ParsedMicroappResult) : OpenFileEffect

    /** Навигация к тестовому экрану рендеринга */
    data class NavigateToTestScreen(val mappedData: CachedMicroappData) : OpenFileEffect

    /** Показать сообщение об ошибке */
    data class ShowError(val message: String) : OpenFileEffect

    /** Показать сообщение об успехе */
    data class ShowSuccess(val message: String) : OpenFileEffect

    /** Показать диалог результата парсинга (успех) с переходом к деталям */
    data class ShowParsingSuccessDialog(
        val microappTitle: String,
        val screensCount: Int,
        val textStylesCount: Int,
        val colorStylesCount: Int,
        val componentsCount: Int,
        val hasBindings: Boolean,
        val jsonFilesCount: Int,
    ) : OpenFileEffect

    /** Показать диалог результата парсинга (ошибка) */
    data class ShowParsingErrorDialog(val message: String) : OpenFileEffect

    /** Открыть экран сканирования QR для одного микроаппа */
    data object OpenQrScanner : OpenFileEffect

    /** Открыть экран сканирования QR для ID коллекции */
    data object OpenQrScannerForCollection : OpenFileEffect

    /** Показать сообщение об успехе с информацией о биндингах */
    data class ShowSuccessWithBindings(
        val message: String,
        val bindingStats: Map<String, Any>?,
        val resolvedValues: Map<String, String>,
    ) : OpenFileEffect

    /** Показать статистику биндингов */
    data class ShowBindingStats(
        val stats: Map<String, Any>?,
        val resolvedValues: Map<String, String>,
    ) : OpenFileEffect

    /** Показать диалог выбора JSON файлов */
    data class ShowJsonFileSelectionDialog(
        val availableFiles: List<String>,
        val selectedFiles: List<String>,
    ) : OpenFileEffect
}
