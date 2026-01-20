package com.example.drivenui.engine.generative_screen.action

import android.util.Log
import com.example.drivenui.data.RequestInteractor
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.context.resolveScreen
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.generative_screen.models.ScreenState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.generative_screen.navigation.ScreenNavigationManager
import com.example.drivenui.engine.generative_screen.widget.IWidgetValueProvider

class ActionHandler(
    private val navigationManager: ScreenNavigationManager,
    private val screenProvider: ScreenProvider,
    private val externalDeeplinkHandler: ExternalDeeplinkHandler,
    private val contextManager: IContextManager,
    private val widgetValueProvider: IWidgetValueProvider,
    private val requestInteractor: RequestInteractor,
    private val microappCode: String?
) {

    suspend fun handleAction(action: UiAction): ActionResult {
        return try {
            when (action) {
                is UiAction.OpenScreen -> handleOpenScreen(action.screenCode)
                is UiAction.Back -> handleBack()
                is UiAction.OpenDeeplink -> handleOpenDeeplink(action.deeplink)
                is UiAction.RefreshScreen -> handleRefreshScreen(action.screenCode)
                is UiAction.ExecuteQuery -> handleExecuteQuery(action.queryCode)
                is UiAction.DataTransform -> handleDataTransform(action)
                is UiAction.SaveToContext -> handleSaveToContext(action)
                is UiAction.NativeCode -> handleNativeCode(action)
                is UiAction.RefreshWidget -> ActionResult.Success
                is UiAction.RefreshLayout -> ActionResult.Success
                is UiAction.Empty -> ActionResult.Success
            }
        } catch (e: Exception) {
            Log.e("ActionHandler", "Error handling action: ${action::class.simpleName}", e)
            ActionResult.Error("Error handling action: ${e.message}", e)
        }
    }

    private suspend fun handleOpenScreen(screenCode: String): ActionResult {
        val screenModel = screenProvider.findScreen(screenCode)
            ?: return ActionResult.Error("Screen not found: $screenCode")

        return openResolvedScreen(screenModel)
    }

    private fun handleBack(): ActionResult {
        val previousScreen = navigationManager.popScreen()
        return if (previousScreen != null) {
            ActionResult.NavigationChanged
        } else {
            ActionResult.Error("Cannot navigate back: already at root")
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
            ActionResult.Error("Deeplink not found: $deeplink")
        }
    }

    /**
     * Резолвит переменные контекста в экране и пушит результат в стек навигации.
     */
    private fun openResolvedScreen(screenModel: ScreenModel): ActionResult {
        val resolvedScreen = resolveScreen(screenModel, contextManager)
        navigationManager.pushScreen(ScreenState.fromDefinition(resolvedScreen))
        return ActionResult.NavigationChanged
    }

    private suspend fun handleRefreshScreen(screenCode: String): ActionResult {
        return ActionResult.Success
    }

    private suspend fun handleExecuteQuery(queryCode: String): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
        if (currentScreen?.definition == null) {
            return ActionResult.Error("No current screen to execute query on")
        }

        val processedScreen = requestInteractor.executeQueryAndUpdateScreen(
            screenModel = currentScreen.definition,
            queryCode = queryCode
        )

        val resolvedScreen = resolveScreen(processedScreen, contextManager)
        navigationManager.updateCurrentScreen(ScreenState.fromDefinition(resolvedScreen))

        return ActionResult.Success
    }

    private fun handleSaveToContext(action: UiAction.SaveToContext): ActionResult {
        val sourceValue = resolveValueFrom(action.valueFrom)
        if (sourceValue == null) {
            return ActionResult.Error("Failed to resolve value from: ${action.valueFrom}")
        }

        parseMicroappContextTarget(action.valueTo)?.let { (microappCode, variableName) ->
            contextManager.setMicroappVariable(
                microappCode = microappCode,
                variableName = variableName,
                value = sourceValue
            )
            return ActionResult.Success
        }

        parseEngineContextTarget(action.valueTo)?.let { variableName ->
            contextManager.setEngineVariable(
                variableName = variableName,
                value = sourceValue
            )
            return ActionResult.Success
        }

        return ActionResult.Error(
            "Invalid target format for saveToContext: ${action.valueTo}. Expected @{microappCode.variableName} or @@{variableName}"
        )
    }

    private fun resolveValueFrom(expression: String): Any? {
        parseWidgetVariable(expression)?.let { (widgetCode, parameter) ->
            return widgetValueProvider.getWidgetValue(
                widgetCode = widgetCode,
                parameter = parameter
            )
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
            Log.w("ActionHandler", "HostActionExecutor not registered. Cannot execute action: ${action.actionCode}")
            return ActionResult.Error("Host action executor not registered")
        }

        return try {
            when (val result = executor.executeAction(action.actionCode, action.parameters)) {
                is NativeActionResult.Success -> {
                    if (microappCode != null) {
                        result.data?.forEach { (key, value) ->
                            contextManager.setMicroappVariable(
                                microappCode = microappCode,
                                variableName = key,
                                value = value
                            )
                        }
                    } else {
                        Log.w("ActionHandler", "Microapp code is null, cannot save NativeCode result to microapp context")
                    }
                    ActionResult.Success
                }
                is NativeActionResult.Error -> {
                    ActionResult.Error(result.message, result.exception)
                }
            }
        } catch (e: Exception) {
            Log.e("ActionHandler", "Error executing host action: ${action.actionCode}", e)
            ActionResult.Error("Error executing host action: ${e.message}", e)
        }
    }
}

sealed class ActionResult {
    data object Success : ActionResult()

    data object NavigationChanged : ActionResult()

    data class Error(val message: String, val exception: Exception? = null) : ActionResult()
}

interface ScreenProvider {
    suspend fun findScreen(screenCode: String): ScreenModel?
    suspend fun findScreenByDeeplink(deeplink: String): ScreenModel?
}
