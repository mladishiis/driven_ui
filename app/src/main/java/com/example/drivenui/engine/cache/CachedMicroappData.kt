package com.example.drivenui.engine.cache

import com.example.drivenui.engine.parser.models.AllStyles

/**
 * Сериализуемое представление замапленного микроаппа.
 * Сохраняется вместо ParsedMicroappResult для быстрой загрузки без повторного маппинга.
 *
 * @property microappCode Код микроаппа
 * @property microappTitle Заголовок микроаппа
 * @property microappDeeplink Deeplink микроаппа
 * @property allStyles Все стили микроаппа
 * @property screens Список кэшированных экранов
 */
data class CachedMicroappData(
    val microappCode: String,
    val microappTitle: String,
    val microappDeeplink: String = "",
    val allStyles: AllStyles?,
    val screens: List<CachedScreenModel>,
) {
    /**
     * Проверяет наличие полезных данных в кэше.
     *
     * @return `true` если есть код микроаппа, экраны или стили
     */
    fun hasData(): Boolean =
        microappCode.isNotBlank() || screens.isNotEmpty() || allStyles != null
}
