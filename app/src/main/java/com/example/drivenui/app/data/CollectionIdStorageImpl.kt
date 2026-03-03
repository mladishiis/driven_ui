package com.example.drivenui.app.data

import android.content.Context
import androidx.core.content.edit
import com.example.drivenui.app.domain.CollectionIdStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val PREFS_NAME = "collection_prefs"
private const val KEY_COLLECTION_ID = "collection_id"

internal class CollectionIdStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : CollectionIdStorage {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun saveCollectionId(id: String) = withContext(Dispatchers.IO) {
        prefs.edit { putString(KEY_COLLECTION_ID, id) }
    }

    override suspend fun getCollectionId(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_COLLECTION_ID, null)?.takeIf { it.isNotBlank() }
    }

    override suspend fun clearCollectionId() = withContext(Dispatchers.IO) {
        prefs.edit { remove(KEY_COLLECTION_ID) }
    }
}
