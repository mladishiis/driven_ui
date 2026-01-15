package com.example.drivenui.engine.generative_screen.action

import android.util.Log
import com.example.drivenui.engine.generative_screen.context.ScreenContextManager
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.generative_screen.models.ScreenState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.generative_screen.navigation.ScreenNavigationManager

class ActionHandler(
    private val navigationManager: ScreenNavigationManager,
    private val screenProvider: ScreenProvider,
    private val contextManager: ScreenContextManager,
    private val externalDeeplinkHandler: ExternalDeeplinkHandler
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

        navigationManager.pushScreen(ScreenState.fromDefinition(screenModel))
        return ActionResult.Success
    }

    private fun handleBack(): ActionResult {
        val previousScreen = navigationManager.popScreen()
        return if (previousScreen != null) {
            ActionResult.Success
        } else {
            ActionResult.Error("Cannot navigate back: already at root")
        }
    }

    private suspend fun handleOpenDeeplink(deeplink: String): ActionResult {
        val screenModel = screenProvider.findScreenByDeeplink(deeplink)
        if (screenModel != null) {
            navigationManager.pushScreen(ScreenState.fromDefinition(screenModel))
            return ActionResult.Success
        }

        val handled = externalDeeplinkHandler.handleExternalDeeplink(deeplink)
        return if (handled) {
            ActionResult.Success
        } else {
            ActionResult.Error("Deeplink not found: $deeplink")
        }
    }

    private suspend fun handleRefreshScreen(screenCode: String): ActionResult {
        return ActionResult.Success
    }

    private suspend fun handleExecuteQuery(queryCode: String): ActionResult {
        return ActionResult.Success
    }

    private fun handleSaveToContext(action: UiAction.SaveToContext): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
        val sourceData = buildMap<String, Any> {
            currentScreen?.data?.let { putAll(it) }
            putAll(contextManager.getContext())
        }

        val success = contextManager.saveFromSource(
            targetKey = action.valueTo,
            sourceKey = action.valueFrom,
            sourceData = sourceData
        )

        return if (success) {
            ActionResult.Success
        } else {
            ActionResult.Error("Failed to save context: source '${action.valueFrom}' not found")
        }
    }

    private fun handleDataTransform(action: UiAction.DataTransform): ActionResult {
        contextManager.setVariable(action.variableName, action.newValue)
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
                    result.data?.forEach { (key, value) ->
                        contextManager.setVariable(key, value)
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
    data class Error(val message: String, val exception: Exception? = null) : ActionResult()
}

interface ScreenProvider {
    suspend fun findScreen(screenCode: String): ScreenModel?
    suspend fun findScreenByDeeplink(deeplink: String): ScreenModel?
}
