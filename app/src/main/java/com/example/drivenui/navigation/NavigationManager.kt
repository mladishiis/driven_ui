package com.example.drivenui.navigation

import com.example.drivenui.parser.SDUIParser

/**
 * Менеджер навигации для передачи данных между фрагментами
 */
object NavigationManager {

    private var sharedData: SDUIParser.ParsedMicroappResult? = null

    /**
     * Сохраняет данные для следующего экрана (новая структура)
     */
    fun setDataForNextScreen(data: SDUIParser.ParsedMicroappResult) {
        sharedData = data
    }

    /**
     * Получает и очищает сохраненные данные (новая структура)
     */
    fun getAndClearData(): SDUIParser.ParsedMicroappResult? {
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