package com.example.drivenui.engine.generative_screen

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenui.engine.generative_screen.models.GenerativeUiState
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.generative_screen.models.ScreenState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.mappers.mapParsedScreenToUI
import com.example.drivenui.parser.models.AllStyles
import com.example.drivenui.parser.models.ParsedScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenerativeScreenViewModel() : ViewModel() {

    private var parsedScreens: List<ParsedScreen>? = null
    private var allStyles: AllStyles? = null

    private val _uiState = MutableStateFlow<GenerativeUiState>(GenerativeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _navigationStack = mutableStateListOf<ScreenState>()
    val navigationStack: List<ScreenState> get() = _navigationStack

    //TODO: здесь нужно, чтобы приходил список ScreenModel с подставленными запросами
    fun setParsedResult(
        parsedScreens: List<ParsedScreen>,
        allStyles: AllStyles?,
    ) {
        this.parsedScreens = parsedScreens
        this.allStyles = allStyles
        loadInitialScreen()
    }

    // Тут пока первый экран - это первый экран в массиве
    private fun loadInitialScreen() {
        val firstScreen = parsedScreens?.firstOrNull()
        val registry = ComposeStyleRegistry(allStyles)
        if (firstScreen != null) {
            val screenModel = ScreenModel(
                id = firstScreen.screenCode,
                requests = emptyList(),
                rootComponent = mapParsedScreenToUI(firstScreen, registry)
            )
            navigateToScreen(screenModel)
        }
    }

    fun handleAction(action: UiAction) {
        viewModelScope.launch {
            when (action) {
                is UiAction.OpenScreen -> handleNavigation(action)
                is UiAction.Back -> navigateBack()
                else -> {}
            }
        }
    }

    private fun handleNavigation(action: UiAction.OpenScreen) {
        try {
            findScreen(action.screenCode)?.also {
                navigateToScreen(it)
            }
        } catch (e: Exception) {
            // TODO: Добавить ошибку
        }
    }

    private fun navigateToScreen(screen: ScreenModel) {
        _navigationStack.add(ScreenState.fromDefinition(screen))

        if (screen.rootComponent != null) {
            _uiState.value = GenerativeUiState.Screen(screen.rootComponent)
        }
    }

    private fun findScreen(id: String): ScreenModel? {
        val registry = ComposeStyleRegistry(allStyles)
        return parsedScreens?.find { it.screenCode == id }
            ?.let {
                ScreenModel(
                    id = id,
                    requests = emptyList(),
                    rootComponent = mapParsedScreenToUI(it, registry)
                )
            }
    }

    fun navigateBack(): Boolean {
        if (_navigationStack.size > 1) {
            _navigationStack.removeLast()
            val previous = _navigationStack.last()
            _uiState.value = GenerativeUiState.Screen(
                previous.definition?.rootComponent
            )
            return true
        }
        return false
    }
}