package com.example.drivenui.engine.generative_screen.models

/**
 * Состояние экрана в стеке навигации.
 *
 * @property id Код экрана
 * @property definition Фаза 1: шаблон с сырыми строками и кодами стилей
 * @property presentation Фаза 2: resolved-дерево для Compose; null до первой сборки
 * @property data Дополнительные данные экрана (зарезервировано)
 * @property timestamp Время последнего обновления состояния
 */
data class ScreenState(
    val id: String,
    val definition: ScreenDefinition,
    val presentation: ScreenPresentation? = null,
    val data: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
) {
    companion object {
        /**
         * Создаёт состояние экрана из definition и готовой presentation.
         *
         * @param definition шаблон экрана
         * @param presentation готовое дерево или null до первой сборки
         * @return состояние для стека навигации
         */
        fun create(
            definition: ScreenDefinition,
            presentation: ScreenPresentation? = null,
        ): ScreenState =
            ScreenState(
                id = definition.id,
                definition = definition,
                presentation = presentation,
            )
    }
}
