package com.example.drivenui.engine.generative_screen.action

import com.example.drivenui.engine.generative_screen.models.ScreenModel

/**
 * Результат обработки действия.
 */
sealed class ActionResult {
    data object Success : ActionResult()

    /**
     * Результат навигации.
     *
     * @property isBack true, если произошёл переход назад по стеку навигации
     * @todo Рефакторинг структуры результата навигации
     */
    data class NavigationChanged(val isBack: Boolean) : ActionResult()

    /**
     * Изменение состояния нижней шторки (bottom sheet).
     * model == null означает закрытие шторки.
     *
     * @property model Модель экрана для шторки или null для закрытия
     */
    data class BottomSheetChanged(val model: ScreenModel?) : ActionResult()

    /**
     * Запрос выхода из микроаппа: стек навигации на корне, назад некуда (например [UiAction.Back] с первого экрана).
     */
    data object ExitMicroapp : ActionResult()

    /**
     * Ошибка при обработке действия.
     *
     * @property message Сообщение об ошибке
     * @property exception Исключение (если есть)
     */
    data class Error(val message: String, val exception: Exception? = null) : ActionResult()
}
