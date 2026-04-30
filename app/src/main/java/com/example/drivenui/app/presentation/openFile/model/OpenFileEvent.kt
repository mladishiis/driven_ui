package com.example.drivenui.app.presentation.openFile.model

import com.example.drivenui.utile.VtbEvent

/** События на экране */
internal sealed interface OpenFileEvent : VtbEvent {

    /** Жмак по кнопке назад */
    data object OnBackClick : OpenFileEvent

    /** Загрузка конфигурации (микроапп или шаблон) */
    data object OnUpload : OpenFileEvent

    /** Очистить коллекцию (микроаппы из коллекции + ID коллекции) */
    data object OnClearCollection : OpenFileEvent

    /** Очистить список прототипов (микроаппы, добавленные по одному) */
    data object OnClearSingleList : OpenFileEvent

    /** Показать файл */
    data object OnShowFile : OpenFileEvent

    /** Показать детали парсинга */
    data object OnShowParsingDetails : OpenFileEvent

    /** Показать тестовый экран (открыть микроапп по коду) */
    data class OnShowTestScreen(val microappCode: String) : OpenFileEvent

    /** Показать статистику биндингов */
    data object OnShowBindingStats : OpenFileEvent

    /** Загрузить JSON файлы */
    data object OnLoadJsonFiles : OpenFileEvent

    /** Выбрать JSON файлы */
    data class OnSelectJsonFiles(val files: List<String>) : OpenFileEvent

    /** QR успешно отсканирован */
    data class OnQrScanned(val url: String) : OpenFileEvent

    /** Загрузить шаблон в режиме скриншотов */
    data object OnUploadTemplate : OpenFileEvent

    /** Добавить коллекцию прототипов (открыть сканер QR с ID коллекции) */
    data object OnAddCollection : OpenFileEvent

    /** QR с ID коллекции успешно отсканирован */
    data class OnQrScannedCollectionId(val collectionId: String) : OpenFileEvent
}
