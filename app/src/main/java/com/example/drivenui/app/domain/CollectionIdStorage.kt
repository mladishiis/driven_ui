package com.example.drivenui.app.domain

/**
 * Хранилище ID коллекции прототипов.
 * Используется для синхронизации микроаппов при старте приложения.
 */
interface CollectionIdStorage {

    suspend fun saveCollectionId(id: String)

    suspend fun getCollectionId(): String?

    suspend fun clearCollectionId()
}
