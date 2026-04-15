package com.example.drivenui.engine.context

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация [IContextManager] для хранения переменных микроаппов и движка в памяти.
 */
@Singleton
class ContextManager @Inject constructor() : IContextManager {

    private val microappContexts = mutableMapOf<String, MutableMap<String, Any>>()

    private val engineContext = mutableMapOf<String, Any>()

    override fun setMicroappVariable(microappCode: String, variableName: String, value: Any) {
        val context = microappContexts.getOrPut(microappCode) { mutableMapOf() }
        context[variableName] = value
    }

    override fun getMicroappVariable(microappCode: String, variableName: String): Any? {
        return microappContexts[microappCode]?.get(variableName)
    }

    override fun setEngineVariable(variableName: String, value: Any) {
        engineContext[variableName] = value
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
    }

    override fun clearEngineContext() {
        engineContext.clear()
    }

    override fun clearAll() {
        microappContexts.clear()
        engineContext.clear()
    }
}