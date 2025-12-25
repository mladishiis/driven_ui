package com.example.drivenui.navigation

import com.example.drivenui.parser.SDUIParserNew

/**
 * Менеджер навигации для передачи данных между фрагментами
 */
object NavigationManager {

    private var sharedData: SDUIParserNew.ParsedMicroappResult? = null

    /**
     * Сохраняет данные для следующего экрана (новая структура)
     */
    fun setDataForNextScreen(data: SDUIParserNew.ParsedMicroappResult) {
        sharedData = data
    }

    /**
     * Получает и очищает сохраненные данные (новая структура)
     */
    fun getAndClearData(): SDUIParserNew.ParsedMicroappResult? {
        return sharedData.also {
            sharedData = null
        }
    }

    /**
     * Очищает все сохраненные данные
     */
    fun clearAllData() {
        sharedData = null
    }
}