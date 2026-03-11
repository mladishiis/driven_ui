package com.example.drivenui.app.domain

import com.example.drivenui.engine.cache.CachedMicroappData

/**
 * Хранилище замапленных микроаппов и метаданных (коллекция, список прототипов).
 * Всё хранится в parsed_microapps/: файлы микроаппов + meta/ с индексами.
 *
 * Используется для:
 * - Кэширования микроаппов после парсинга и маппинга
 * - Загрузки сохранённых микроаппов
 * - ID и кодов коллекции (синхронизация с сервером)
 * - Кодов списка прототипов (добавленных по одному)
 */
interface MicroappStorage {

    /**
     * Сохраняет замапленный микроапп.
     *
     * @param data замапленные данные микроаппа
     * @return код микроаппа, под которым сохранено, или null при ошибке
     */
    suspend fun saveMapped(data: CachedMicroappData): String?

    /**
     * Загружает сохранённый микроапп по коду без парсинга и маппинга.
     *
     * @param microappCode код микроаппа (microapp.code)
     * @return замапленные данные или null, если не найден
     */
    suspend fun loadMapped(microappCode: String): CachedMicroappData?

    /**
     * Возвращает коды всех сохранённых микроаппов.
     *
     * @return список кодов микроаппов
     */
    suspend fun getAllCodes(): List<String>

    /**
     * Удаляет сохранённый микроапп.
     *
     * @param microappCode код микроаппа для удаления
     */
    suspend fun delete(microappCode: String)

    /**
     * Проверяет наличие сохранённого микроаппа.
     *
     * @param microappCode код микроаппа для проверки
     * 
     * @return true, если микроапп найден в хранилище
     */
    suspend fun contains(microappCode: String): Boolean

    /**
     * Сохраняет ID коллекции микроаппов.
     *
     * @param id идентификатор коллекции
     */
    suspend fun saveCollectionId(id: String)

    /**
     * Возвращает сохранённый ID коллекции.
     *
     * @return ID коллекции или null
     */
    suspend fun getCollectionId(): String?

    /**
     * Сохраняет список кодов микроаппов в коллекции.
     *
     * @param codes список кодов микроаппов
     */
    suspend fun saveCollectionCodes(codes: List<String>)

    /**
     * Возвращает коды микроаппов из коллекции.
     *
     * @return список кодов
     */
    suspend fun getCollectionCodes(): List<String>

    /** Очищает сохранённые ID и коды коллекции. */
    suspend fun clearCollectionId()

    /**
     * Добавляет код микроаппа в список прототипов.
     *
     * @param code код микроаппа
     */
    suspend fun addSingleListCode(code: String)

    /**
     * Возвращает коды микроаппов из списка прототипов.
     *
     * @return список кодов
     */
    suspend fun getSingleListCodes(): List<String>

    /** Очищает список прототипов. */
    suspend fun clearSingleListCodes()

    /**
     * Удаляет указанные коды из списка прототипов.
     *
     * @param codes коды микроаппов для удаления
     */
    suspend fun removeCodesFromSingleList(codes: List<String>)
}
