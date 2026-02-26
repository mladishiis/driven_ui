package com.example.drivenui.app.domain

/**
 * Формат ответа при скачивании архива по QR-ссылке.
 *
 * - [OCTET_STREAM] — архив приходит напрямую (Accept: application/octet-stream).
 * - [JSON] — ответ в виде JSON `{"archive": "base64...", "microappCode": "..."}` (Accept: application/json).
 */
enum class ArchiveDownloadFormat {
    /** Архив приходит как бинарные данные */
    OCTET_STREAM,

    /** Архив закодирован в base64 внутри JSON */
    JSON,
}
