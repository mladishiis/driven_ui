package com.example.drivenui.app.domain

import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.parser.SDUIParser

/**
 * Интерактор для работы с microapp:
 * - парсинг SDUI-описаний,
 * - маппинг в ScreenModel,
 * - хранение замапленного результата,
 * - валидация и сбор диагностической информации.
 *
 * Инкапсулирует логику загрузки и разбора microapp
 * независимо от источника данных (assets, file system и т.д.).
 */
interface FileInteractor {

    /**
     * Запускает парсинг microapp, маппинг и сохранение в кэш.
     *
     * Выполняет чтение всех необходимых файлов (microapp, styles, queries, screens),
     * парсинг, маппинг в ScreenModel и сохранение CachedMicroappData.
     *
     * @return результат парсинга microapp (для обратной совместимости с деталями парсинга)
     * @throws Exception если произошла ошибка чтения или парсинга
     */
    suspend fun parseMicroapp(): SDUIParser.ParsedMicroappResult

    /**
     * Запускает парсинг шаблона (урезанный микроапп для теста визуала), маппинг и сохранение.
     * Обязательны: экран(ы) и allStyles.xml. microapp.xml и allQueries.xml могут отсутствовать.
     *
     * @return результат парсинга
     * @throws Exception если отсутствуют экраны или allStyles.xml
     */
    suspend fun parseTemplate(): SDUIParser.ParsedMicroappResult

    /**
     * Загружает микроапп из кэша без парсинга и маппинга.
     *
     * @param microappCode код микроаппа (microapp.code)
     * @return замапленные данные или `null`, если не найден в хранилище
     */
    suspend fun loadCachedMicroapp(microappCode: String): CachedMicroappData?

    /**
     * Возвращает последний успешно сохранённый результат парсинга.
     *
     * @return последний результат парсинга или `null`, если парсинг ещё не выполнялся
     */
    fun getLastParsedResult(): SDUIParser.ParsedMicroappResult?

    /**
     * Сохраняет результат парсинга для последующего использования
     * (отладка, повторное применение, аналитика).
     *
     * @param parsedMicroapp результат парсинга microapp
     */
    fun saveParsedResult(parsedMicroapp: SDUIParser.ParsedMicroappResult)

    /**
     * Выполняет валидацию результата парсинга.
     *
     * Может использоваться для проверки целостности microapp:
     * - наличие экранов,
     * - корректность биндингов,
     * - обязательные сущности и т.д.
     *
     * @param result результат парсинга для проверки
     * @return `true`, если результат валиден, иначе `false`
     */
    suspend fun validateParsingResult(
        result: SDUIParser.ParsedMicroappResult
    ): Boolean

    /**
     * Возвращает разрешённые значения биндингов,
     * полученные в результате последнего парсинга.
     *
     * Ключ — имя биндинга, значение — итоговое строковое значение.
     *
     * @return map с разрешёнными биндингами или пустую map, если данных нет
     */
    fun getResolvedValues(): Map<String, String>

    /**
     * Возвращает статистику парсинга microapp.
     *
     * Может содержать:
     * - количество экранов,
     * - количество компонентов,
     * - количество ошибок/предупреждений и т.д.
     *
     * @return map со статистикой парсинга или `null`, если данных нет
     */
    fun getParsingStats(): Map<String, Any>?

    /**
     * Возвращает статистику биндингов.
     *
     * Может содержать:
     * - количество разрешённых биндингов,
     * - наличие DataContext,
     * - количество screen queries и т.д.
     *
     * @return map со статистикой биндингов или `null`, если данных нет
     */
    fun getBindingStats(): Map<String, Any>?

    /**
     * Очищает сохранённые данные парсинга и связанные с ними состояния.
     *
     * Используется при повторной загрузке microapp
     * или при сбросе состояния приложения.
     */
    fun clearParsedData()

    /**
     * Возвращает список доступных JSON-файлов,
     * которые могут использоваться в качестве mock-данных
     * для screen queries или биндингов.
     *
     * @return список имён JSON-файлов
     */
    fun getAvailableJsonFiles(): List<String>
}