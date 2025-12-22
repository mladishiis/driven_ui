package com.example.drivenui.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.parser.models.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Репозиторий для управления микроаппами
 *
 * Обеспечивает загрузку, кэширование и навигацию по микроаппам
 */
class MicroappRepository(private val parser: SDUIParser) {

    private val _currentScreen = MutableLiveData<Screen?>()

    /**
     * LiveData текущего экрана микроаппа
     */
    val currentScreen: LiveData<Screen?> = _currentScreen

    private val _navigationStack = mutableListOf<Screen>()

    /**
     * Загружает микроапп из файла в assets
     *
     * @param fileName Имя файла в папке assets
     * @return Результат парсинга микроаппа
     */
    suspend fun loadMicroappFromAssets(fileName: String) = withContext(Dispatchers.IO) {
        val parsedMicroapp = parser.parseFromAssets(fileName)

        if (parsedMicroapp.screens.isNotEmpty()) {
            _currentScreen.postValue(parsedMicroapp.screens.first())
            _navigationStack.clear()
            _navigationStack.add(parsedMicroapp.screens.first())
        }

        parsedMicroapp
    }

    /**
     * Переходит к указанному экрану
     *
     * @param screenCode Код экрана для перехода
     * @param screens Список всех экранов микроаппа
     */
    fun navigateToScreen(screenCode: String, screens: List<Screen>) {
        val screen = screens.find { it.screenCode == screenCode }
        screen?.let {
            _navigationStack.add(it)
            _currentScreen.value = it
        }
    }

    /**
     * Возвращается к предыдущему экрану
     *
     * @return true если навигация назад выполнена, false если это первый экран
     */
    fun navigateBack(): Boolean {
        return if (_navigationStack.size > 1) {
            _navigationStack.removeLast()
            _currentScreen.value = _navigationStack.last()
            true
        } else {
            false
        }
    }
}