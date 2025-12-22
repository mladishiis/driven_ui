package com.example.drivenui.presentation.openFile.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drivenui.presentation.openFile.model.OpenFileEvent
import com.example.drivenui.presentation.openFile.model.OpenFileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OpenFileScreen(
    state: OpenFileState,
    onUploadFile: (OpenFileEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SDUI Парсер",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (state.isUploadFile || state.isParsing) {
                // Показываем индикатор загрузки
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (state.isParsing) "Парсинг файла..." else "Загрузка...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Заголовок
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Сервер-driven UI (SDUI)",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Парсер XML микроаппов Driven UI",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Информация о выбранном файле
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

                    // Кнопка загрузки файла
                    Button(
                        onClick = { onUploadFile(OpenFileEvent.OnUploadFile) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isUploadFile
                    ) {
                        Text(
                            text = if (state.hasParsingResult) "Повторно загрузить файл" else "Загрузить и спарсить файл",
                            fontSize = 16.sp
                        )
                    }

                    // Кнопка показа результата
                    Button(
                        onClick = { onUploadFile(OpenFileEvent.OnShowFile) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.hasParsingResult,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = "Показать результат парсинга",
                            fontSize = 16.sp
                        )
                    }

                    // Кнопка деталей парсинга
                    if (state.hasParsingResult) {
                        OutlinedButton(
                            onClick = { onUploadFile(OpenFileEvent.OnShowParsingDetails) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Детали парсинга")
                        }
                    }

                    // Статус парсинга
                    if (state.hasParsingResult) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "✓ Файл успешно спарсен",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Микроапп: ${state.microappTitle}")
                                Text("Экранов: ${state.screensCount}")
                                Text("Стилей текста: ${state.textStylesCount}")
                                Text("Стилей цвета: ${state.colorStylesCount}")
                                Text("Запросов API: ${state.queriesCount}")
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

                    // Информация
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Информация:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Парсер поддерживает XML формат Driven UI")
                            Text("• Автоматически ищет файлы в папке assets")
                            Text("• Показывает структуру микроаппа")
                            Text("• Экспортирует результат парсинга")
                        }
                    }
                }
            }
        }
    }
}