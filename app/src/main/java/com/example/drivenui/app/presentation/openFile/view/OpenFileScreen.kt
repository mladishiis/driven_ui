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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivenui.app.domain.MicroappSource
import com.example.drivenui.app.presentation.openFile.model.MicroappItem
import com.example.drivenui.app.presentation.openFile.model.OpenFileEvent
import com.example.drivenui.app.presentation.openFile.model.OpenFileState

@Composable
private fun MicroappListItem(
    item: MicroappItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BottomActionsBar(
    state: OpenFileState,
    onEvent: (OpenFileEvent) -> Unit,
) {
    val buttonsEnabled = !state.isUploadFile && !state.isSyncingCollection
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onEvent(OpenFileEvent.OnUpload) },
            modifier = Modifier.fillMaxWidth(),
            enabled = buttonsEnabled
        ) {
            Text(
                text = when (state.microappSource) {
                    MicroappSource.ASSETS -> "Загрузить из assets"
                    MicroappSource.FILE_SYSTEM,
                    MicroappSource.FILE_SYSTEM_JSON -> "Добавить прототип"
                }
            )
        }
        Button(
            onClick = { onEvent(OpenFileEvent.OnAddCollection) },
            modifier = Modifier.fillMaxWidth(),
            enabled = buttonsEnabled
        ) {
            Text(text = "Добавить коллекцию прототипов")
        }
        if (state.savedMicroapps.isNotEmpty() && !state.isSyncingCollection) {
            Button(
                onClick = { onEvent(OpenFileEvent.OnClearSavedMicroapps) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Очистить список")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OpenFileScreen(
    state: OpenFileState,
    onEvent: (OpenFileEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SDUI",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    if (state.hasJsonFiles) {
                        IconButton(
                            onClick = { onEvent(OpenFileEvent.OnLoadJsonFiles) },
                            enabled = !state.isParsing
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Загрузить JSON файлы"
                            )
                        }
                    }
                    if (state.availableJsonFiles.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onEvent(OpenFileEvent.OnSelectJsonFiles(state.selectedJsonFiles))
                            },
                            enabled = !state.isParsing
                        ) {
                            BadgedBox(
                                badge = {
                                    if (state.selectedJsonFiles.isNotEmpty()) {
                                        Badge {
                                            Text(
                                                text = state.selectedJsonFiles.size.toString(),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Настройки JSON"
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (state.isUploadFile || state.isParsing || state.isSyncingCollection) {
                // Показываем индикатор загрузки
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when {
                            state.isSyncingCollection -> "Синхронизация коллекции..."
                            state.isParsing -> "Парсинг файла..."
                            else -> "Загрузка..."
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (state.selectedJsonFiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Используются JSON файлы:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.selectedJsonFiles.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else if (state.savedMicroapps.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Список пуст",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Сканируйте QR-коды на Портале для\u00a0добавления прототипов",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    BottomActionsBar(state = state, onEvent = onEvent)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Список прототипов
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Список прототипов",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.savedMicroapps.forEach { item ->
                                    MicroappListItem(
                                        item = item,
                                        onClick = { onEvent(OpenFileEvent.OnShowTestScreen(item.code)) }
                                    )
                                }
                            }
                        }

                        // Информация о JSON файлах
                        if (state.availableJsonFiles.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "JSON файлы",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Доступно: ${state.availableJsonFiles.size} файлов",
                                        fontSize = 14.sp
                                    )
                                    if (state.selectedJsonFiles.isNotEmpty()) {
                                        Text(
                                            text = "Выбрано: ${state.selectedJsonFiles.size} файлов",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                onEvent(OpenFileEvent.OnSelectJsonFiles(state.selectedJsonFiles))
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        ) {
                                            Text("Изменить выбор JSON")
                                        }
                                    }
                                }
                            }
                        }

                        // Информация о выбранном файле
                        if (state.microappSource == MicroappSource.ASSETS) {
                            state.selectedFileName?.let { fileName ->
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Выбранный файл:",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = fileName,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Сообщение об ошибке
                        state.errorMessage?.let { error ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚠",
                                        fontSize = 24.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    BottomActionsBar(state = state, onEvent = onEvent)
                }
            }
        }
    }
}