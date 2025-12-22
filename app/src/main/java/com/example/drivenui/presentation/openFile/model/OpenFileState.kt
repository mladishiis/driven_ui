package com.example.drivenui.presentation.openFile.model

import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.utile.VtbEffect
import com.example.drivenui.utile.VtbEvent
import com.example.drivenui.utile.VtbState

/** События на экране */
internal sealed interface OpenFileEvent : VtbEvent {

    /** Жмак по кнопке назад */
    data object OnBackClick : OpenFileEvent

    /** Жмак по кнопке Загрузить файл */
    data object OnUploadFile : OpenFileEvent

    /** Показать файл */
    data object OnShowFile : OpenFileEvent

    /** Показать детали парсинга */
    data object OnShowParsingDetails : OpenFileEvent
}

/** События с вью-модели на экран */
internal sealed interface OpenFileEffect : VtbEffect {

    /** Жмак по кнопке назад */
    data object GoBack : OpenFileEffect

    /** Навигация к деталям парсинга */
    data class NavigateToParsingDetails(val result: SDUIParser.ParsedMicroapp) : OpenFileEffect

    /** Показать сообщение об ошибке */
    data class ShowError(val message: String) : OpenFileEffect

    /** Показать сообщение об успехе */
    data class ShowSuccess(val message: String) : OpenFileEffect

    /** Показать результат парсинга в диалоге */
    data class ShowParsingResultDialog(
        val title: String,
        val screensCount: Int,
        val textStylesCount: Int,
        val colorStylesCount: Int,
        val queriesCount: Int
    ) : OpenFileEffect
}

/**
 * Состояние экрана
 *
 * @property isUploadFile состояние загрузки файла
 * @property isParsing состояние парсинга файла
 * @property parsingResult результат парсинга
 * @property availableFiles список доступных файлов
 * @property selectedFileName выбранный файл
 */
internal data class OpenFileState(
    val isUploadFile: Boolean = false,
    val isParsing: Boolean = false,
    val parsingResult: SDUIParser.ParsedMicroapp? = null,
    val availableFiles: List<String> = emptyList(),
    val selectedFileName: String? = null,
    val errorMessage: String? = null,
) : VtbState {

    /**
     * Проверяет, есть ли результат парсинга для отображения
     */
    val hasParsingResult: Boolean get() = parsingResult != null

    /**
     * Получает название микроаппа
     */
    val microappTitle: String get() = parsingResult?.microapp?.title ?: "Неизвестный микроапп"

    /**
     * Получает количество экранов
     */
    val screensCount: Int get() = parsingResult?.screens?.size ?: 0

    /**
     * Получает количество стилей текста
     */
    val textStylesCount: Int get() = parsingResult?.styles?.textStyles?.size ?: 0

    /**
     * Получает количество стилей цвета
     */
    val colorStylesCount: Int get() = parsingResult?.styles?.colorStyles?.size ?: 0

    /**
     * Получает количество запросов
     */
    val queriesCount: Int get() = parsingResult?.queries?.size ?: 0
}