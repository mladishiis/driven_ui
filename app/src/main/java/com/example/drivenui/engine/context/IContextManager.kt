package com.example.drivenui.engine.context

interface IContextManager {

    fun setMicroappVariable(microappCode: String, variableName: String, value: Any)

    fun getMicroappVariable(microappCode: String, variableName: String): Any?

    fun setEngineVariable(variableName: String, value: Any)

    fun getEngineVariable(variableName: String): Any?

    fun getMicroappContext(microappCode: String): Map<String, Any>

    fun getEngineContext(): Map<String, Any>

    fun clearMicroappContext(microappCode: String)

    fun clearEngineContext()

    fun clearAll()
}
