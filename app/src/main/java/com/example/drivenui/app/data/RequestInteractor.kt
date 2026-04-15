package com.example.drivenui.app.data

import android.content.Context
import android.util.Log
import com.example.drivenui.engine.generative_screen.binding.DataContextProvider
import com.example.drivenui.engine.generative_screen.binding.ForLayoutBinding
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.parser.models.DataContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Интерактор для выполнения запросов и применения биндингов к экранам.
 *
 * @property appContext контекст приложения для доступа к assets и файловой системе
 */
@Singleton
class RequestInteractor @Inject constructor(
    private val appContext: Context
) {

    private val dataContextProvider = DataContextProvider(appContext)

    /**
     * При `ExecuteQuery.mockEnabled` и непустом `mockFile` загружает JSON в `DataContext.screenQueryResults`
     * под ключом `queryCode` (как в экшене).
     *
     * @param screenModel экран для обновления
     * @param action параметры запроса из разметки события
     * @return экран с применёнными биндингами
     */
    fun executeQueryAndUpdateScreen(
        screenModel: ScreenModel,
        action: UiAction.ExecuteQuery,
    ): ScreenModel {
        if (action.mockEnabled && !action.mockFile.isNullOrBlank()) {
            val fileName = action.mockFile.trim()
            try {
                val jsonData = dataContextProvider.loadJsonSmart(fileName)
                if (jsonData != null) {
                    dataContextProvider.addScreenQueryResult(action.queryCode, jsonData)
                } else {
                    Log.e(TAG, "Не удалось загрузить mock-файл: $fileName, queryCode=${action.queryCode}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка загрузки mock-файла: $fileName", e)
            }
        }

        return ForLayoutBinding.applyBindings(
            screenModel,
            dataContextProvider.getDataContext(),
        )
    }

    /**
     * Применяет биндинги к экрану без выполнения запросов.
     *
     * @param screenModel экран для применения биндингов
     * 
     * @return экран с применёнными биндингами
     */
    fun applyBindingsToScreen(screenModel: ScreenModel): ScreenModel {
        return ForLayoutBinding.applyBindings(screenModel, dataContextProvider.getDataContext())
    }

    /**
     * Возвращает `ForLayoutBinding` для обхода дерева и `resolvedMaxForIndex` на FOR-layout’ах.
     *
     * @return синглтон `ForLayoutBinding`
     */
    fun getForLayoutBinding(): ForLayoutBinding = ForLayoutBinding

    /**
     * Возвращает `DataContext` для применения биндингов в рендерерах.
     *
     * @return текущий контекст данных (JSON-источники, результаты запросов)
     */
    fun getDataContext(): DataContext = dataContextProvider.getDataContext()

    companion object {
        private const val TAG = "RequestInteractor"
    }
}