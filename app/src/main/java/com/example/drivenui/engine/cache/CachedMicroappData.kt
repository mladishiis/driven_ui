package com.example.drivenui.engine.cache

import com.example.drivenui.engine.parser.models.AllStyles
import com.example.drivenui.engine.parser.models.ScreenQuery

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

/**
 * Сериализуемое представление экрана с замапленным деревом компонентов.
 *
 * @property id Идентификатор экрана
 * @property deeplink Deeplink экрана
 * @property requests Список запросов экрана
 * @property rootComponent Корневой компонент экрана
 */
data class CachedScreenModel(
    val id: String,
    val deeplink: String = "",
    val requests: List<ScreenQuery>,
    val rootComponent: CachedComponentModel?,
)
