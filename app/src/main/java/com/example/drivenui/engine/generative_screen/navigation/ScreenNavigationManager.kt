package com.example.drivenui.engine.generative_screen.navigation

import androidx.compose.runtime.mutableStateListOf
import com.example.drivenui.engine.generative_screen.models.ScreenState

class ScreenNavigationManager {

    private val _navigationStack = mutableStateListOf<ScreenState>()
    val navigationStack: List<ScreenState> get() = _navigationStack


    fun pushScreen(screenState: ScreenState) {
        _navigationStack.add(screenState)
    }

    fun popScreen(): ScreenState? {
        if (_navigationStack.size > 1) {
            _navigationStack.removeLast()
            return _navigationStack.lastOrNull()
        }
        return null
    }

    fun getCurrentScreen(): ScreenState? {
        return _navigationStack.lastOrNull()
    }

    fun getPreviousScreen(): ScreenState? {
        if (_navigationStack.size > 1) {
            return _navigationStack[_navigationStack.size - 2]
        }
        return null
    }

    fun canNavigateBack(): Boolean = _navigationStack.size > 1

    fun updateCurrentScreen(screenState: ScreenState) {
        if (_navigationStack.isNotEmpty()) {
            _navigationStack[_navigationStack.size - 1] = screenState
        }
    }

    fun clear() {
        _navigationStack.clear()
    }
}