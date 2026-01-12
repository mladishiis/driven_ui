package com.example.drivenui.engine.generative_screen.context

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenContextManager {

    private val _contextVariables = MutableStateFlow<Map<String, Any>>(emptyMap())

    val contextVariables: StateFlow<Map<String, Any>> = _contextVariables.asStateFlow()

    fun getContext(): Map<String, Any> = _contextVariables.value

    fun getVariable(key: String): Any? {
        return _contextVariables.value[key]
    }

    fun getVariableAsString(key: String, defaultValue: String = ""): String {
        return when (val value = getVariable(key)) {
            is String -> value
            is Number -> value.toString()
            is Boolean -> value.toString()
            null -> defaultValue
            else -> value.toString()
        }
    }

    fun getVariableAsInt(key: String, defaultValue: Int = 0): Int {
        return when (val value = getVariable(key)) {
            is Int -> value
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: defaultValue
            else -> defaultValue
        }
    }

    fun setVariable(key: String, value: Any) {
        val current = _contextVariables.value.toMutableMap()
        current[key] = value
        _contextVariables.value = current
        Log.d("ScreenContextManager", "Set variable: $key = $value")
    }

    fun setVariables(variables: Map<String, Any>) {
        val current = _contextVariables.value.toMutableMap()
        current.putAll(variables)
        _contextVariables.value = current
        Log.d("ScreenContextManager", "Set ${variables.size} variables")
    }

    fun saveFromSource(
        targetKey: String,
        sourceKey: String,
        sourceData: Map<String, Any>
    ): Boolean {
        val sourceValue = sourceData[sourceKey] ?: return false
        setVariable(targetKey, sourceValue)
        return true
    }

    fun removeVariable(key: String) {
        val current = _contextVariables.value.toMutableMap()
        if (current.remove(key) != null) {
            _contextVariables.value = current
            Log.d("ScreenContextManager", "Removed variable: $key")
        }
    }

    fun hasVariable(key: String): Boolean {
        return _contextVariables.value.containsKey(key)
    }

    fun clear() {
        _contextVariables.value = emptyMap()
        Log.d("ScreenContextManager", "Context cleared")
    }

    fun clearWithPrefix(prefix: String) {
        val current = _contextVariables.value.toMutableMap()
        val keysToRemove = current.keys.filter { it.startsWith(prefix) }
        keysToRemove.forEach { current.remove(it) }
        _contextVariables.value = current
        Log.d("ScreenContextManager", "Cleared ${keysToRemove.size} variables with prefix: $prefix")
    }

    fun getAllKeys(): Set<String> = _contextVariables.value.keys

    fun toDataContextAppState(): Map<String, Any> = _contextVariables.value
}
