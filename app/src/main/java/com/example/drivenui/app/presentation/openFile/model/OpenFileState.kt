package com.example.drivenui.app.presentation.openFile.model

import com.example.drivenui.app.domain.MicroappSource
import com.example.drivenui.engine.parser.SDUIParser
import com.example.drivenui.utile.VtbState

/**
 * Состояние экрана выбора и загрузки микроаппа.
 *
 * @property microappSource источник микроаппа (assets, file system)
 * @property collectionMicroapps микроаппы из коллекции
 * @property singleMicroapps микроаппы, добавленные по одному
 * @property isUploadFile идёт загрузка файла
 * @property isParsing идёт парсинг
 * @property isSyncingCollection идёт синхронизация коллекции
 * @property parsingResult результат парсинга
 * @property availableFiles доступные файлы
 * @property availableJsonFiles доступные JSON файлы для биндингов
 * @property selectedFileName выбранный файл
 * @property selectedJsonFiles выбранные JSON файлы
 * @property errorMessage сообщение об ошибке
 * @property showJsonSelectionDialog показать диалог выбора JSON
 * @property bindingStats статистика биндингов
 * @property resolvedValues разрешённые значения биндингов
 */
internal data class OpenFileState(
    val microappSource: MicroappSource = MicroappSource.ASSETS,
    val collectionMicroapps: List<MicroappItem> = emptyList(),
    val singleMicroapps: List<MicroappItem> = emptyList(),
    val isUploadFile: Boolean = false,
    val isParsing: Boolean = false,
    val isSyncingCollection: Boolean = false,
    val parsingResult: SDUIParser.ParsedMicroappResult? = null,
    val availableFiles: List<String> = emptyList(),
    val availableJsonFiles: List<String> = emptyList(),
    val selectedFileName: String? = null,
    val selectedJsonFiles: List<String> = emptyList(),
    val errorMessage: String? = null,
    val showJsonSelectionDialog: Boolean = false,
    val bindingStats: Map<String, Any>? = null,
    val resolvedValues: Map<String, String> = emptyMap(),
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
    private fun countComponents(component: com.example.drivenui.engine.parser.models.Component): Int {
        var count = 1
        component.children.forEach { child ->
            count += countComponents(child)
        }
        return count
    }
}