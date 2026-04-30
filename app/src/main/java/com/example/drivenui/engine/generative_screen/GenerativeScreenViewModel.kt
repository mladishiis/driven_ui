package com.example.drivenui.engine.generative_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenui.app.data.RequestInteractor
import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.cache.toScreenModel
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.action.ActionHandler
import com.example.drivenui.engine.generative_screen.action.ActionResult
import com.example.drivenui.engine.generative_screen.action.ExternalDeeplinkHandler
import com.example.drivenui.engine.generative_screen.action.ScreenProvider
import com.example.drivenui.engine.generative_screen.binding.resolveTemplateString
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
import com.example.drivenui.engine.parser.models.AllStyles
import com.example.drivenui.engine.parser.models.ParsedScreen
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для отображения экранов микроаппа на базе SDUI.
 *
 * Управляет навигацией, состоянием экрана, нижней шторкой и обработкой действий.
 * Поддерживает загрузку как из распарсенных данных ([setParsedResult]),
 * так и из закэшированных ([setMappedResult]).
 *
 * @property externalDeeplinkHandler обработка внешних deeplink
 * @property requestInteractor выполнение запросов и биндинги
 * @property contextManager хранилище переменных микроаппа
 * @property widgetValueProvider значения виджетов
 */
@HiltViewModel
class GenerativeScreenViewModel @Inject constructor(
    private val externalDeeplinkHandler: ExternalDeeplinkHandler,
    private val requestInteractor: RequestInteractor,
    private val contextManager: IContextManager,
    private val widgetValueProvider: IWidgetValueProvider,
) : ViewModel() {

    private var parsedScreens: List<ParsedScreen>? = null
    private var mappedScreens: List<ScreenModel>? = null
    private var allStyles: AllStyles? = null
    private var microappCode: String? = null
    private var microappDeeplink: String = ""

    private val navigationManager = ScreenNavigationManager()
    private var screenMapper: ScreenMapper? = null
    private var styleRegistry: ComposeStyleRegistry? = null
    private var actionHandler: ActionHandler? = null
    private var screenProvider: ScreenProvider? = null

    private val _uiState = MutableStateFlow<GenerativeUiState>(GenerativeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _styleRegistryState = MutableStateFlow<ComposeStyleRegistry?>(null)
    val styleRegistryState = _styleRegistryState.asStateFlow()

    /**
     * Состояние нижней шторки (root component или null, если закрыта).
     */
    private val _bottomSheetState = MutableStateFlow<ComponentModel?>(null)
    val bottomSheetState = _bottomSheetState.asStateFlow()

    /**
     * Событие «выйти из микроаппа» (например [UiAction.Back] на корневом экране стека).
     */
    private val _exitMicroappEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val exitMicroappEvents = _exitMicroappEvents.asSharedFlow()

    /**
     * Код текущего экрана (обновляется при каждом переходе).
     */
    private val _currentScreenId = MutableStateFlow<String?>(null)
    val currentScreenId = _currentScreenId.asStateFlow()

    /**
     * Устанавливает результат парсинга и загружает начальный экран.
     *
     * @param parsedScreens список распарсенных экранов
     * @param allStyles стили (текст, цвета и т.д.)
     * @param microappCode код микроаппа для контекста и действий
     * @param microappDeeplink deeplink микроаппа для выбора стартового экрана
     */
    fun setParsedResult(
        parsedScreens: List<ParsedScreen>,
        allStyles: AllStyles?,
        microappCode: String? = null,
        microappDeeplink: String = "",
    ) {
        this.parsedScreens = parsedScreens
        this.mappedScreens = null
        this.allStyles = allStyles
        this.microappCode = microappCode
        this.microappDeeplink = microappDeeplink

        val localStyleRegistry = ComposeStyleRegistry(allStyles)
        styleRegistry = localStyleRegistry
        _styleRegistryState.value = localStyleRegistry

        val mapper = ScreenMapper(localStyleRegistry)
        screenMapper = mapper
        val provider = ScreenProviderImpl(parsedScreens, mapper)
        screenProvider = provider
        actionHandler = ActionHandler(
            navigationManager,
            provider,
            externalDeeplinkHandler,
            contextManager,
            widgetValueProvider,
            requestInteractor,
            microappCode,
            localStyleRegistry,
        )

        loadInitialScreen()
    }

    /**
     * Устанавливает закэшированный результат для быстрой загрузки без парсинга.
     *
     * @param mappedData заранее замапленные экраны и стили
     */
    fun setMappedResult(mappedData: CachedMicroappData) {
        val screens = mappedData.screens.map { it.toScreenModel() }
        this.mappedScreens = screens
        this.parsedScreens = null
        this.allStyles = mappedData.allStyles
        this.microappCode = mappedData.microappCode.takeIf { it.isNotBlank() }
        this.microappDeeplink = mappedData.microappDeeplink

        val localStyleRegistry = ComposeStyleRegistry(mappedData.allStyles)
        styleRegistry = localStyleRegistry
        _styleRegistryState.value = localStyleRegistry

        screenMapper = null
        val provider = MappedScreenProviderImpl(screens)
        screenProvider = provider
        actionHandler = ActionHandler(
            navigationManager,
            provider,
            externalDeeplinkHandler,
            contextManager,
            widgetValueProvider,
            requestInteractor,
            microappCode,
            localStyleRegistry,
        )

        loadInitialScreenFromMapped(screens)
    }

    private fun loadInitialScreen() {
        val screens = mappedScreens
        if (screens != null) {
            loadInitialScreenFromMapped(screens)
            return
        }
        val parsed = parsedScreens ?: return
        val mapper = screenMapper ?: return

        val targetScreen = findScreenByMicroappDeeplink(parsed) { it.deeplink }
            ?: parsed.firstOrNull()

        if (targetScreen != null) {
            val screenModel = mapper.mapToScreenModel(targetScreen)
            navigateToScreen(screenModel)
        } else {
            _uiState.value = GenerativeUiState.Error("No screens available")
        }
    }

    private fun loadInitialScreenFromMapped(screens: List<ScreenModel>) {
        val targetScreen = findScreenByMicroappDeeplink(screens) { it.deeplink }
            ?: screens.firstOrNull()

        if (targetScreen != null) {
            navigateToScreen(targetScreen)
        } else {
            _uiState.value = GenerativeUiState.Error("No screens available")
        }
    }

    /**
     * Ищет экран, у которого deeplink совпадает с deeplink микроаппа.
     * Возвращает null, если deeplink микроаппа пустой или совпадений нет.
     */
    private fun <T> findScreenByMicroappDeeplink(
        screens: List<T>,
        deeplinkSelector: (T) -> String,
    ): T? {
        if (microappDeeplink.isBlank()) return null
        return screens.find { deeplinkSelector(it) == microappDeeplink }
    }

    /**
     * Обрабатывает список действий (навигация, запросы, открытие шторки и т.д.).
     *
     * @param actions список действий для выполнения
     */
    fun handleActions(actions: List<UiAction>) {
        viewModelScope.launch {
            runActionsSequentially(actions)
        }
    }

    private suspend fun runActionsSequentially(actions: List<UiAction>) {
        val handler = actionHandler
        if (handler == null) {
            Log.w("GenerativeScreenViewModel", "Обработчик действий не инициализирован")
            return
        }

        for (action in actions) {
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
                    if (action is UiAction.ExecuteQuery ||
                        action is UiAction.RefreshWidget ||
                        action is UiAction.RefreshLayout ||
                        action is UiAction.RefreshScreen
                    ) {
                        updateUiStateFromNavigation()
                    }
                }
                is ActionResult.BottomSheetChanged -> {
                    if (result.model == null) {
                        _bottomSheetState.value = null
                    } else {
                        viewModelScope.launch {
                            openBottomSheetAfterScreenOnCreate(result.model)
                        }
                    }
                }
                is ActionResult.ExitMicroapp -> {
                    runDestroyActionsForCurrentScreen()
                    _exitMicroappEvents.tryEmit(Unit)
                }
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

    /**
     * Выполняет переход назад (сначала закрывает шторку, затем стек навигации).
     *
     * @return false если обработчик действий не инициализирован, иначе true
     */
    fun navigateBack(): Boolean {
        val handler = actionHandler ?: return false

        if (_bottomSheetState.value != null) {
            _bottomSheetState.value = null
            return true
        }

        if (!navigationManager.canNavigateBack()) {
            viewModelScope.launch {
                runDestroyActionsForCurrentScreen()
                _exitMicroappEvents.tryEmit(Unit)
            }
            return true
        }

        viewModelScope.launch {
            runDestroyActionsForCurrentScreen()
            when (val result = handler.handleAction(UiAction.Back)) {
                is ActionResult.NavigationChanged -> updateUiStateFromNavigation()
                is ActionResult.Success -> Unit
                is ActionResult.ExitMicroapp -> Unit
                is ActionResult.BottomSheetChanged -> Unit
                is ActionResult.Error -> {
                    Log.w("GenerativeScreenViewModel", "Ошибка при переходе назад: ${result.message}")
                }
            }
        }

        return true
    }

    /**
     * Сохраняет значение параметра виджета (вызывается UI-слоем).
     *
     * @param widgetCode код виджета
     * @param parameter имя параметра
     * @param value сохраняемое значение
     */
    fun onWidgetValueChange(widgetCode: String, parameter: String, value: Any) {
        widgetValueProvider.setWidgetValue(widgetCode, parameter, value)
    }

    /**
     * Возвращает радиус скругления (в dp) для корневого layout шторки.
     *
     * @param sheetRoot корневой компонент шторки
     * @return радиус в dp или null
     */
    fun getSheetCornerRadiusDp(sheetRoot: ComponentModel): Int? {
        if (sheetRoot !is LayoutModel) return null
        sheetRoot.cornerRadius.all?.let { return it }
        val radiusStr = sheetRoot.radiusValues.radius ?: return null
        val resolved = resolveTemplateString(
            radiusStr,
            requestInteractor.getDataContext(),
            contextManager,
        ) ?: radiusStr
        return resolved.toIntOrNull()
    }

    /**
     * Применяет биндинги и резолвит стили к компоненту.
     *
     * @param componentModel компонент для обработки
     * @return компонент с применёнными биндингами и стилями
     */
    fun applyBindingsToComponent(componentModel: ComponentModel): ComponentModel {
        val forLayoutBinding = requestInteractor.getForLayoutBinding()
        val dataContext = requestInteractor.getDataContext()
        val withBindings = forLayoutBinding.applyBindingsToComponent(componentModel, dataContext)
        val localStyleRegistry = styleRegistry
        return if (localStyleRegistry != null) {
            resolveComponent(
                withBindings,
                contextManager,
                localStyleRegistry,
                dataContext,
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
            replaceCurrent = true,
        )
    }

    private fun updateUiStateFromNavigation() {
        val currentScreen = navigationManager.getCurrentScreen()
        if (currentScreen != null) {
            _currentScreenId.value = currentScreen.definition?.id
            val resolvedRoot = currentScreen.definition?.rootComponent
            _uiState.value = GenerativeUiState.Screen(resolvedRoot)
        }
    }

    private fun navigateToScreen(screen: ScreenModel) {
        viewModelScope.launch {
            processScreenForNavigation(
                baseScreen = screen,
                replaceCurrent = false,
            )
        }
    }

    private suspend fun processScreenForNavigation(
        baseScreen: ScreenModel,
        replaceCurrent: Boolean,
    ) {
        val localStyleRegistry = styleRegistry ?: return
        val preComposeActions = baseScreen.onCreateActions
        if (preComposeActions.isNotEmpty()) {
            _uiState.value = GenerativeUiState.Loading
        }
        val leadingQueryCount = countLeadingExecuteQueries(preComposeActions)
        var workingScreen = baseScreen
        for (action in preComposeActions.take(leadingQueryCount)) {
            workingScreen = requestInteractor.executeQueryAndUpdateScreen(
                screenModel = workingScreen,
                action = action as UiAction.ExecuteQuery,
            )
        }
        val resolvedScreen = resolveScreen(
            workingScreen,
            contextManager,
            localStyleRegistry,
            requestInteractor.getDataContext(),
        )
        if (replaceCurrent) {
            navigationManager.updateCurrentScreen(ScreenState.fromDefinition(resolvedScreen))
        } else {
            navigationManager.pushScreen(ScreenState.fromDefinition(resolvedScreen))
        }
        runActionsSequentially(preComposeActions.drop(leadingQueryCount))
        updateUiStateFromNavigation()
    }

    private fun countLeadingExecuteQueries(orderedOnCreate: List<UiAction>): Int =
        orderedOnCreate.takeWhile { it is UiAction.ExecuteQuery }.size

    
    private suspend fun openBottomSheetAfterScreenOnCreate(screen: ScreenModel) {
        val localStyleRegistry = styleRegistry ?: return
        val preComposeActions = screen.onCreateActions
        val leadingQueryCount = countLeadingExecuteQueries(preComposeActions)
        var workingScreen = screen
        for (action in preComposeActions.take(leadingQueryCount)) {
            workingScreen = requestInteractor.executeQueryAndUpdateScreen(
                screenModel = workingScreen,
                action = action as UiAction.ExecuteQuery,
            )
        }
        val resolvedScreen = resolveScreen(
            workingScreen,
            contextManager,
            localStyleRegistry,
            requestInteractor.getDataContext(),
        )
        runActionsSequentially(preComposeActions.drop(leadingQueryCount))
        _bottomSheetState.value = resolvedScreen.rootComponent
    }

    private suspend fun runDestroyActionsForCurrentScreen() {
        val definition = navigationManager.getCurrentScreen()?.definition ?: return
        runActionsSequentially(definition.onDestroyActions)
    }

    override fun onCleared() {
        super.onCleared()
        navigationManager.clear()
    }

    private class ScreenProviderImpl(
        private val parsedScreens: List<ParsedScreen>,
        private val mapper: ScreenMapper,
    ) : ScreenProvider {

        override suspend fun findScreen(screenCode: String): ScreenModel? {
            return parsedScreens.find { it.screenCode.equals(screenCode, ignoreCase = true) }
                ?.let { mapper.mapToScreenModel(it) }
        }

        override suspend fun findScreenByDeeplink(deeplink: String): ScreenModel? {
            return parsedScreens.find { it.deeplink == deeplink }
                ?.let { mapper.mapToScreenModel(it) }
        }
    }

    private class MappedScreenProviderImpl(
        private val screens: List<ScreenModel>,
    ) : ScreenProvider {

        override suspend fun findScreen(screenCode: String): ScreenModel? {
            return screens.find { it.id.equals(screenCode, ignoreCase = true) }
        }

        override suspend fun findScreenByDeeplink(deeplink: String): ScreenModel? {
            return screens.find { it.deeplink == deeplink }
        }
    }
}