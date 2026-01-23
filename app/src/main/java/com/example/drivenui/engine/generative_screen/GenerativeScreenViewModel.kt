package com.example.drivenui.engine.generative_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenui.data.RequestInteractor
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.context.resolveScreen
import com.example.drivenui.engine.generative_screen.action.ActionHandler
import com.example.drivenui.engine.generative_screen.action.ActionResult
import com.example.drivenui.engine.generative_screen.action.ExternalDeeplinkHandler
import com.example.drivenui.engine.generative_screen.action.ScreenProvider
import com.example.drivenui.engine.generative_screen.mapper.ScreenMapper
import com.example.drivenui.engine.generative_screen.models.GenerativeUiState
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.generative_screen.models.ScreenState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.generative_screen.navigation.ScreenNavigationManager
import com.example.drivenui.engine.generative_screen.widget.IWidgetValueProvider
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.parser.models.AllStyles
import com.example.drivenui.parser.models.ParsedScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenerativeScreenViewModel @Inject constructor(
    private val externalDeeplinkHandler: ExternalDeeplinkHandler,
    private val requestInteractor: RequestInteractor,
    private val contextManager: IContextManager,
    private val widgetValueProvider: IWidgetValueProvider
) : ViewModel() {

    private var parsedScreens: List<ParsedScreen>? = null
    private var allStyles: AllStyles? = null
    private var microappCode: String? = null

    private val navigationManager = ScreenNavigationManager()
    private var screenMapper: ScreenMapper? = null
    private var actionHandler: ActionHandler? = null
    private var screenProvider: ScreenProviderImpl? = null

    private val _uiState = MutableStateFlow<GenerativeUiState>(GenerativeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    //TODO: здесь нужно, чтобы приходил список ScreenModel с подставленными запросами
    fun setParsedResult(
        parsedScreens: List<ParsedScreen>,
        allStyles: AllStyles?,
        microappCode: String? = null
    ) {
        this.parsedScreens = parsedScreens
        this.allStyles = allStyles
        this.microappCode = microappCode

        screenMapper = ScreenMapper.create(allStyles)
        screenProvider = ScreenProviderImpl(parsedScreens, screenMapper!!)
        actionHandler = ActionHandler(
            navigationManager,
            screenProvider!!,
            externalDeeplinkHandler,
            contextManager,
            widgetValueProvider,
            requestInteractor,
            microappCode
        )

        loadInitialScreen()
    }

    private fun loadInitialScreen() {
        val firstScreen = parsedScreens?.get(1)
        val mapper = screenMapper ?: return

        if (firstScreen != null) {
            val screenModel = mapper.mapToScreenModel(firstScreen)

            navigateToScreen(screenModel)
        } else {
            _uiState.value = GenerativeUiState.Error("No screens available")
        }
    }

    fun handleActions(actions: List<UiAction>) {
        viewModelScope.launch {
            val handler = actionHandler
            if (handler == null) {
                Log.w("GenerativeScreenViewModel", "ActionHandler not initialized")
                return@launch
            }

            for (action in actions) {
                when (val result = handler.handleAction(action)) {
                    is ActionResult.NavigationChanged -> {
                        if (result.isBack) {
                            updateUiStateFromNavigation()
                        } else {
                            handleNavigationChanged()
                        }
                    }
                    is ActionResult.Success -> {
                        // TODO: поправить в будущем. После выполнения запроса обновление происходит по отдельному экшену, а не сразу
                        if (action is UiAction.ExecuteQuery ||
                            action is UiAction.RefreshWidget ||
                            action is UiAction.RefreshLayout ||
                            action is UiAction.RefreshScreen
                        ) {
                            updateUiStateFromNavigation()
                        }
                    }
                    // TODO: выяснить нужно ли прерывать оставшиеся действия при ошибке одного из
                    is ActionResult.Error -> {
                        Log.e(
                            "GenerativeScreenViewModel",
                            "Action error: ${result.message}",
                            result.exception
                        )
                        _uiState.value = GenerativeUiState.Error(result.message)
                        break
                    }
                }
            }
        }
    }

    fun navigateBack(): Boolean {
        val handler = actionHandler ?: return false

        viewModelScope.launch {
            when (val result = handler.handleAction(UiAction.Back)) {
                is ActionResult.NavigationChanged,
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

    /**
     * Вызывается UI-слоем, когда нужно сохранить значение виджета.
     */
    fun onWidgetValueChange(widgetCode: String, parameter: String, value: Any) {
        widgetValueProvider.setWidgetValue(widgetCode, parameter, value)
    }

    private suspend fun handleNavigationChanged() {
        val currentScreen = navigationManager.getCurrentScreen()
        val definition = currentScreen?.definition

        if (definition == null) {
            updateUiStateFromNavigation()
            return
        }

        processScreenForNavigation(
            baseScreen = definition,
            replaceCurrent = true
        )
    }

    private fun updateUiStateFromNavigation() {
        val currentScreen = navigationManager.getCurrentScreen()
        if (currentScreen != null) {
            val resolvedRoot = currentScreen.definition?.rootComponent
            _uiState.value = GenerativeUiState.Screen(resolvedRoot)
        }
    }

    private fun navigateToScreen(screen: ScreenModel) {
        viewModelScope.launch {
            processScreenForNavigation(
                baseScreen = screen,
                replaceCurrent = false
            )
        }
    }

    private suspend fun processScreenForNavigation(
        baseScreen: ScreenModel,
        replaceCurrent: Boolean
    ) {
        val onCreateQueries = extractOnCreateQueries(baseScreen.rootComponent)

        val finalScreen = if (onCreateQueries.isNotEmpty()) {
            _uiState.value = GenerativeUiState.Loading

            var processedScreen = baseScreen
            for (queryCode in onCreateQueries) {
                processedScreen = requestInteractor.executeQueryAndUpdateScreen(
                    screenModel = processedScreen,
                    queryCode = queryCode
                )
            }
            processedScreen
        } else {
            baseScreen
        }

        val resolvedScreen = resolveScreen(finalScreen, contextManager)
        if (replaceCurrent) {
            navigationManager.updateCurrentScreen(ScreenState.fromDefinition(resolvedScreen))
        } else {
            navigationManager.pushScreen(ScreenState.fromDefinition(resolvedScreen))
        }
        _uiState.value = GenerativeUiState.Screen(resolvedScreen.rootComponent)
    }

    private fun extractOnCreateQueries(rootComponent: ComponentModel?): List<String> {
        if (rootComponent !is com.example.drivenui.engine.uirender.models.LayoutModel) {
            return emptyList()
        }

        return rootComponent.onCreateActions
            .filterIsInstance<UiAction.ExecuteQuery>()
            .map { it.queryCode }
    }

    override fun onCleared() {
        super.onCleared()
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