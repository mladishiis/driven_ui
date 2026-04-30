package com.example.drivenui.app.presentation.openFile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivenui.R
import com.example.drivenui.app.domain.MicroappSource
import com.example.drivenui.app.presentation.openFile.model.MicroappItem
import com.example.drivenui.app.presentation.openFile.model.OpenFileEvent
import com.example.drivenui.app.presentation.openFile.model.OpenFileState
import com.example.drivenui.app.theme.DrivenUITheme


/**
 * Экран выбора и загрузки микроаппа.
 *
 * Отображает загрузку, пустое состояние или список микроаппов; поддерживает QR, коллекции, JSON.
 *
 * @param state состояние экрана (загрузка, парсинг, список микроаппов)
 * @param onEvent колбэк для отправки событий
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OpenFileScreen(
    state: OpenFileState,
    onEvent: (OpenFileEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            OpenFileTopBar(
                state = state,
                onEvent = onEvent,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isUploadFile || state.isParsing || state.isSyncingCollection -> {
                    LoadingContent(
                        isLoading = state.isUploadFile || state.isParsing,
                        isSyncing = state.isSyncingCollection,
                        selectedJsonFiles = state.selectedJsonFiles,
                    )
                }
                state.collectionMicroapps.isEmpty() && state.singleMicroapps.isEmpty() -> {
                    EmptyStateContent(
                        state = state,
                        onEvent = onEvent,
                    )
                }
                else -> {
                    ContentWithMicroapps(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpenFileTopBar(
    state: OpenFileState,
    onEvent: (OpenFileEvent) -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.sdui),
                fontWeight = FontWeight.Bold,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        actions = {
            if (state.hasJsonFiles) {
                IconButton(
                    onClick = { onEvent(OpenFileEvent.OnLoadJsonFiles) },
                    enabled = !state.isParsing,
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = stringResource(R.string.load_json_files),
                    )
                }
            }
            if (state.availableJsonFiles.isNotEmpty()) {
                IconButton(
                    onClick = { onEvent(OpenFileEvent.OnSelectJsonFiles(state.selectedJsonFiles)) },
                    enabled = !state.isParsing,
                ) {
                    BadgedBox(
                        badge = {
                            if (state.selectedJsonFiles.isNotEmpty()) {
                                Badge {
                                    Text(
                                        text = state.selectedJsonFiles.size.toString(),
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.json_settings),
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun LoadingContent(
    isLoading: Boolean,
    isSyncing: Boolean,
    selectedJsonFiles: List<String>,
) {
    val statusText = when {
        isSyncing -> stringResource(R.string.syncing_collection)
        isLoading -> stringResource(R.string.parsing_file)
        else -> stringResource(R.string.loading)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (selectedJsonFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.using_json_files),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = selectedJsonFiles.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EmptyStateContent(
    state: OpenFileState,
    onEvent: (OpenFileEvent) -> Unit,
) {
    val buttonsEnabled = !state.isUploadFile && !state.isSyncingCollection
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStateMessage()
        }
        BottomActionsBar(
            uploadButtonText = when (state.microappSource) {
                MicroappSource.ASSETS -> stringResource(R.string.load_from_assets)
                MicroappSource.FILE_SYSTEM,
                MicroappSource.FILE_SYSTEM_JSON -> stringResource(R.string.add_prototype)
            },
            buttonsEnabled = buttonsEnabled,
            onUpload = { onEvent(OpenFileEvent.OnUpload) },
            onAddCollection = { onEvent(OpenFileEvent.OnAddCollection) },
            onUploadTemplate = { onEvent(OpenFileEvent.OnUploadTemplate) },
        )
    }
}

@Composable
private fun EmptyStateMessage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Text(
            text = stringResource(R.string.list_empty),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.scan_qr_for_prototypes),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ContentWithMicroapps(
    state: OpenFileState,
    onEvent: (OpenFileEvent) -> Unit,
) {
    val buttonsEnabled = !state.isUploadFile && !state.isSyncingCollection
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.collectionMicroapps.isNotEmpty()) {
                MicroappSection(
                    title = stringResource(R.string.collection),
                    items = state.collectionMicroapps,
                    showClearButton = !state.isSyncingCollection,
                    onClear = { onEvent(OpenFileEvent.OnClearCollection) },
                    onItemClick = { code -> onEvent(OpenFileEvent.OnShowTestScreen(code)) },
                )
            }
            if (state.singleMicroapps.isNotEmpty()) {
                MicroappSection(
                    title = stringResource(R.string.prototypes_list),
                    items = state.singleMicroapps,
                    showClearButton = !state.isSyncingCollection,
                    onClear = { onEvent(OpenFileEvent.OnClearSingleList) },
                    onItemClick = { code -> onEvent(OpenFileEvent.OnShowTestScreen(code)) },
                )
            }
            if (state.availableJsonFiles.isNotEmpty()) {
                JsonFilesCard(
                    availableCount = state.availableJsonFiles.size,
                    selectedCount = state.selectedJsonFiles.size,
                    onSelectJson = { onEvent(OpenFileEvent.OnSelectJsonFiles(state.selectedJsonFiles)) },
                )
            }
            if (state.microappSource == MicroappSource.ASSETS) {
                state.selectedFileName?.let { fileName ->
                    SelectedFileCard(fileName = fileName)
                }
            }
            state.errorMessage?.let { error ->
                ErrorCard(message = error)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        BottomActionsBar(
            uploadButtonText = when (state.microappSource) {
                MicroappSource.ASSETS -> stringResource(R.string.load_from_assets)
                MicroappSource.FILE_SYSTEM,
                MicroappSource.FILE_SYSTEM_JSON -> stringResource(R.string.add_prototype)
            },
            buttonsEnabled = buttonsEnabled,
            onUpload = { onEvent(OpenFileEvent.OnUpload) },
            onAddCollection = { onEvent(OpenFileEvent.OnAddCollection) },
            onUploadTemplate = { onEvent(OpenFileEvent.OnUploadTemplate) },
        )
    }
}

@Composable
private fun MicroappSection(
    title: String,
    items: List<MicroappItem>,
    showClearButton: Boolean,
    onClear: () -> Unit,
    onItemClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            if (showClearButton) {
                Button(
                    onClick = onClear,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(stringResource(R.string.clear))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                MicroappListItem(
                    item = item,
                    onClick = { onItemClick(item.code) },
                )
            }
        }
    }
}

@Composable
private fun MicroappListItem(
    item: MicroappItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = item.title,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun JsonFilesCard(
    availableCount: Int,
    selectedCount: Int,
    onSelectJson: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.json_files),
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.available_files, availableCount),
                fontSize = 14.sp,
            )
            if (selectedCount > 0) {
                Text(
                    text = stringResource(R.string.selected_files, selectedCount),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSelectJson,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Text(stringResource(R.string.change_json_selection))
                }
            }
        }
    }
}

@Composable
private fun SelectedFileCard(fileName: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.selected_file),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = fileName,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "⚠",
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun BottomActionsBar(
    uploadButtonText: String,
    buttonsEnabled: Boolean,
    onUpload: () -> Unit,
    onAddCollection: () -> Unit,
    onUploadTemplate: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onUpload,
            modifier = Modifier.fillMaxWidth(),
            enabled = buttonsEnabled,
        ) {
            Text(text = uploadButtonText)
        }
        Button(
            onClick = onUploadTemplate,
            modifier = Modifier.fillMaxWidth(),
            enabled = buttonsEnabled,
        ) {
            Text(text = "Загрузить шаблон (скриншоты)")
        }
        Button(
            onClick = onAddCollection,
            modifier = Modifier.fillMaxWidth(),
            enabled = buttonsEnabled,
        ) {
            Text(text = stringResource(R.string.add_collection_prototypes))
        }
    }
}


@Preview(name = "Loading")
@Composable
private fun OpenFileScreenPreviewLoading() {
    DrivenUITheme {
        OpenFileScreen(
            state = OpenFileState(isParsing = true),
            onEvent = {},
        )
    }
}

@Preview(name = "Syncing")
@Composable
private fun OpenFileScreenPreviewSyncing() {
    DrivenUITheme {
        OpenFileScreen(
            state = OpenFileState(isSyncingCollection = true, selectedJsonFiles = listOf("a.json", "b.json")),
            onEvent = {},
        )
    }
}

@Preview(name = "Empty")
@Composable
private fun OpenFileScreenPreviewEmpty() {
    DrivenUITheme {
        OpenFileScreen(
            state = OpenFileState(microappSource = MicroappSource.FILE_SYSTEM_JSON),
            onEvent = {},
        )
    }
}

@Preview(name = "Content — Collection + Single")
@Composable
private fun OpenFileScreenPreviewContent() {
    DrivenUITheme {
        OpenFileScreen(
            state = OpenFileState(
                collectionMicroapps = listOf(
                    MicroappItem("col1", "Коллекционный микроапп 1"),
                    MicroappItem("col2", "Коллекционный микроапп 2"),
                ),
                singleMicroapps = listOf(
                    MicroappItem("single1", "Одиночный микроапп 1"),
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Content — Only Collection")
@Composable
private fun OpenFileScreenPreviewCollectionOnly() {
    DrivenUITheme {
        OpenFileScreen(
            state = OpenFileState(
                collectionMicroapps = listOf(MicroappItem("test", "Тестовый микроапп")),
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Error")
@Composable
private fun OpenFileScreenPreviewError() {
    DrivenUITheme {
        OpenFileScreen(
            state = OpenFileState(
                collectionMicroapps = listOf(MicroappItem("x", "Микроапп")),
                errorMessage = "Не удалось загрузить архив",
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "With JSON files")
@Composable
private fun OpenFileScreenPreviewWithJson() {
    DrivenUITheme {
        OpenFileScreen(
            state = OpenFileState(
                collectionMicroapps = listOf(MicroappItem("a", "Микроапп")),
                availableJsonFiles = listOf("data1.json", "data2.json"),
                selectedJsonFiles = listOf("data1.json"),
            ),
            onEvent = {},
        )
    }
}
