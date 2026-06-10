package com.example.drivenui.engine.generative_screen.models

import com.example.drivenui.engine.uirender.models.ComponentModel

/**
 * Строится из [ScreenDefinition] при смене данных (запрос, refresh, навигация).
 * Compose читает только presentation; биндинги и стили резолвятся до попадания сюда.
 *
 * @property screenId Код экрана (совпадает с [ScreenDefinition.id])
 * @property dataEpoch Версия данных; увеличивается при каждой пересборке presentation
 * @property rootComponent Resolved-дерево для [ComponentRenderer]
 */
data class ScreenPresentation(
    val screenId: String,
    val dataEpoch: Long,
    val rootComponent: ComponentModel? = null,
)
