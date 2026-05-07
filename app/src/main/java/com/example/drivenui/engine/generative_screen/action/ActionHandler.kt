package com.example.drivenui.engine.generative_screen.action

import android.content.Context
import android.util.Log
import com.example.drivenui.app.theme.isSystemInDarkTheme
import com.example.drivenui.app.data.RequestInteractor
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.generative_screen.models.ScreenState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.generative_screen.navigation.ScreenNavigationManager
import com.example.drivenui.engine.generative_screen.styles.resolveScreen
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
     * @param action Действие для обработки
     * @return Результат выполнения действия
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
        val screenModel = screenProvider.findScreen(screenCode)
            ?: return ActionResult.Error("Экран не найден: $screenCode")

        return openResolvedScreen(screenModel)
    }

    private suspend fun handleOpenBottomSheet(screenCode: String): ActionResult {
        val screenModel = screenProvider.findScreen(screenCode)
            ?: return ActionResult.Error("Экран для нижней шторки не найден: $screenCode")

        val resolvedScreen = resolveScreen(
            screenModel,
            contextManager,
            styleRegistry,
            requestInteractor.getDataContext(),
            applicationContext.isSystemInDarkTheme(),
        )
        return ActionResult.BottomSheetChanged(resolvedScreen)
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
        val screenModel = screenProvider.findScreenByDeeplink(deeplink)
        if (screenModel != null) {
            return openResolvedScreen(screenModel)
        }

        val handled = externalDeeplinkHandler.handleExternalDeeplink(deeplink)
        return if (handled) {
            ActionResult.Success
        } else {
            ActionResult.Error("Deeplink не найден: $deeplink")
        }
    }

    /**
     * Резолвит переменные контекста в экране и пушит результат в стек навигации.
     */
    private fun openResolvedScreen(screenModel: ScreenModel): ActionResult {
        val resolvedScreen = resolveScreen(
            screenModel,
            contextManager,
            styleRegistry,
            requestInteractor.getDataContext(),
            applicationContext.isSystemInDarkTheme(),
        )
        navigationManager.pushScreen(
            ScreenState.fromDefinition(
                definition = resolvedScreen,
                sourceDefinition = screenModel,
            )
        )
        return ActionResult.NavigationChanged(isBack = false)
    }

    /**
     * Метод для обновления всего экранаа
     */
    private suspend fun handleRefreshScreen(@Suppress("UNUSED_PARAMETER") screenCode: String): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
        val definition = currentScreen?.sourceDefinition ?: currentScreen?.definition
            ?: return ActionResult.Error("Нет текущего экрана для обновления")

        val screenWithBindings = requestInteractor.applyBindingsToScreen(definition)
        val resolvedScreen = resolveScreen(
            screenWithBindings,
            contextManager,
            styleRegistry,
            requestInteractor.getDataContext(),
            applicationContext.isSystemInDarkTheme(),
        )
        navigationManager.updateCurrentScreen(
            ScreenState.fromDefinition(
                definition = resolvedScreen,
                sourceDefinition = definition,
            )
        )

        return ActionResult.Success
    }

    /**
     * Обновляет экран: повторно применяет биндинги (FOR) и полный резолв шаблонов в поля display*.
     */
    private suspend fun handleRefreshWidget(@Suppress("UNUSED_PARAMETER") widgetCode: String): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
        val definition = currentScreen?.sourceDefinition ?: currentScreen?.definition
            ?: return ActionResult.Error("Нет текущего экрана для обновления виджета")

        val screenWithBindings = requestInteractor.applyBindingsToScreen(definition)
        val resolvedScreen = resolveScreen(
            screenWithBindings,
            contextManager,
            styleRegistry,
            requestInteractor.getDataContext(),
            applicationContext.isSystemInDarkTheme(),
        )
        navigationManager.updateCurrentScreen(
            ScreenState.fromDefinition(
                definition = resolvedScreen,
                sourceDefinition = definition,
            )
        )
        return ActionResult.Success
    }

    /**
     * Метод для обновления конкретного лейаута
     * Пока что реализовано как обновление всего экрана
     */
    private suspend fun handleRefreshLayout(@Suppress("UNUSED_PARAMETER") layoutCode: String): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
        val definition = currentScreen?.sourceDefinition ?: currentScreen?.definition
            ?: return ActionResult.Error("Нет текущего экрана для обновления лейаута")

        val screenWithBindings = requestInteractor.applyBindingsToScreen(definition)
        val resolvedScreen = resolveScreen(
            screenWithBindings,
            contextManager,
            styleRegistry,
            requestInteractor.getDataContext(),
            applicationContext.isSystemInDarkTheme(),
        )
        navigationManager.updateCurrentScreen(
            ScreenState.fromDefinition(
                definition = resolvedScreen,
                sourceDefinition = definition,
            )
        )
        return ActionResult.Success
    }

    private suspend fun handleExecuteQuery(action: UiAction.ExecuteQuery): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
        val definition = currentScreen?.sourceDefinition ?: currentScreen?.definition
        if (definition == null) {
            return ActionResult.Error("Нет текущего экрана для выполнения запроса")
        }

        val processedScreen = requestInteractor.executeQueryAndUpdateScreen(
            screenModel = definition,
            action = action,
        )

        val resolvedScreen = resolveScreen(
            processedScreen,
            contextManager,
            styleRegistry,
            requestInteractor.getDataContext(),
            applicationContext.isSystemInDarkTheme(),
        )
        navigationManager.updateCurrentScreen(
            ScreenState.fromDefinition(
                definition = resolvedScreen,
                sourceDefinition = definition,
            )
        )

        return ActionResult.Success
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

        return expression
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
