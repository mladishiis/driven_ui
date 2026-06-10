package com.example.drivenui.engine.generative_screen.action

import android.content.Context
import android.util.Log
import com.example.drivenui.app.data.RequestInteractor
import com.example.drivenui.app.theme.isSystemInDarkTheme
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.binding.resolveTemplateString
import com.example.drivenui.engine.generative_screen.models.ScreenDefinition
import com.example.drivenui.engine.generative_screen.models.ScreenPresentation
import com.example.drivenui.engine.generative_screen.models.ScreenState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.generative_screen.navigation.ScreenNavigationManager
import com.example.drivenui.engine.generative_screen.presentation.PresentationBuilder
import com.example.drivenui.engine.generative_screen.refresh.RefreshResolveContext
import com.example.drivenui.engine.generative_screen.refresh.TargetedRefreshResult
import com.example.drivenui.engine.generative_screen.refresh.refreshLayoutInScreen
import com.example.drivenui.engine.generative_screen.refresh.refreshWidgetInScreen
import com.example.drivenui.engine.generative_screen.widget.IWidgetValueProvider
import com.example.drivenui.engine.mappers.ComposeStyleRegistry

/**
 * Обработчик UI-действий: навигация, deeplink, запросы, шторки, контекст.
 *
 * @property navigationManager менеджер стека навигации
 * @property screenProvider поиск экранов по коду и deeplink
 * @property externalDeeplinkHandler обработка внешних deeplink
 * @property contextManager хранилище переменных
 * @property widgetValueProvider значения виджетов
 * @property requestInteractor выполнение запросов
 * @property applicationContext контекст приложения (системная тёмная тема для резолва палитры)
 * @property microappCode код микроаппа для контекста
 * @property styleRegistry реестр стилей
 */
