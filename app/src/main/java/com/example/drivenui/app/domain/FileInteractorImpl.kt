package com.example.drivenui.app.domain

import android.content.Context
import android.util.Log
import com.example.drivenui.app.data.FileRepository
import com.example.drivenui.engine.cache.CachedMicroappData
import com.example.drivenui.engine.cache.toCachedScreenModel
import com.example.drivenui.app.domain.MicroappStorage
import com.example.drivenui.engine.generative_screen.mapper.ScreenMapper
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.parser.SDUIParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Оркестратор:
 * - выбирает источник
 * - читает файлы
 * - вызывает SDUIParser
 * - маппит в ScreenModel и сохраняет CachedMicroappData
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
                Log.d("FileInteractor", "Start parsing, source=$source")

                val result = parser.parse(
                    microappXml = fileProvider.readMicroapp(),
                    stylesXml = fileProvider.readStyles(),
                    queriesXml = fileProvider.readQueries(),
                    screens = fileProvider.readScreens()
                )

                lastParsedResult = result
                saveMappedAfterParse(result)
                result

            } catch (e: Exception) {
                Log.e("FileInteractor", "Parsing error", e)
                throw e
            }
        }

    override suspend fun parseTemplate(): SDUIParser.ParsedMicroappResult =
        withContext(Dispatchers.IO) {
            try {
                Log.d("FileInteractor", "Start template parsing, source=$source")

                val stylesXml = fileProvider.readStyles()
                val screens = fileProvider.readScreens()

                if (screens.isEmpty()) {
                    throw IllegalStateException("Шаблон должен содержать хотя бы один экран в папке screens")
                }

                val result = parser.parse(
                    microappXml = fileProvider.readMicroappOrEmpty(),
                    stylesXml = stylesXml,
                    queriesXml = fileProvider.readQueriesOrEmpty(),
                    screens = screens
                )

                lastParsedResult = result
                Log.d("FileInteractor", "Template parsed: ${result.screens.size} screen(s)")
                saveMappedAfterParse(result)
                result

            } catch (e: Exception) {
                Log.e("FileInteractor", "Template parsing error", e)
                throw e
            }
        }

    private fun saveMappedAfterParse(result: SDUIParser.ParsedMicroappResult) {
        if (!result.hasData()) return
        val styleRegistry = ComposeStyleRegistry(result.styles)
        val screenMapper = ScreenMapper(styleRegistry)
        val cachedScreens = result.screens.map { screenMapper.mapToScreenModel(it).toCachedScreenModel() }
        val cachedData = CachedMicroappData(
            microappCode = result.microapp?.code?.takeIf { it.isNotBlank() } ?: "template",
            microappTitle = result.microapp?.title ?: "",
            allStyles = result.styles,
            screens = cachedScreens
        )
        microappStorage.saveMapped(cachedData)
    }

    override suspend fun loadCachedMicroapp(microappCode: String): CachedMicroappData? =
        microappStorage.loadMapped(microappCode)?.also {
            Log.d("FileInteractor", "Loaded cached microapp: $microappCode")
        }

    override fun saveParsedResult(parsedMicroapp: SDUIParser.ParsedMicroappResult) {
        lastParsedResult = parsedMicroapp
        Log.d("FileInteractor", "Parsed result saved")
    }

    override fun getLastParsedResult(): SDUIParser.ParsedMicroappResult? =
        lastParsedResult

    override fun clearParsedData() {
        lastParsedResult = null
        Log.d("FileInteractor", "Parsed data cleared")
    }

    override suspend fun validateParsingResult(
        result: SDUIParser.ParsedMicroappResult
    ): Boolean = withContext(Dispatchers.IO) {

        if (result.microapp == null && result.screens.isEmpty()) {
            Log.w("FileInteractor", "Empty parsing result")
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
                "screenQueries" to result.screenQueries.size,
                "hasDataContext" to (result.dataContext != null)
            )
        }


    override fun getAvailableJsonFiles(): List<String> =
        fileRepository.getAvailableJsonFiles()
}
