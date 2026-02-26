package com.example.drivenui.app.domain

/**
 * Источник данных микроаппа.
 *
 * - [ASSETS] — загрузка из assets приложения.
 * - [FILE_SYSTEM] — загрузка по QR (архив напрямую, Accept: application/octet-stream).
 * - [FILE_SYSTEM_JSON] — загрузка по QR (архив в JSON с base64, Accept: application/json).
 */
enum class MicroappSource {
    ASSETS,

    /** QR → архив напрямую (application/octet-stream) */
    FILE_SYSTEM,

    /** QR → JSON с archive в base64 (application/json) */
    FILE_SYSTEM_JSON,
}

internal fun MicroappSource.toArchiveDownloadFormat(): ArchiveDownloadFormat? = when (this) {
    MicroappSource.ASSETS -> null
    MicroappSource.FILE_SYSTEM -> ArchiveDownloadFormat.OCTET_STREAM
    MicroappSource.FILE_SYSTEM_JSON -> ArchiveDownloadFormat.JSON
}