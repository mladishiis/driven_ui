package com.example.drivenui.app.presentation.render

import android.content.Context
import android.util.Log
import com.example.drivenui.engine.generative_screen.action.NativeActionExecutor
import com.example.drivenui.engine.generative_screen.action.NativeActionResult

/**
 * Завела этот класс для тестирования
 * Пока пусть будет, позже уберу
 *
 * TODO: Убрать
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
                    Log.d("ExampleNativeActionExecutor",
                        "black action executed successfully")
                    NativeActionResult.Success()
                }
                else -> {
                    NativeActionResult.Error("Unknown action: $actionCode")
                }
            }
        } catch (e: Exception) {
            Log.e("ExampleNativeActionExecutor", "Error executing action: $actionCode", e)
            NativeActionResult.Error("${e.message}", e)
        }
    }
}
