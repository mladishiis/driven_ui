package com.example.drivenui.engine.generative_screen.navigation

import com.example.drivenui.engine.generative_screen.models.ScreenState

/**
 * Стек навигации SDUI-экранов внутри микроаппа.
 */
class ScreenNavigationManager {

    private val navigationStack = ArrayDeque<ScreenState>()

    /** Текущий стек (копия для чтения). */
    val stackSnapshot: List<ScreenState>
        get() = navigationStack.toList()

    fun pushScreen(screenState: ScreenState) {
        navigationStack.addLast(screenState)
    }

    fun popScreen(): ScreenState? {
        if (navigationStack.size <= 1) return null
        navigationStack.removeLast()
        return navigationStack.lastOrNull()
    }

    fun getCurrentScreen(): ScreenState? =
        navigationStack.lastOrNull()

    fun getPreviousScreen(): ScreenState? =
        navigationStack.dropLast(1).lastOrNull()

    fun canNavigateBack(): Boolean =
        navigationStack.size > 1

    fun updateCurrentScreen(screenState: ScreenState) {
        if (navigationStack.isEmpty()) return
        navigationStack[navigationStack.lastIndex] = screenState
    }

    fun clear() {
        navigationStack.clear()
    }
}
