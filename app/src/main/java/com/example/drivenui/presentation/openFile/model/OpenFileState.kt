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

    /** Показать тестовый экран */
    data object OnShowTestScreen : OpenFileEvent

    /** Показать статистику биндингов */
    data object OnShowBindingStats : OpenFileEvent

    /** Загрузить JSON файлы */
    data object OnLoadJsonFiles : OpenFileEvent

    /** Выбрать JSON файлы */
    data class OnSelectJsonFiles(val files: List<String>) : OpenFileEvent
}

/** События с вью-модели на экран */
internal sealed interface OpenFileEffect : VtbEffect {

    /** Жмак по кнопке назад */
    data object GoBack : OpenFileEffect

    /** Навигация к деталям парсинга */
    data class NavigateToParsingDetails(val result: SDUIParser.ParsedMicroappResult) : OpenFileEffect

    /** Навигация к деталям парсинга */
    data class NavigateToTestScreen(val result: SDUIParser.ParsedMicroappResult) : OpenFileEffect

    /** Показать сообщение об ошибке */
    data class ShowError(val message: String) : OpenFileEffect

    /** Показать сообщение об успехе */
    data class ShowSuccess(val message: String) : OpenFileEffect

    /** Показать сообщение об успехе с информацией о биндингах */
    data class ShowSuccessWithBindings(
        val message: String,
        val bindingStats: Map<String, Any>?,
        val resolvedValues: Map<String, String>
    ) : OpenFileEffect

    /** Показать результат парсинга в диалоге */
    data class ShowParsingResultDialog(
        val title: String,
        val screensCount: Int,
        val textStylesCount: Int,
        val colorStylesCount: Int,
        val queriesCount: Int,
        val componentsCount: Int = 0,
        val hasComponentStructure: Boolean = false,
        val bindingsCount: Int = 0,
        val resolvedBindingsCount: Int = 0,
        val jsonFilesCount: Int = 0
    ) : OpenFileEffect

    /** Показать статистику биндингов */
    data class ShowBindingStats(
        val stats: Map<String, Any>?,
        val resolvedValues: Map<String, String>
    ) : OpenFileEffect

    /** Показать диалог выбора JSON файлов */
    data class ShowJsonFileSelectionDialog(
        val availableFiles: List<String>,
        val selectedFiles: List<String>
    ) : OpenFileEffect
}

/**
 * Состояние экрана с поддержкой новой структуры компонентов и биндингов
 *
 * @property isUploadFile состояние загрузки файла
 * @property isParsing состояние парсинга файла
 * @property parsingResult результат парсинга с новой структурой
 * @property availableFiles список доступных файлов
 * @property availableJsonFiles список доступных JSON файлов
 * @property selectedFileName выбранный файл
 * @property selectedJsonFiles выбранные JSON файлы для биндингов
 * @property errorMessage сообщение об ошибке
 * @property showJsonSelectionDialog показать диалог выбора JSON файлов
 */
internal data class OpenFileState(
    val isUploadFile: Boolean = false,
    val isParsing: Boolean = false,
    val parsingResult: SDUIParser.ParsedMicroappResult? = null,
    val availableFiles: List<String> = emptyList(),
    val availableJsonFiles: List<String> = emptyList(),
    val selectedFileName: String? = null,
    val selectedJsonFiles: List<String> = emptyList(),
    val errorMessage: String? = null,
    val showJsonSelectionDialog: Boolean = false,
    val bindingStats: Map<String, Any>? = null,
    val resolvedValues: Map<String, String> = emptyMap()
) : VtbState {

    /**
     * Проверяет, есть ли результат парсинга для отображения
     */
    val hasParsingResult: Boolean get() = parsingResult != null

    /**
     * Проверяет, есть ли JSON файлы для выбора
     */
    val hasJsonFiles: Boolean get() = availableJsonFiles.isNotEmpty()

    /**
     * Получает название микроаппа
     */
    val microappTitle: String get() = parsingResult?.microapp?.title ?: "Неизвестный микроапп"

    /**
     * Получает количество экранов
     */
    val screensCount: Int get() = parsingResult?.screens?.size ?: 0

    /**
     * Получает количество текстовых стилей
     */
    val textStylesCount: Int get() = parsingResult?.styles?.textStyles?.size ?: 0

    /**
     * Получает количество цветовых стилей
     */
    val colorStylesCount: Int get() = parsingResult?.styles?.colorStyles?.size ?: 0

    /**
     * Получает количество запросов API
     */
    val queriesCount: Int get() = parsingResult?.queries?.size ?: 0

    /**
     * Получает количество разрешенных биндингов
     */
    val resolvedBindingsCount: Int get() = parsingResult?.getResolvedValues()?.size ?: 0

    /**
     * Получает общее количество компонентов во всех экранах
     */
    val componentsCount: Int get() {
        var total = 0
        parsingResult?.screens?.forEach { screen ->
            screen.rootComponent?.let { root ->
                total += countComponents(root)
            }
        }
        return total
    }

    /**
     * Считает количество компонентов рекурсивно
     */
    private fun countComponents(component: com.example.drivenui.parser.models.Component): Int {
        var count = 1 // текущий компонент
        component.children.forEach { child ->
            count += countComponents(child)
        }
        return count
    }
}