class ActionHandler(
    private val navigationManager: ScreenNavigationManager,
    private val screenProvider: ScreenProvider,
    private val externalDeeplinkHandler: ExternalDeeplinkHandler,
    private val contextManager: IContextManager,
    private val widgetValueProvider: IWidgetValueProvider,
    private val requestInteractor: RequestInteractor,
    private val applicationContext: Context,
    private val microappCode: String?,
    private val styleRegistry: ComposeStyleRegistry,
) {

    /**
     * Обрабатывает UI-действие.
     *
     * @param action действие для обработки
     * @return результат выполнения действия
     */
    suspend fun handleAction(action: UiAction): ActionResult {
        return try {
            when (action) {
                is UiAction.OpenScreen -> handleOpenScreen(action.screenCode)
                is UiAction.OpenBottomSheet -> handleOpenBottomSheet(action.screenCode)
                is UiAction.Back -> handleBack()
                is UiAction.OpenDeeplink -> handleOpenDeeplink(action.deeplink)
                is UiAction.RefreshScreen -> handleRefreshScreen(action.screenCode)
                is UiAction.ExecuteQuery -> handleExecuteQuery(action)
                is UiAction.DataTransform -> handleDataTransform(action)
                is UiAction.SaveToContext -> handleSaveToContext(action)
                is UiAction.NativeCode -> handleNativeCode(action)
                is UiAction.RefreshWidget -> handleRefreshWidget(action.widgetCode)
                is UiAction.RefreshLayout -> handleRefreshLayout(action.layoutCode)
                is UiAction.Empty -> ActionResult.Success
            }
        } catch (e: Exception) {
            Log.e("ActionHandler", "Ошибка обработки действия: ${action::class.simpleName}", e)
            ActionResult.Error("Ошибка обработки действия: ${e.message}", e)
        }
    }

    private suspend fun handleOpenScreen(screenCode: String): ActionResult {
        val definition = screenProvider.findScreen(screenCode)
            ?: return ActionResult.Error("Экран не найден: $screenCode")

        return openScreenDefinition(definition)
    }

    private suspend fun handleOpenBottomSheet(screenCode: String): ActionResult {
        val definition = screenProvider.findScreen(screenCode)
            ?: return ActionResult.Error("Экран для нижней шторки не найден: $screenCode")

        return ActionResult.BottomSheetChanged(definition)
    }

    private fun handleBack(): ActionResult {
        val previousScreen = navigationManager.popScreen()
        return if (previousScreen != null) {
            ActionResult.NavigationChanged(isBack = true)
        } else {
            ActionResult.ExitMicroapp
        }
    }

    private suspend fun handleOpenDeeplink(deeplink: String): ActionResult {
        val definition = screenProvider.findScreenByDeeplink(deeplink)
        if (definition != null) {
            return openScreenDefinition(definition)
        }

        val handled = externalDeeplinkHandler.handleExternalDeeplink(deeplink)
        return if (handled) {
            ActionResult.Success
        } else {
            ActionResult.Error("Deeplink не найден: $deeplink")
        }
    }

    /**
     * Кладёт definition в стек; presentation собирает ViewModel в [processScreenForNavigation].
     */
    private fun openScreenDefinition(definition: ScreenDefinition): ActionResult {
        navigationManager.pushScreen(ScreenState.create(definition = definition))
        return ActionResult.NavigationChanged(isBack = false)
    }

    /**
     * Обновляет весь текущий экран: FOR-биндинги и полная пересборка presentation.
     */
    private suspend fun handleRefreshScreen(@Suppress("UNUSED_PARAMETER") screenCode: String): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
            ?: return ActionResult.Error("Нет текущего экрана для обновления")

        val definitionWithBindings = requestInteractor.applyBindingsToScreen(currentScreen.definition)
        val presentation = buildPresentation(definitionWithBindings)
        navigationManager.updateCurrentScreen(
            ScreenState.create(
                definition = definitionWithBindings,
                presentation = presentation,
            ),
        )

        return ActionResult.Success
    }

    private suspend fun handleRefreshWidget(widgetCode: String): ActionResult =
        applyTargetedRefresh(
            noScreenError = "Нет текущего экрана для обновления виджета",
            notFoundError = "Виджет не найден: $widgetCode",
        ) { definition, presentation, resolveContext ->
            refreshWidgetInScreen(
                definition = definition,
                presentation = presentation,
                widgetCode = widgetCode,
                resolveContext = resolveContext,
            )
        }

    private suspend fun handleRefreshLayout(layoutCode: String): ActionResult =
        applyTargetedRefresh(
            noScreenError = "Нет текущего экрана для обновления лейаута",
            notFoundError = "Layout не найден: $layoutCode",
        ) { definition, presentation, resolveContext ->
            refreshLayoutInScreen(
                definition = definition,
                presentation = presentation,
                layoutCode = layoutCode,
                resolveContext = resolveContext,
            )
        }

    private suspend fun applyTargetedRefresh(
        noScreenError: String,
        notFoundError: String,
        refresh: (ScreenDefinition, ScreenPresentation, RefreshResolveContext) -> TargetedRefreshResult?,
    ): ActionResult {
        val screenPair = resolveCurrentScreenPair() ?: return ActionResult.Error(noScreenError)
        val refreshResult = refresh(
            screenPair.definition,
            screenPair.presentation,
            buildRefreshResolveContext(),
        ) ?: return ActionResult.Error(notFoundError)

        commitTargetedRefresh(refreshResult)
        return ActionResult.Success
    }

    private fun resolveCurrentScreenPair(): ScreenPair? {
        val currentScreen = navigationManager.getCurrentScreen() ?: return null
        val presentation = currentScreen.presentation ?: return null
        return ScreenPair(
            definition = currentScreen.definition,
            presentation = presentation,
        )
    }

    private fun buildRefreshResolveContext(): RefreshResolveContext =
        RefreshResolveContext(
            contextManager = contextManager,
            styleRegistry = styleRegistry,
            dataContext = requestInteractor.getDataContext(),
            useDarkColorPalette = applicationContext.isSystemInDarkTheme(),
        )

    private fun commitTargetedRefresh(refreshResult: TargetedRefreshResult) {
        navigationManager.updateCurrentScreen(
            ScreenState.create(
                definition = refreshResult.definition,
                presentation = refreshResult.presentation,
            ),
        )
    }

    private data class ScreenPair(
        val definition: ScreenDefinition,
        val presentation: ScreenPresentation,
    )

    private suspend fun handleExecuteQuery(action: UiAction.ExecuteQuery): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
            ?: return ActionResult.Error("Нет текущего экрана для выполнения запроса")

        val definitionWithQuery = requestInteractor.executeQueryAndUpdateScreen(
            definition = currentScreen.definition,
            action = action,
            resolveQueryValue = ::resolveQueryValue,
        )
        val presentation = buildPresentation(definitionWithQuery)
        navigationManager.updateCurrentScreen(
            ScreenState.create(
                definition = definitionWithQuery,
                presentation = presentation,
            ),
        )

        return ActionResult.Success
    }

    private fun buildPresentation(definition: ScreenDefinition) =
        PresentationBuilder.build(
            definition = definition,
            contextManager = contextManager,
            styleRegistry = styleRegistry,
            dataContext = requestInteractor.getDataContext(),
            useDarkColorPalette = applicationContext.isSystemInDarkTheme(),
        )

    private fun resolveQueryValue(value: String): String {
        return resolveTemplateString(
            value,
            requestInteractor.getDataContext(),
            contextManager,
        ) ?: value
    }

    private fun handleSaveToContext(action: UiAction.SaveToContext): ActionResult {
        val sourceValue = resolveValueFrom(action.valueFrom)
        if (sourceValue == null) {
            return ActionResult.Error("Не удалось получить значение из: ${action.valueFrom}")
        }

        parseMicroappContextTarget(action.valueTo)?.let { (microappCode, variableName) ->
            contextManager.setMicroappVariable(
                microappCode = microappCode,
                variableName = variableName,
                value = sourceValue,
            )
            return ActionResult.Success
        }

        parseEngineContextTarget(action.valueTo)?.let { variableName ->
            contextManager.setEngineVariable(
                variableName = variableName,
                value = sourceValue,
            )
            return ActionResult.Success
        }

        return ActionResult.Error(
            "Неверный формат цели для saveToContext: ${action.valueTo}. Ожидается @{кодМикроаппа.имяПеременной} или @@{имяПеременной}",
        )
    }

    private fun resolveValueFrom(expression: String): Any? {
        parseWidgetVariable(expression)?.let { (widgetCode, parameter) ->
            return widgetValueProvider.getWidgetValue(
                widgetCode = widgetCode,
                parameter = parameter,
            ) ?: ""
        }

        return resolveTemplateString(
            expression,
            requestInteractor.getDataContext(),
            contextManager,
        ) ?: expression
    }

    private fun parseMicroappContextTarget(expression: String): Pair<String, String>? {
        if (!expression.startsWith("@{") || !expression.endsWith("}")) return null
        val content = expression.substring(2, expression.length - 1)
        val parts = content.split(".", limit = 2)
        if (parts.size != 2) return null
        val microappCode = parts[0].trim()
        val variableName = parts[1].trim()
        if (microappCode.isEmpty() || variableName.isEmpty()) return null
        return microappCode to variableName
    }

    private fun parseEngineContextTarget(expression: String): String? {
        if (!expression.startsWith("@@{") || !expression.endsWith("}")) return null
        val content = expression.substring(3, expression.length - 1).trim()
        return content.ifEmpty { null }
    }

    private fun parseWidgetVariable(expression: String): Pair<String, String>? {
        if (!expression.startsWith("%{") || !expression.endsWith("}")) return null
        val content = expression.substring(2, expression.length - 1)
        val parts = content.split(".", limit = 2)
        if (parts.size != 2) return null
        val widgetCode = parts[0].trim()
        val parameter = parts[1].trim()
        if (widgetCode.isEmpty() || parameter.isEmpty()) return null
        return widgetCode to parameter
    }

    private fun handleDataTransform(action: UiAction.DataTransform): ActionResult {
        return ActionResult.Success
    }

    private suspend fun handleNativeCode(action: UiAction.NativeCode): ActionResult {
        val executor = NativeActionRegistry.getExecutor()
        if (executor == null) {
            Log.w("ActionHandler", "Исполнитель действий хоста не зарегистрирован, код: ${action.actionCode}")
            return ActionResult.Error("Исполнитель действий хоста не зарегистрирован")
        }

        return try {
            when (val result = executor.executeAction(action.actionCode, action.parameters)) {
                is NativeActionResult.Success -> {
                    if (microappCode != null) {
                        result.data?.forEach { (key, value) ->
                            contextManager.setMicroappVariable(
                                microappCode = microappCode,
                                variableName = key,
                                value = value,
                            )
                        }
                    } else {
                        Log.w("ActionHandler", "Код микроаппа null, результат NativeCode не сохранён в контекст")
                    }
                    ActionResult.Success
                }
                is NativeActionResult.Error -> {
                    ActionResult.Error(result.message, result.exception)
                }
            }
        } catch (e: Exception) {
            Log.e("ActionHandler", "Ошибка выполнения действия хоста: ${action.actionCode}", e)
            ActionResult.Error("Ошибка выполнения действия хоста: ${e.message}", e)
        }
    }
}
