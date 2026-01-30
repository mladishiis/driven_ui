package com.example.drivenui.domain

import android.content.Context
import android.util.Log
import com.example.drivenui.data.FileRepository
import com.example.drivenui.parser.SDUIParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Оркестратор:
 * - выбирает источник
 * - читает файлы
 * - вызывает SDUIParser
 * - хранит результат
 */
internal class FileInteractorImpl @Inject constructor(
    private val fileRepository: FileRepository,
    private val context: Context,
    private val source: MicroappSource,
    private val fileProvider: MicroappFileProvider
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
                result

            } catch (e: Exception) {
                Log.e("FileInteractor", "Parsing error", e)
                throw e
            }
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
