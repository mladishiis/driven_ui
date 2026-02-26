package com.example.drivenui.app.domain

import com.google.gson.annotations.SerializedName

/**
 * Ответ сервера при Accept: application/json.
 * ```json
 * {"archive": "base64...", "microappCode": "string"}
 * ```
 */
internal data class ArchiveJsonResponse(
    @SerializedName("archive") val archiveBase64: String,
    @SerializedName("microappCode") val microappCode: String? = null,
)
