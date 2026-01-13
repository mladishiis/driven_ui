package com.example.drivenui.engine.generative_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenui.engine.generative_screen.action.ActionHandler
import com.example.drivenui.engine.generative_screen.action.ActionResult
import com.example.drivenui.engine.generative_screen.action.ExternalDeeplinkHandler
import com.example.drivenui.engine.generative_screen.action.ScreenProvider
import com.example.drivenui.engine.generative_screen.context.ScreenContextManager
import com.example.drivenui.engine.generative_screen.mapper.ScreenMapper
import com.example.drivenui.engine.generative_screen.models.GenerativeUiState
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.generative_screen.models.ScreenState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.generative_screen.navigation.ScreenNavigationManager
import com.example.drivenui.parser.models.AllStyles
import com.example.drivenui.parser.models.ParsedScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenerativeScreenViewModel @Inject constructor(
    private val externalDeeplinkHandler: ExternalDeeplinkHandler
) : ViewModel() {

    private var parsedScreens: List<ParsedScreen>? = null
    private var allStyles: AllStyles? = null

    private val contextManager = ScreenContextManager()
    private val navigationManager = ScreenNavigationManager()
    private var screenMapper: ScreenMapper? = null
    private var actionHandler: ActionHandler? = null
    private var screenProvider: ScreenProviderImpl? = null

    private val _uiState = MutableStateFlow<GenerativeUiState>(GenerativeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    val navigationStack: List<ScreenState> get() = navigationManager.navigationStack

    val context: ScreenContextManager get() = contextManager

    //TODO: здесь нужно, чтобы приходил список ScreenModel с подставленными запросами
    fun setParsedResult(
        parsedScreens: List<ParsedScreen>,
        allStyles: AllStyles?,
    ) {
        this.parsedScreens = parsedScreens
        this.allStyles = allStyles

        screenMapper = ScreenMapper.create(allStyles, contextManager)
        screenProvider = ScreenProviderImpl(parsedScreens, screenMapper!!)
        actionHandler = ActionHandler(
            navigationManager,
            screenProvider!!,
            contextManager,
            externalDeeplinkHandler
        )

        loadInitialScreen()
    }

    private fun loadInitialScreen() {
        val firstScreen = parsedScreens?.firstOrNull()
        val mapper = screenMapper ?: return

        if (firstScreen != null) {
            val screenModel = mapper.mapToScreenModel(firstScreen)
            navigateToScreen(screenModel)
        } else {
            _uiState.value = GenerativeUiState.Error("No screens available")
        }
    }

    fun handleAction(action: UiAction) {
        viewModelScope.launch {
            val handler = actionHandler
            if (handler == null) {
                Log.w("GenerativeScreenViewModel", "ActionHandler not initialized")
                return@launch
            }

            when (val result = handler.handleAction(action)) {
                is ActionResult.Success -> {
                    updateUiStateFromNavigation()
                }
                is ActionResult.Error -> {
                    Log.e("GenerativeScreenViewModel", "Action error: ${result.message}", result.exception)
                    _uiState.value = GenerativeUiState.Error(result.message)
                }
            }
        }
    }

    fun navigateBack(): Boolean {
        val handler = actionHandler ?: return false

        viewModelScope.launch {
            when (val result = handler.handleAction(UiAction.Back)) {
                is ActionResult.Success -> {
                    updateUiStateFromNavigation()
                }
                is ActionResult.Error -> {
                    Log.w("GenerativeScreenViewModel", "Navigate back error: ${result.message}")
                }
            }
        }

        return navigationManager.canNavigateBack()
    }

    private fun updateUiStateFromNavigation() {
        val currentScreen = navigationManager.getCurrentScreen()
        if (currentScreen != null) {
            _uiState.value = GenerativeUiState.Screen(currentScreen.definition?.rootComponent)
        }
    }

    private fun navigateToScreen(screen: ScreenModel) {
        navigationManager.pushScreen(ScreenState.fromDefinition(screen))
        _uiState.value = GenerativeUiState.Screen(screen.rootComponent)
    }

    override fun onCleared() {
        super.onCleared()
        contextManager.clear()
        navigationManager.clear()
    }

    private class ScreenProviderImpl(
        private val parsedScreens: List<ParsedScreen>,
        private val mapper: ScreenMapper
    ) : ScreenProvider {

        override suspend fun findScreen(screenCode: String): ScreenModel? {
            return parsedScreens.find { it.screenCode == screenCode }
                ?.let { mapper.mapToScreenModel(it) }
        }

        override suspend fun findScreenByDeeplink(deeplink: String): ScreenModel? {
            return parsedScreens.find { it.deeplink == deeplink }
                ?.let { mapper.mapToScreenModel(it) }
        }
    }
}