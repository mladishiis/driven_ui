package com.example.drivenui.utile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Создает класс состояния аналог [kotlinx.coroutines.flow.MutableStateFlow]
 * Чтобы наблюдать за изменениями значений используйте
 * [PropertyObserver.bind], [PropertyObserver.bindWhenCreate], [PropertyObserver.bindWhenResumed]
 */
fun <T> state(initialValue: T) =
    State(initialValue)

/**
 * Класс состояния аналог [kotlinx.coroutines.flow.MutableStateFlow]
 * Чтобы наблюдать за изменениями значений используйте
 * [PropertyObserver.bind], [PropertyObserver.bindWhenCreate], [PropertyObserver.bindWhenResumed]
 * @property flow поток для подписки на состояние
 */
class State<T> internal constructor(
    initialValue: T,
    private val stateFlow: MutableStateFlow<T> =
        MutableStateFlow(initialValue),
    val flow: StateFlow<T> =
        stateFlow.asStateFlow()
) : Flow<T> by flow {

    /** Текущее значение состяния */
    val value: T
        get() = stateFlow.value

    /** Отправляет новое значение */
    operator fun invoke(newValue: T) {
        stateFlow.value = newValue
    }
}
