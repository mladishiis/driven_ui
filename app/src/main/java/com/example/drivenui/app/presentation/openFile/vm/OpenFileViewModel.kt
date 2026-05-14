package com.example.drivenui.app.presentation.openFile.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.drivenui.R
import com.example.drivenui.app.data.MicroappCollectionApi
import com.example.drivenui.app.navigation.NavigationManager
import com.example.drivenui.app.domain.ArchiveDownloadFormat
import com.example.drivenui.app.domain.FileDownloadInteractor
import com.example.drivenui.app.domain.FileInteractor
import com.example.drivenui.app.domain.MicroappSource
import com.example.drivenui.app.domain.MicroappStorage
import com.example.drivenui.app.domain.toArchiveDownloadFormat
import com.example.drivenui.app.presentation.openFile.model.MicroappItem
import com.example.drivenui.app.presentation.openFile.model.OpenFileEffect
import com.example.drivenui.app.presentation.openFile.model.OpenFileEvent
import com.example.drivenui.app.presentation.openFile.model.OpenFileState
import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.utile.CoreMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel экрана выбора и загрузки микроаппа.
 *
 * @property context контекст приложения
 * @property fileInteractor парсинг и загрузка микроаппов
 * @property fileDownloadInteractor загрузка архивов по URL
 * @property microappStorage хранилище закэшированных микроаппов
 * @property collectionApi API для синхронизации коллекций
 * @property microappSource источник микроаппа (assets или file system)
 * @property contextManager переменные контекста микроаппов в памяти — сбрасываются при удалении кэша
 */
