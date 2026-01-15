package com.example.drivenui.presentation.render

import androidx.fragment.app.FragmentActivity
import com.example.drivenui.engine.generative_screen.action.NativeActionExecutor
import com.example.drivenui.engine.generative_screen.action.NativeActionResult

/**
 * Завела этот класс для тестирования
 * Пока пусть будет, позже уберу
 *
 * TODO: Убрать
 */
class ExampleNativeActionExecutor(
    private val activity: FragmentActivity
) : NativeActionExecutor {

    override suspend fun executeAction(
        actionCode: String,
        parameters: Map<String, String>
    ): NativeActionResult {
        return try {
            when (actionCode) {
                "black" -> {
                    setMinBrightness()
                    NativeActionResult.Success()
                }
                else -> {
                    NativeActionResult.Error("Unknown action: $actionCode")
                }
            }
        } catch (e: Exception) {
            NativeActionResult.Error("${e.message}", e)
        }
    }

    private fun setMinBrightness() {
        activity.runOnUiThread {
            val window = activity.window
            val layoutParams = window.attributes
            layoutParams.screenBrightness = 0.0f
            window.attributes = layoutParams
        }
    }
}
