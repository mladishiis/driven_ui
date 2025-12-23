package com.example.drivenui.navigation

import com.example.drivenui.parser.SDUIParserNew

/**
 * Менеджер навигации для передачи данных между фрагментами
 * Поддерживает как старую, так и новую структуру данных
 */
object NavigationManager {

    private var sharedData: SDUIParserNew.ParsedMicroappResult? = null
    private var sharedOldData: com.example.drivenui.parser.SDUIParser.ParsedMicroapp? = null

    // ===== Новая структура данных =====

    /**
     * Сохраняет данные для следующего экрана (новая структура)
     */
    fun setDataForNextScreen(data: SDUIParserNew.ParsedMicroappResult) {
        sharedData = data
        sharedOldData = null // Очищаем старые данные
    }

    /**
     * Получает и очищает сохраненные данные (новая структура)
     */
    fun getAndClearData(): SDUIParserNew.ParsedMicroappResult? {
        return sharedData.also {
            sharedData = null
            sharedOldData = null // Очищаем и старые данные при запросе новых
        }
    }

    /**
     * Получает данные без очистки (новая структура)
     */
    fun peekData(): SDUIParserNew.ParsedMicroappResult? = sharedData

    // ===== Общие методы =====

    /**
     * Очищает все сохраненные данные
     */
    fun clearAllData() {
        sharedData = null
        sharedOldData = null
    }

    /**
     * Проверяет, есть ли сохраненные данные
     */
    fun hasData(): Boolean = sharedData != null || sharedOldData != null

    /**
     * Проверяет, есть ли сохраненные данные новой структуры
     */
    fun hasNewData(): Boolean = sharedData != null

    /**
     * Проверяет, есть ли сохраненные данные старой структуры
     */
    fun hasOldData(): Boolean = sharedOldData != null

    // ===== Методы для удобной работы с компонентами =====

    /**
     * Получает первый экран из сохраненных данных (новая структура)
     */
    fun getFirstScreen(): com.example.drivenui.parser.models.ParsedScreen? {
        return sharedData?.screens?.firstOrNull()
    }

    /**
     * Получает экран по коду (новая структура)
     */
    fun getScreenByCode(screenCode: String): com.example.drivenui.parser.models.ParsedScreen? {
        return sharedData?.screens?.firstOrNull { it.screenCode == screenCode }
    }

    /**
     * Получает микроапп из сохраненных данных
     */
    fun getMicroapp(): com.example.drivenui.parser.models.Microapp? {
        return sharedData?.microapp ?: sharedOldData?.microapp
    }

    /**
     * Получает стили из сохраненных данных
     */
    fun getStyles(): com.example.drivenui.parser.models.AllStyles? {
        return sharedData?.styles ?: sharedOldData?.styles
    }

    /**
     * Получает статистику для отображения
     */
    fun getParsingStats(): Map<String, Any>? {
        return if (sharedData != null) {
            // Новая структура
            mapOf(
                "structure" to "new",
                "microapp" to (sharedData?.microapp?.title ?: "не найден"),
                "screens" to (sharedData?.screens?.size ?: 0),
                "hasComponentStructure" to (sharedData?.screens?.any { it.rootComponent != null } == true),
                "totalComponents" to (sharedData?.countAllComponents() ?: 0)
            )
        } else if (sharedOldData != null) {
            // Старая структура
            mapOf(
                "structure" to "old",
                "microapp" to (sharedOldData?.microapp?.title ?: "не найден"),
                "screens" to (sharedOldData?.screens?.size ?: 0),
                "hasComponentStructure" to false,
                "totalComponents" to 0
            )
        } else {
            null
        }
    }
}