@HiltViewModel
internal class OpenFileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileInteractor: FileInteractor,
    private val fileDownloadInteractor: FileDownloadInteractor,
    private val microappStorage: MicroappStorage,
    private val collectionApi: MicroappCollectionApi,
    private val microappSource: MicroappSource,
    private val contextManager: IContextManager,
) : CoreMviViewModel<OpenFileEvent, OpenFileState, OpenFileEffect>() {

    private var isTemplateMode = false
    private var pendingTemplateUrl: String? = null

    init {
        loadJsonFilesOnInit()
        loadSavedMicroapps()
        syncCollectionOnStart()
    }

    override fun createInitialState() = OpenFileState(
        microappSource = microappSource,
    )

    override fun handleEvent(event: OpenFileEvent) {
        when (event) {
            OpenFileEvent.OnBackClick -> setEffect { OpenFileEffect.GoBack }
            OpenFileEvent.OnUpload -> {
                isTemplateMode = false
                NavigationManager.clearTemplateInfo()
                handleUpload()
            }
            OpenFileEvent.OnUploadTemplate -> {
                isTemplateMode = true
                handleUpload()
            }
            OpenFileEvent.OnClearCollection -> handleClearCollection()
            OpenFileEvent.OnClearSingleList -> handleClearSingleList()
            OpenFileEvent.OnShowFile -> handleShowFile()
            OpenFileEvent.OnShowParsingDetails -> handleShowParsingDetails()
            is OpenFileEvent.OnShowTestScreen -> handleShowTestScreen(event.microappCode)
            OpenFileEvent.OnShowBindingStats -> handleShowBindingStats()
            OpenFileEvent.OnLoadJsonFiles -> handleLoadJsonFiles()
            is OpenFileEvent.OnQrScanned -> handleQrScanned(event.url)
            OpenFileEvent.OnAddCollection -> setEffect { OpenFileEffect.OpenQrScannerForCollection }
            is OpenFileEvent.OnQrScannedCollectionId -> handleQrScannedCollectionId(event.collectionId)
            is OpenFileEvent.OnSelectJsonFiles -> handleSelectJsonFiles(event.files)
        }
    }

    private fun handleUpload() {
        when (microappSource) {
            MicroappSource.ASSETS -> {
                handleUploadFile()
            }

            MicroappSource.FILE_SYSTEM,
            MicroappSource.FILE_SYSTEM_JSON -> {
                setEffect { OpenFileEffect.OpenQrScanner }
            }
        }
    }

    /**
     * Обрабатывает загрузку и парсинг файла (полный микроапп или шаблон).
     * Логика парсинга единая и допускает отсутствие microapp.xml.
     */
    private fun handleUploadFile() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = true,
                            isParsing = true,
                            errorMessage = null,
                            selectedFileName = "microapps",
                        )
                    }
                }

                val parsedResult = fileInteractor.parseTemplate()

                val microappCode = parsedResult.microapp?.code?.takeIf { it.isNotBlank() } ?: "template"
                if (microappSource == MicroappSource.FILE_SYSTEM || microappSource == MicroappSource.FILE_SYSTEM_JSON) {
                    microappStorage.addSingleListCode(microappCode)
                }
                withContext(Dispatchers.Main) {
                    val state = uiState.value
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            parsingResult = parsedResult,
                            errorMessage = null,
                        )
                    }
                    if (isTemplateMode) {
                        val baseUrl = extractBaseUrl(pendingTemplateUrl ?: "")
                        NavigationManager.setTemplateInfo(baseUrl, microappCode)
                        val mappedData = fileInteractor.loadCachedMicroapp(microappCode)
                            ?: microappStorage.loadMapped(microappCode)
                        if (mappedData != null) {
                            setEffect { OpenFileEffect.NavigateToTestScreen(mappedData) }
                        } else {
                            setEffect { OpenFileEffect.ShowParsingErrorDialog("Не удалось загрузить шаблон") }
                        }
                    } else {
                        setEffect {
                            OpenFileEffect.ShowParsingSuccessDialog(
                                microappTitle = parsedResult.microapp?.title ?: context.getString(R.string.unknown_microapp),
                                screensCount = parsedResult.screens.size,
                                textStylesCount = parsedResult.styles?.textStyles?.size ?: 0,
                                colorStylesCount = parsedResult.styles?.colorStyles?.size ?: 0,
                                componentsCount = parsedResult.screens.sumOf { countComponents(it.rootComponent) },
                                hasBindings = state.selectedJsonFiles.isNotEmpty(),
                                jsonFilesCount = state.selectedJsonFiles.size,
                            )
                        }
                    }
                    loadSavedMicroapps()
                }

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при загрузке или парсинге", e)
                withContext(Dispatchers.Main) {
                    updateState {
                        copy(
                            isUploadFile = false,
                            isParsing = false,
                            errorMessage = e.localizedMessage,
                        )
                    }
                    setEffect { OpenFileEffect.ShowParsingErrorDialog(e.localizedMessage ?: context.getString(R.string.unknown_error)) }
                }
            }
        }
    }

    private fun handleQrScanned(url: String) {
        if (!url.startsWith("http")) {
            setEffect { OpenFileEffect.ShowError(context.getString(R.string.qr_invalid_link)) }
            return
        }

        val format = if (isTemplateMode) {
            ArchiveDownloadFormat.JSON
        } else {
            microappSource.toArchiveDownloadFormat()
        }
        if (format == null) {
            setEffect { OpenFileEffect.ShowError(context.getString(R.string.qr_mode_unavailable)) }
            return
        }

        if (isTemplateMode) {
            pendingTemplateUrl = url
        }

        viewModelScope.launch {
            try {
                updateState { copy(isUploadFile = true, isParsing = true, errorMessage = null) }

                val success = withContext(Dispatchers.IO) {
                    fileDownloadInteractor.downloadAndExtractZip(url, format)
                }

                if (!success) {
                    setEffect { OpenFileEffect.ShowParsingErrorDialog(context.getString(R.string.archive_load_failed)) }
                    updateState { copy(isUploadFile = false, isParsing = false) }
                    return@launch
                }
                handleUploadFile()
            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка загрузки по QR", e)
                updateState { copy(isUploadFile = false, isParsing = false) }
                val message = when (e) {
                    is java.net.UnknownHostException -> context.getString(R.string.no_network)
                    is java.net.SocketTimeoutException -> context.getString(R.string.timeout)
                    is java.io.IOException -> context.getString(R.string.network_error, e.message ?: e.localizedMessage ?: "")
                    is IllegalArgumentException -> context.getString(R.string.invalid_data, e.message ?: e.localizedMessage ?: "")
                    else -> context.getString(R.string.load_error, e.message ?: e.localizedMessage ?: "")
                }
                setEffect { OpenFileEffect.ShowParsingErrorDialog(message) }
            }
        }
    }

    private fun loadJsonFilesOnInit() {
        viewModelScope.launch {
            try {
                val jsonFiles = fileInteractor.getAvailableJsonFiles()
                updateState { copy(availableJsonFiles = jsonFiles) }

                val selectedFiles = jsonFiles.take(2)
                if (selectedFiles.isNotEmpty()) {
                    updateState { copy(selectedJsonFiles = selectedFiles) }
                }

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при загрузке JSON файлов", e)
            }
        }
    }

    private fun handleLoadJsonFiles() {
        viewModelScope.launch {
            try {
                val jsonFiles = fileInteractor.getAvailableJsonFiles()
                updateState { copy(availableJsonFiles = jsonFiles) }

                if (jsonFiles.isEmpty()) setEffect { OpenFileEffect.ShowError(context.getString(R.string.json_not_found)) }
                else setEffect { OpenFileEffect.ShowSuccess(context.getString(R.string.json_files_found, jsonFiles.size)) }

            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при загрузке JSON", e)
                setEffect { OpenFileEffect.ShowError(context.getString(R.string.json_load_error)) }
            }
        }
    }

    private fun handleSelectJsonFiles(files: List<String>) {
        updateState { copy(selectedJsonFiles = files) }
        setEffect {
            OpenFileEffect.ShowJsonFileSelectionDialog(
                availableFiles = uiState.value.availableJsonFiles,
                selectedFiles = files,
            )
        }
    }


    private fun handleShowBindingStats() {
        val resolvedValues = fileInteractor.getResolvedValues()
        val bindingStats = fileInteractor.getBindingStats().orEmpty()

        if (resolvedValues.isEmpty() && bindingStats.isEmpty()) {
            setEffect { OpenFileEffect.ShowError(context.getString(R.string.load_bindings_first)) }
            return
        }

        setEffect {
            OpenFileEffect.ShowBindingStats(
                stats = bindingStats,
                resolvedValues = resolvedValues,
            )
        }
    }

    private fun handleShowFile() {
        uiState.value.parsingResult?.let {
            setEffect { OpenFileEffect.NavigateToParsingDetails(it) }
        } ?: setEffect { OpenFileEffect.ShowError(context.getString(R.string.load_file_first)) }
    }

    private fun handleShowParsingDetails() = handleShowFile()
    private fun loadSavedMicroapps() {
        viewModelScope.launch(Dispatchers.IO) {
            val collectionCodes = microappStorage.getCollectionCodes().toSet()
            val singleListCodes = microappStorage.getSingleListCodes().toSet()
            val collectionItems = mutableListOf<MicroappItem>()
            val singleItems = mutableListOf<MicroappItem>()
            collectionCodes.forEach { code ->
                microappStorage.loadMapped(code)?.let { data ->
                    collectionItems.add(
                        MicroappItem(
                            code = data.microappCode,
                            title = data.microappTitle.takeIf { it.isNotBlank() } ?: data.microappCode,
                        )
                    )
                }
            }
            singleListCodes.forEach { code ->
                microappStorage.loadMapped(code)?.let { data ->
                    singleItems.add(
                        MicroappItem(
                            code = data.microappCode,
                            title = data.microappTitle.takeIf { it.isNotBlank() } ?: data.microappCode,
                        )
                    )
                }
            }
            withContext(Dispatchers.Main) {
                updateState {
                    copy(
                        collectionMicroapps = collectionItems,
                        singleMicroapps = singleItems,
                    )
                }
            }
        }
    }

    private fun handleClearCollection() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val collectionCodes = microappStorage.getCollectionCodes().toSet()
                val singleListCodes = microappStorage.getSingleListCodes().toSet()
                collectionCodes.filter { it !in singleListCodes }.forEach { code ->
                    microappStorage.delete(code)
                    contextManager.clearMicroappContext(code)
                }
                microappStorage.clearCollectionId()
                loadSavedMicroapps()
            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при очистке коллекции", e)
            }
        }
    }

    private fun handleClearSingleList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val collectionCodes = microappStorage.getCollectionCodes().toSet()
                val singleListCodes = microappStorage.getSingleListCodes()
                singleListCodes.filter { it !in collectionCodes }.forEach { code ->
                    microappStorage.delete(code)
                    contextManager.clearMicroappContext(code)
                }
                microappStorage.clearSingleListCodes()
                loadSavedMicroapps()
            } catch (e: Exception) {
                Log.e("OpenFileViewModel", "Ошибка при очистке списка прототипов", e)
            }
        }
    }

    private fun handleQrScannedCollectionId(collectionId: String) {
        val id = collectionId.trim().takeIf { it.isNotBlank() }
            ?: run {
                setEffect { OpenFileEffect.ShowError(context.getString(R.string.qr_no_collection_id)) }
                return
            }
        viewModelScope.launch {
            syncCollection(id)
        }
    }

    private fun syncCollectionOnStart() {
        viewModelScope.launch {
            val savedId = microappStorage.getCollectionId() ?: return@launch
            syncCollection(savedId)
        }
    }

    /**
     * Синхронизирует коллекцию: запрашивает список микроаппов, удаляет лишние, скачивает и заменяет все.
     * Версионирования нет, поэтому уже существующие микроаппы тоже перезагружаются.
     */
    private suspend fun syncCollection(collectionId: String) {
        withContext(Dispatchers.Main) {
            updateState {
                copy(
                    isSyncingCollection = true,
                    errorMessage = null,
                )
            }
        }
        try {
            microappStorage.saveCollectionId(collectionId)
            val codesResult = collectionApi.fetchMicroappCodes(collectionId)
            val allCodes = codesResult.getOrElse {
                withContext(Dispatchers.Main) {
                    updateState { copy(isSyncingCollection = false) }
                    setEffect { OpenFileEffect.ShowParsingErrorDialog(it.message ?: context.getString(R.string.load_list_failed)) }
                }
                return
            }
            val serverCodesSet = allCodes.toSet()
            val oldCollectionCodes = microappStorage.getCollectionCodes().toSet()
            val singleListCodes = microappStorage.getSingleListCodes().toSet()
            oldCollectionCodes.filter { it !in serverCodesSet && it !in singleListCodes }.forEach { code ->
                microappStorage.delete(code)
                contextManager.clearMicroappContext(code)
            }
            for (microappCode in allCodes) {
                val url = collectionApi.getMicroappZipUrl(microappCode)
                val success = fileDownloadInteractor.downloadAndExtractZip(url, ArchiveDownloadFormat.OCTET_STREAM)
                if (!success) {
                    Log.e("OpenFileViewModel", "Не удалось загрузить: $microappCode")
                    continue
                }
                try {
                    fileInteractor.parseTemplate()
                } catch (e: Exception) {
                    Log.e("OpenFileViewModel", "Ошибка парсинга $microappCode", e)
                }
            }
            microappStorage.saveCollectionCodes(allCodes)
            withContext(Dispatchers.Main) {
                loadSavedMicroapps()
                updateState { copy(isSyncingCollection = false) }
                if (allCodes.isNotEmpty()) {
                    setEffect { OpenFileEffect.ShowSuccess(context.getString(R.string.synced_prototypes, allCodes.size)) }
                }
            }
        } catch (e: Exception) {
            Log.e("OpenFileViewModel", "Ошибка синхронизации коллекции", e)
            withContext(Dispatchers.Main) {
                updateState { copy(isSyncingCollection = false) }
                setEffect {
                    OpenFileEffect.ShowParsingErrorDialog(
                        e.message ?: e.localizedMessage ?: context.getString(R.string.sync_error),
                    )
                }
            }
        }
    }

    private fun handleShowTestScreen(microappCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val mappedData = fileInteractor.loadCachedMicroapp(microappCode)
                ?: microappStorage.loadMapped(microappCode)
            withContext(Dispatchers.Main) {
                if (mappedData != null) {
                    setEffect { OpenFileEffect.NavigateToTestScreen(mappedData) }
                } else {
                    setEffect { OpenFileEffect.ShowError(context.getString(R.string.microapp_not_found)) }
                }
            }
        }
    }

    private fun countComponents(component: com.example.drivenui.engine.parser.models.Component?): Int {
        if (component == null) return 0
        return 1 + component.children.sumOf { countComponents(it) }
    }

    /**
     * Извлекает базовый URL (scheme + host + port + первый сегмент пути) из URL скачивания.
     *
     * Первый сегмент (`/microapp` или `/template`) определяет контекст API для загрузки скриншотов.
     *
     * Например, для `http://45.8.229.106:8092/template/zip/newgroup/celldoctor`
     * вернёт `http://45.8.229.106:8092/template`.
     */
    private fun extractBaseUrl(url: String): String {
        val uri = java.net.URI(url)
        val port = if (uri.port > 0) ":${uri.port}" else ""
        val firstSegment = uri.path
            ?.trimStart('/')
            ?.substringBefore('/')
            ?.takeIf { it.isNotBlank() }
            ?.let { "/$it" }
            .orEmpty()
        return "${uri.scheme}://${uri.host}$port$firstSegment"
    }
}
