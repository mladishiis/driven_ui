package com.example.drivenui.utile

import androidx.lifecycle.ViewModel
import kotlin.coroutines.cancellation.CancellationException

/**
 * Базовая вью модель, содержит индикатор загрузки, ошибку и шаблон навигации
 */
abstract class CoreMviViewModel<Event : VtbEvent, State : VtbState, Effect : VtbEffect> : ViewModel() {

    /** Модель состояния экрана */
    val uiState get() = _uiState.flow

    /** Какие-либо триггеры, на которые надо подписаться в Compose методе и реагировать */
    val effect get() = _effect.flow

    private val _effect by lazy { command<Effect>() }
    private val _uiState by lazy { state(createInitialState()) }

    /**
     * Обновить state экрана через [reducer].
     *
     * @param reducer Функция преобразования текущего state в новый
     */
    protected fun updateState(reducer: State.() -> State) {
        val newState = uiState.value.reducer()
        _uiState(newState)
    }

    /**
     * Отправить на экран событие [builder], на которое надо как-то среагировать.
     *
     * @param builder Функция, создающая эффект
     */
    protected fun setEffect(builder: () -> Effect) {
        val effectValue = builder()
        _effect(effectValue)
    }

    /** Стартовое значение state экрана, например Loading */
    abstract fun createInitialState(): State

    /** Обработать поступившее с экрана событие [event] */
    abstract fun handleEvent(event: Event)

    /**
     * Отлов ошибки корутины, с пробросом CancellationException для корректного её завершения.
     *
     * @param finally Лямбда для выполнения в finally
     * @param block Защищаемый от краша код
     * @return Result с успешным значением или ошибкой
     */
    protected suspend fun <R> runCatchingCancellable(finally: () -> Unit = {}, block: suspend () -> R): Result<R> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        } finally {
            finally()
        }
    }
}

/**
 * Маркерный интерфейс для модели состояния экрана (MVI State).
 */
interface VtbState

/**
 * Маркерный интерфейс для событий с экрана во ViewModel (MVI Event).
 */
interface VtbEvent

/**
 * Маркерный интерфейс для эффектов с ViewModel на экран (MVI Effect).
 */
interface VtbEffect