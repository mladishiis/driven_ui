package com.example.drivenui.engine.generative_screen.action

import android.util.Log
import com.example.drivenui.app.data.RequestInteractor
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.generative_screen.models.ScreenState
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.generative_screen.navigation.ScreenNavigationManager
import com.example.drivenui.engine.generative_screen.styles.resolveComponent
import com.example.drivenui.engine.generative_screen.styles.resolveScreen
import com.example.drivenui.engine.generative_screen.widget.IWidgetValueProvider
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.ImageModel
import com.example.drivenui.engine.uirender.models.InputModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel

class ActionHandler(
    private val navigationManager: ScreenNavigationManager,
    private val screenProvider: ScreenProvider,
    private val externalDeeplinkHandler: ExternalDeeplinkHandler,
    private val contextManager: IContextManager,
    private val widgetValueProvider: IWidgetValueProvider,
    private val requestInteractor: RequestInteractor,
    private val microappCode: String?,
    private val styleRegistry: ComposeStyleRegistry,
) {

    suspend fun handleAction(action: UiAction): ActionResult {
        return try {
            when (action) {
                is UiAction.OpenScreen -> handleOpenScreen(action.screenCode)
                is UiAction.OpenBottomSheet -> handleOpenBottomSheet(action.screenCode)
                is UiAction.Back -> handleBack()
                is UiAction.OpenDeeplink -> handleOpenDeeplink(action.deeplink)
                is UiAction.RefreshScreen -> handleRefreshScreen(action.screenCode)
                is UiAction.ExecuteQuery -> handleExecuteQuery(action.queryCode)
                is UiAction.DataTransform -> handleDataTransform(action)
                is UiAction.SaveToContext -> handleSaveToContext(action)
                is UiAction.NativeCode -> handleNativeCode(action)
                is UiAction.RefreshWidget -> handleRefreshWidget(action.widgetCode)
                is UiAction.RefreshLayout -> handleRefreshLayout(action.layoutCode)
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

    private suspend fun handleOpenBottomSheet(screenCode: String): ActionResult {
        val screenModel = screenProvider.findScreen(screenCode)
            ?: return ActionResult.Error("Bottom sheet screen not found: $screenCode")

        val resolvedScreen = resolveScreen(screenModel, contextManager, styleRegistry)
        return ActionResult.BottomSheetChanged(resolvedScreen)
    }

    private fun handleBack(): ActionResult {
        val previousScreen = navigationManager.popScreen()
        return if (previousScreen != null) {
            ActionResult.NavigationChanged(isBack = true)
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
        val resolvedScreen = resolveScreen(screenModel, contextManager, styleRegistry)
        navigationManager.pushScreen(ScreenState.fromDefinition(resolvedScreen))
        return ActionResult.NavigationChanged(isBack = false)
    }

    /**
     * Метод для обновления всего экранаа
     */
    private suspend fun handleRefreshScreen(screenCode: String): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
        val definition = currentScreen?.definition
            ?: return ActionResult.Error("No current screen to refresh")

        val screenWithBindings = requestInteractor.applyBindingsToScreen(definition)
        val resolvedScreen = resolveScreen(screenWithBindings, contextManager, styleRegistry)
        navigationManager.updateCurrentScreen(ScreenState.fromDefinition(resolvedScreen))

        return ActionResult.Success
    }

    /**
     * Метод для обновления конкретного виджета
     */
    private fun handleRefreshWidget(widgetCode: String): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
        val definition = currentScreen?.definition
            ?: return ActionResult.Error("No current screen to refresh widget on")

        val updatedRoot = refreshWidgetInComponent(definition.rootComponent, widgetCode)
        val updatedScreen = definition.copy(rootComponent = updatedRoot)

        navigationManager.updateCurrentScreen(ScreenState.fromDefinition(updatedScreen))
        return ActionResult.Success
    }

    /**
     * Метод для обновления конкретного лейаута
     * Пока что реализовано как обновление всего экрана
     */
    private fun handleRefreshLayout(layoutCode: String): ActionResult {
        val currentScreen = navigationManager.getCurrentScreen()
        val definition = currentScreen?.definition
            ?: return ActionResult.Error("No current screen to refresh layout on")

        val resolvedScreen = resolveScreen(definition, contextManager, styleRegistry)
        navigationManager.updateCurrentScreen(ScreenState.fromDefinition(resolvedScreen))
        return ActionResult.Success
    }

    //TODO: рефакторинг или вынести куда-то вообще
    private fun refreshWidgetInComponent(
        component: ComponentModel?,
        widgetCode: String
    ): ComponentModel? {
        return when (component) {
            null -> null
            is LayoutModel -> component.copy(
                children = component.children.map { child ->
                    refreshWidgetInComponent(child, widgetCode) ?: child
                }
            )
            is InputModel -> {
                if (component.widgetCode == widgetCode) {
                    resolveComponent(component, contextManager, styleRegistry) as? InputModel ?: component
                } else {
                    component
                }
            }
            is ButtonModel -> {
                if (component.widgetCode == widgetCode) {
                    resolveComponent(component, contextManager, styleRegistry) as? ButtonModel ?: component
                } else {
                    component
                }
            }
            is LabelModel -> {
                if (component.widgetCode == widgetCode) {
                    resolveComponent(component, contextManager, styleRegistry) as? LabelModel ?: component
                } else {
                    component
                }
            }
            is ImageModel -> {
                if (component.widgetCode == widgetCode) {
                    resolveComponent(component, contextManager, styleRegistry) as? ImageModel ?: component
                } else {
                    component
                }
            }
            is AppBarModel -> {
                if (component.widgetCode == widgetCode) {
                    resolveComponent(component, contextManager, styleRegistry) as? AppBarModel ?: component
                } else {
                    component
                }
            }
            else -> component
        }
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

        val resolvedScreen = resolveScreen(processedScreen, contextManager, styleRegistry)
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

    // TODO: подумать над рефакторингом
    data class NavigationChanged(val isBack: Boolean) : ActionResult()

    /**
     * Изменение состояния нижней шторки (bottom sheet).
     * model == null означает закрытие шторки.
     */
    data class BottomSheetChanged(val model: ScreenModel?) : ActionResult()

    data class Error(val message: String, val exception: Exception? = null) : ActionResult()
}

interface ScreenProvider {
    suspend fun findScreen(screenCode: String): ScreenModel?
    suspend fun findScreenByDeeplink(deeplink: String): ScreenModel?
}
