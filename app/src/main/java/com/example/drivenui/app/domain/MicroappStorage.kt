package com.example.drivenui.app.domain

import com.example.drivenui.engine.cache.CachedMicroappData

/**
 * Хранилище замапленных микроаппов.
 * Сохраняет результат маппинга (CachedMicroappData) для быстрой загрузки без повторного парсинга и маппинга.
 *
 * Используется для:
 * - Кэширования микроаппов после парсинга и маппинга
 * - Загрузки сохранённых микроаппов (в т.ч. для экрана со списком микроаппов)
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
     * Для экрана со списком микроаппов.
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
}
