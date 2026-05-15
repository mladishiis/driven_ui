package com.example.drivenui.app.domain

import android.content.Context
import android.util.Log
import com.example.drivenui.app.data.FileRepository
import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.cache.toCachedScreenModel
import com.example.drivenui.engine.generative_screen.mapper.ScreenMapper
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.parser.SDUIParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Оркестратор парсинга микроаппов:
 * выбирает источник, читает файлы, вызывает SDUIParser,
 * маппит в ScreenModel и сохраняет CachedMicroappData.
 *
 * @property fileRepository репозиторий для чтения/записи файлов
 * @property context контекст приложения
 * @property source источник микроаппа (ASSETS или FILE_SYSTEM_JSON)
 * @property fileProvider провайдер JSON-файлов микроаппа
 * @property microappStorage хранилище закэшированных данных
 */
internal class FileInteractorImpl @Inject constructor(
    private val fileRepository: FileRepository,
    private val context: Context,
    private val source: MicroappSource,
    private val fileProvider: MicroappFileProvider,
    private val microappStorage: MicroappStorage
) : FileInteractor {

    private val parser = SDUIParser()
    private var lastParsedResult: SDUIParser.ParsedMicroappResult? = null

    override suspend fun parseMicroapp(): SDUIParser.ParsedMicroappResult =
        withContext(Dispatchers.IO) {
            try {
                val result = parser.parse(
                    microappJson = fileProvider.readMicroapp(),
                    stylesJson = fileProvider.readStyles(),
                    screens = fileProvider.readScreens(),
                )

                lastParsedResult = result
                saveMappedAfterParse(result)
                result

            } catch (e: Exception) {
                Log.e("FileInteractor", "Ошибка парсинга", e)
                throw e
            }
        }

    override suspend fun parseTemplate(): SDUIParser.ParsedMicroappResult =
        withContext(Dispatchers.IO) {
            try {
                val stylesJson = fileProvider.readStyles()
                val screens = fileProvider.readScreens()

                if (screens.isEmpty()) {
                    throw IllegalStateException("Шаблон должен содержать хотя бы один экран в папке screens")
                }

                val result = parser.parse(
                    microappJson = fileProvider.readMicroappOrEmpty(),
                    stylesJson = stylesJson,
                    screens = screens,
                )

                lastParsedResult = result
                saveMappedAfterParse(result)
                result

            } catch (e: Exception) {
                Log.e("FileInteractor", "Ошибка парсинга шаблона", e)
                throw e
            }
        }

    private suspend fun saveMappedAfterParse(result: SDUIParser.ParsedMicroappResult) {
        if (!result.hasData()) return
        val styleRegistry = ComposeStyleRegistry(result.styles)
        val screenMapper = ScreenMapper(styleRegistry)
        val cachedScreens = result.screens.map { screenMapper.mapToScreenModel(it).toCachedScreenModel() }
        val cachedData = CachedMicroappData(
            microappCode = result.microapp?.code?.takeIf { it.isNotBlank() } ?: "template",
            microappTitle = result.microapp?.title ?: "",
            microappDeeplink = result.microapp?.deeplink ?: "",
            allStyles = result.styles,
            screens = cachedScreens,
        )
        microappStorage.saveMapped(cachedData)
    }

    override suspend fun loadCachedMicroapp(microappCode: String): CachedMicroappData? =
        microappStorage.loadMapped(microappCode)

    override fun saveParsedResult(parsedMicroapp: SDUIParser.ParsedMicroappResult) {
        lastParsedResult = parsedMicroapp
    }

    override fun getLastParsedResult(): SDUIParser.ParsedMicroappResult? =
        lastParsedResult

    override fun clearParsedData() {
        lastParsedResult = null
    }

    override suspend fun validateParsingResult(
        result: SDUIParser.ParsedMicroappResult
    ): Boolean = withContext(Dispatchers.IO) {

        if (result.microapp == null && result.screens.isEmpty()) {
            Log.w("FileInteractor", "Пустой результат парсинга")
            return@withContext false
        }

        result.microapp?.let {
            if (it.title.isBlank() || it.code.isBlank()) return@withContext false
        }

        if (result.screens.any { it.screenCode.isBlank() }) return@withContext false

        true
    }

    override fun getResolvedValues(): Map<String, String> =
        lastParsedResult?.getResolvedValues().orEmpty()

    override fun getParsingStats(): Map<String, Any>? =
        lastParsedResult?.getStats()

    override fun getBindingStats(): Map<String, Any>? =
        lastParsedResult?.let { result ->
            mapOf(
                "resolvedBindings" to result.getResolvedValues().size,
                "hasDataContext" to (result.dataContext != null),
            )
        }


    override fun getAvailableJsonFiles(): List<String> =
        fileRepository.getAvailableJsonFiles()
}
