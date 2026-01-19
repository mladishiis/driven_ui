package com.example.drivenui.engine.context

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextManager @Inject constructor() : IContextManager {

    private val microappContexts = mutableMapOf<String, MutableMap<String, Any>>()

    private val engineContext = mutableMapOf<String, Any>()

    override fun setMicroappVariable(microappCode: String, variableName: String, value: Any) {
        val context = microappContexts.getOrPut(microappCode) { mutableMapOf() }
        context[variableName] = value
        Log.d("ContextManager", "Set microapp variable: $microappCode.$variableName = $value")
    }

    override fun getMicroappVariable(microappCode: String, variableName: String): Any? {
        return microappContexts[microappCode]?.get(variableName)
    }

    override fun setEngineVariable(variableName: String, value: Any) {
        engineContext[variableName] = value
        Log.d("ContextManager", "Set engine variable: $variableName = $value")
    }

    override fun getEngineVariable(variableName: String): Any? {
        return engineContext[variableName]
    }

    override fun getMicroappContext(microappCode: String): Map<String, Any> {
        return microappContexts[microappCode]?.toMap() ?: emptyMap()
    }

    override fun getEngineContext(): Map<String, Any> {
        return engineContext.toMap()
    }

    override fun clearMicroappContext(microappCode: String) {
        microappContexts.remove(microappCode)
        Log.d("ContextManager", "Cleared microapp context: $microappCode")
    }

    override fun clearEngineContext() {
        engineContext.clear()
        Log.d("ContextManager", "Cleared engine context")
    }

    override fun clearAll() {
        microappContexts.clear()
        engineContext.clear()
        Log.d("ContextManager", "Cleared all contexts")
    }
}