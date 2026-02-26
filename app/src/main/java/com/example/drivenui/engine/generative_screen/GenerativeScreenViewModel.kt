package com.example.drivenui.engine.generative_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenui.app.data.RequestInteractor
import com.example.drivenui.engine.context.IContextManager
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
import com.example.drivenui.engine.generative_screen.styles.resolveComponent
import com.example.drivenui.engine.generative_screen.styles.resolveScreen
import com.example.drivenui.engine.generative_screen.widget.IWidgetValueProvider
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.engine.value.resolveValueExpression
import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.cache.toScreenModel
import com.example.drivenui.engine.parser.models.AllStyles
import com.example.drivenui.engine.parser.models.ParsedScreen
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
    private var mappedScreens: List<ScreenModel>? = null
    private var allStyles: AllStyles? = null
    private var microappCode: String? = null

    private val navigationManager = ScreenNavigationManager()
    private var screenMapper: ScreenMapper? = null
    private var styleRegistry: ComposeStyleRegistry? = null
    private var actionHandler: ActionHandler? = null
    private var screenProvider: ScreenProvider? = null

    private val _uiState = MutableStateFlow<GenerativeUiState>(GenerativeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    /**
     * Состояние нижней шторки. Отдельный стейт, чтобы изменение шторки
     * не трогало состояние основного экрана и не вызывало его лишние перерисовки.
     */
    private val _bottomSheetState = MutableStateFlow<ComponentModel?>(null)
    val bottomSheetState = _bottomSheetState.asStateFlow()

    fun setParsedResult(
        parsedScreens: List<ParsedScreen>,
        allStyles: AllStyles?,
        microappCode: String? = null
    ) {
        this.parsedScreens = parsedScreens
        this.mappedScreens = null
        this.allStyles = allStyles
        this.microappCode = microappCode

        val localStyleRegistry = ComposeStyleRegistry(allStyles)
        styleRegistry = localStyleRegistry

        screenMapper = ScreenMapper(localStyleRegistry)
        screenProvider = ScreenProviderImpl(parsedScreens, screenMapper!!)
        actionHandler = ActionHandler(
            navigationManager,
            screenProvider!!,
            externalDeeplinkHandler,
            contextManager,
            widgetValueProvider,
            requestInteractor,
            microappCode,
            localStyleRegistry
        )

        loadInitialScreen()
    }

    /**
     * Устанавливает замапленный результат (быстрая загрузка без парсинга и маппинга).
     */
    fun setMappedResult(mappedData: CachedMicroappData) {
        val screens = mappedData.screens.map { it.toScreenModel() }
        this.mappedScreens = screens
        this.parsedScreens = null
        this.allStyles = mappedData.allStyles
        this.microappCode = mappedData.microappCode.takeIf { it.isNotBlank() }

        val localStyleRegistry = ComposeStyleRegistry(mappedData.allStyles)
        styleRegistry = localStyleRegistry

        screenMapper = null
        screenProvider = MappedScreenProviderImpl(screens)
        actionHandler = ActionHandler(
            navigationManager,
            screenProvider!!,
            externalDeeplinkHandler,
            contextManager,
            widgetValueProvider,
            requestInteractor,
            microappCode,
            localStyleRegistry
        )

        loadInitialScreenFromMapped(screens)
    }

    private fun loadInitialScreen() {
        val screens = mappedScreens
        if (screens != null) {
            loadInitialScreenFromMapped(screens)
            return
        }
        val firstScreen = parsedScreens?.get(0)
        val mapper = screenMapper ?: return

        if (firstScreen != null) {
            val screenModel = mapper.mapToScreenModel(firstScreen)
            navigateToScreen(screenModel)
        } else {
            _uiState.value = GenerativeUiState.Error("No screens available")
        }
    }

    private fun loadInitialScreenFromMapped(screens: List<ScreenModel>) {
        val firstScreen = screens.firstOrNull()
        if (firstScreen != null) {
            navigateToScreen(firstScreen)
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
                // Если сейчас открыта нижняя шторка и приходит UiAction.Back,
                // то воспринимаем это как закрытие шторки, не трогая стек навигации.
                if (action is UiAction.Back && _bottomSheetState.value != null) {
                    _bottomSheetState.value = null
                    continue
                }

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
                    is ActionResult.BottomSheetChanged -> {
                        // Обновляем только состояние шторки, не трогая экран.
                        _bottomSheetState.value = result.model?.rootComponent
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

        // Если открыта нижняя шторка — сначала закрываем её, не трогая стек навигации.
        if (_bottomSheetState.value != null) {
            _bottomSheetState.value = null
            return true
        }

        viewModelScope.launch {
            when (val result = handler.handleAction(UiAction.Back)) {
                is ActionResult.NavigationChanged,
                is ActionResult.Success -> {
                    updateUiStateFromNavigation()
                }

                is ActionResult.BottomSheetChanged -> {
                    // Игнорируем: для команды Back состояние шторки уже обработано выше.
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

    /**
     * Возвращает радиус скругления (в dp) для корневого layout шторки.
     * Берётся из roundStyle корневого компонента шторки; если корень не layout или стиль не задан — null.
     */
    fun getSheetCornerRadiusDp(sheetRoot: ComponentModel): Int? {
        if (sheetRoot !is LayoutModel) return null
        val code = sheetRoot.roundStyleCode ?: return null
        val registry = styleRegistry ?: return null
        val resolvedCode = resolveValueExpression(code, contextManager)
        return registry.getRoundStyle(resolvedCode)?.radiusValue
    }

    /**
     * Применить биндинги к одному компоненту
     */
    fun applyBindingsToComponent(componentModel: ComponentModel): ComponentModel {
        val binder = requestInteractor.getDataBinder()
        val dataContext = requestInteractor.getDataContext()
        // Сначала подставляем данные (${...}) для конкретного экземпляра компонента
        val withBindings = binder.applyBindingsToComponentPublic(componentModel, dataContext) ?: componentModel

        // Затем резолвим условные выражения *if(...)* и коды стилей уже ПОСЛЕ биндингов.
        val localStyleRegistry = styleRegistry
        return if (localStyleRegistry != null) {
            resolveComponent(
                withBindings,
                contextManager,
                localStyleRegistry
            ) ?: withBindings
        } else {
            withBindings
        }
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

        val localStyleRegistry = styleRegistry ?: return
        val resolvedScreen = resolveScreen(finalScreen, contextManager, localStyleRegistry)
        if (replaceCurrent) {
            navigationManager.updateCurrentScreen(ScreenState.fromDefinition(resolvedScreen))
        } else {
            navigationManager.pushScreen(ScreenState.fromDefinition(resolvedScreen))
        }
        // При смене экрана шторку закрываем (если была открыта)
        _uiState.value = GenerativeUiState.Screen(resolvedScreen.rootComponent)
    }

    private fun extractOnCreateQueries(rootComponent: ComponentModel?): List<String> {
        if (rootComponent !is LayoutModel) {
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

    private class MappedScreenProviderImpl(
        private val screens: List<ScreenModel>
    ) : ScreenProvider {

        override suspend fun findScreen(screenCode: String): ScreenModel? {
            return screens.find { it.id == screenCode }
        }

        override suspend fun findScreenByDeeplink(deeplink: String): ScreenModel? {
            return null
        }
    }
}