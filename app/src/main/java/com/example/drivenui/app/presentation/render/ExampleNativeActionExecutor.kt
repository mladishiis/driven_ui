package com.example.drivenui.app.presentation.render

import android.content.Context
import android.util.Log
import com.example.drivenui.engine.generative_screen.action.NativeActionExecutor
import com.example.drivenui.engine.generative_screen.action.NativeActionResult

/**
 * Заглушка [NativeActionExecutor] для тестирования.
 *
 * @property context контекст приложения
 * @todo Убрать после реализации реального исполнителя
 */
class ExampleNativeActionExecutor(
    private val context: Context
) : NativeActionExecutor {

    override suspend fun executeAction(
        actionCode: String,
        parameters: Map<String, String>
    ): NativeActionResult {
        return try {
            when (actionCode) {
                "black" -> {
                    NativeActionResult.Success()
                }
                else -> {
                    NativeActionResult.Error("Неизвестное действие: $actionCode")
                }
            }
        } catch (e: Exception) {
            Log.e("ExampleNativeActionExecutor", "пакет=${context.packageName} действие=$actionCode", e)
            NativeActionResult.Error("${e.message}", e)
        }
    }
}
