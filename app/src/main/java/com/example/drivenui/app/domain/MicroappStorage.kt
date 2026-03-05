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
     */
    suspend fun getAllCodes(): List<String>

    /**
     * Удаляет сохранённый микроапп.
     */
    suspend fun delete(microappCode: String)

    /**
     * Проверяет наличие сохранённого микроаппа.
     */
    suspend fun contains(microappCode: String): Boolean

    // --- Коллекция (синхронизация с сервером) ---

    suspend fun saveCollectionId(id: String)
    suspend fun getCollectionId(): String?
    suspend fun saveCollectionCodes(codes: List<String>)
    suspend fun getCollectionCodes(): List<String>
    suspend fun clearCollectionId()

    // --- Список прототипов (добавленных по одному) ---

    suspend fun addSingleListCode(code: String)
    suspend fun getSingleListCodes(): List<String>
    suspend fun clearSingleListCodes()
    suspend fun removeCodesFromSingleList(codes: List<String>)
}
