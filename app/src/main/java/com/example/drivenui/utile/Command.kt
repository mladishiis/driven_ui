package com.example.drivenui.utile

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Создает класс для отправки команд (подходит для показа тоста или диалогового окна). См. [Command]
 * Чтобы наблюдать за изменениями значений используйте
 * [PropertyObserver.bind], [PropertyObserver.bindWhenCreate], [PropertyObserver.bindWhenResumed]
 */
fun <T> command() =
    Command<T>()

/** Отправляет команду без аргументов */
operator fun Command<Unit>.invoke() =
    invoke(Unit)

/**
 * Класс для отправки команд (подходит для показа тоста или диалогового окна). См. [utils.command]
 * Чтобы наблюдать за изменениями значений используйте
 * [PropertyObserver.bind], [PropertyObserver.bindWhenCreate], [PropertyObserver.bindWhenResumed]
 * @property flow поток для подписки на команду
 */
class Command<T> internal constructor(
    private val channel: Channel<T> =
        Channel(Channel.CONFLATED),
    val flow: Flow<T> =
        channel.receiveAsFlow(),
) : Flow<T> by flow {

    /** Текущее значение в команде */
    var value: T? = null
        private set

    /** Отправляет команду */
    operator fun invoke(value: T) {
        this.value = value
        channel.trySend(value)
    }
}
