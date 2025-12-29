package com.example.drivenui.presentation.details.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.renderer.ComponentRenderer
import com.example.drivenui.presentation.details.model.DetailsEvent
import com.example.drivenui.presentation.details.model.DetailsState
import com.example.drivenui.presentation.details.model.LayoutItem
import com.example.drivenui.presentation.details.model.WidgetItem
import com.example.drivenui.presentation.details.vm.DetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailsScreen(
    state: DetailsState,
    viewModel: DetailsViewModel,
    onEvent: (DetailsEvent) -> Unit
) {
    val renderModel = viewModel.getComponentModelForRender()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.microappTitle,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(DetailsEvent.OnBackClick) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(DetailsEvent.OnExportData) },
                        enabled = state.hasData && !state.isLoading
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Экспорт")
                    }
                    IconButton(
                        onClick = { onEvent(DetailsEvent.OnRefreshClick) },
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!state.hasData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Ошибка",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Нет данных для отображения",
                        style = MaterialTheme.typography.titleMedium
                    )
                    state.errorMessage?.let { message ->
                        Text(
                            message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Табы
                ScrollableTabRow(
                    selectedTabIndex = state.selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    edgePadding = 0.dp
                ) {
                    state.tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = state.selectedTabIndex == index,
                            onClick = { onEvent(DetailsEvent.OnTabSelected(index)) },
                            text = { Text(title) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Контент в зависимости от выбранной вкладки
                when (state.selectedTabIndex) {
                    0 -> OverviewTab(state, viewModel, onEvent)
                    1 -> ScreensTab(state, viewModel, onEvent)
                    2 -> StylesTab(state, viewModel, onEvent)
                    3 -> QueriesTab(state, viewModel, onEvent)
                    4 -> EventsTab(state, viewModel, onEvent)
                    5 -> WidgetsTab(state, viewModel, onEvent)
                    6 -> LayoutsTab(state, viewModel, onEvent)
                    7 -> DetailsTab(state, viewModel, onEvent) // Новая вкладка для деталей
                    8 -> if (renderModel != null) TestTab(renderModel) // Новая вкладка для деталей
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(
    state: DetailsState,
    viewModel: DetailsViewModel,
    onEvent: (DetailsEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Статистика
        item {
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
                        "Статистика",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard("Экраны", state.screensCount.toString())
                        StatCard("Стили текста", state.textStylesCount.toString())
                        StatCard("Стили цвета", state.colorStylesCount.toString())
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard("Запросы", state.queriesCount.toString())
                        StatCard("События", state.eventsCount.toString())
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Информация о микроаппе
        state.parsedResult?.microapp?.let { microapp ->
            item {
                ExpandableSection(
                    title = "Информация о микроаппе",
                    initiallyExpanded = state.expandedSections.contains("microapp"),
                    onExpandedChange = { expanded ->
                        onEvent(DetailsEvent.OnSectionExpanded("microapp", expanded))
                    }
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow("Название", microapp.title)
                        InfoRow("Код", microapp.code)
                        InfoRow("ShortCode", microapp.shortCode)
                        InfoRow("Deeplink", microapp.deeplink)
                        InfoRow("Persistents", microapp.persistents.size.toString())
                    }
                }
            }
        }

        // Быстрые действия
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Быстрые действия",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            icon = Icons.Filled.ContentCopy,
                            text = "Копировать JSON",
                            onClick = {
                                val json = createExportJson(state.parsedResult!!)
                                onEvent(DetailsEvent.OnCopyToClipboard(json))
                            }
                        )
                        ActionButton(
                            icon = Icons.Filled.Share,
                            text = "Поделиться",
                            onClick = {
                                // TODO: Реализация шеринга
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreensTab(
    state: DetailsState,
    viewModel: DetailsViewModel,
    onEvent: (DetailsEvent) -> Unit
) {
    val screens = viewModel.getScreens()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Экраны (${screens.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (screens.isEmpty()) {
            item {
                Text(
                    "Экраны не найдены",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(screens) { screen ->
                ScreenCard(screen, onEvent)
            }
        }
    }
}

@Composable
private fun StylesTab(
    state: DetailsState,
    viewModel: DetailsViewModel,
    onEvent: (DetailsEvent) -> Unit
) {
    val textStyles = viewModel.getTextStyles()
    val colorStyles = viewModel.getColorStyles()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Стили",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Стили текста
        item {
            ExpandableSection(
                title = "Стили текста (${textStyles.size})",
                initiallyExpanded = state.expandedSections.contains("textStyles"),
                onExpandedChange = { expanded ->
                    onEvent(DetailsEvent.OnSectionExpanded("textStyles", expanded))
                }
            ) {
                if (textStyles.isEmpty()) {
                    Text("Стили текста не найдены")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        textStyles.forEach { style ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        style.code,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text("Шрифт: ${style.fontFamily}")
                                    Text("Размер: ${style.fontSize}sp")
                                    Text("Толщина: ${style.fontWeight}")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Стили цвета
        item {
            ExpandableSection(
                title = "Стили цвета (${colorStyles.size})",
                initiallyExpanded = state.expandedSections.contains("colorStyles"),
                onExpandedChange = { expanded ->
                    onEvent(DetailsEvent.OnSectionExpanded("colorStyles", expanded))
                }
            ) {
                if (colorStyles.isEmpty()) {
                    Text("Стили цвета не найдены")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorStyles.take(10).forEach { style ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                try {
                                                    Color(android.graphics.Color.parseColor(style.lightColor))
                                                } catch (e: Exception) {
                                                    Color.Gray
                                                }
                                            )
                                            .padding(4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(style.code)
                                        Text("Светлая: ${style.lightColor}")
                                        Text("Темная: ${style.darkColor}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueriesTab(
    state: DetailsState,
    viewModel: DetailsViewModel,
    onEvent: (DetailsEvent) -> Unit
) {
    val queries = viewModel.getQueries()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Запросы API (${queries.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (queries.isEmpty()) {
            item {
                Text("Запросы не найдены")
            }
        } else {
            items(queries) { query ->
                QueryCard(query, onEvent)
            }
        }
    }
}

@Composable
private fun EventsTab(
    state: DetailsState,
    viewModel: DetailsViewModel,
    onEvent: (DetailsEvent) -> Unit
) {
    val events = viewModel.getEvents()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "События (${events.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (events.isEmpty()) {
            item {
                Text("События не найдены")
            }
        } else {
            items(events) { event ->
                EventCard(event, onEvent)
            }
        }
    }
}

// Вспомогательные компоненты

@Composable
private fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(title)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value)
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    initiallyExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                        onExpandedChange(expanded)
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть"
                )
            }

            if (expanded) {
                Box(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ScreenCard(
    screen: com.example.drivenui.presentation.details.model.ScreenItem,
    onEvent: (DetailsEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEvent(DetailsEvent.OnCopyToClipboard(screen.code))
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                screen.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Код: ${screen.code}")
            Text("ShortCode: ${screen.shortCode}")
            Text("Deeplink: ${screen.deeplink}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Событий: ${screen.eventsCount}")
                Text("Лэйаутов: ${screen.layoutsCount}")
            }
        }
    }
}

@Composable
private fun QueryCard(
    query: com.example.drivenui.presentation.details.model.QueryItem,
    onEvent: (DetailsEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEvent(DetailsEvent.OnCopyToClipboard(query.endpoint))
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                query.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Код: ${query.code}")
            Text("Метод: ${query.type}")
            Text("Endpoint: ${query.endpoint}")
            Text("Параметров: ${query.propertiesCount}")
        }
    }
}

@Composable
private fun EventCard(
    event: com.example.drivenui.presentation.details.model.EventItem,
    onEvent: (DetailsEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEvent(DetailsEvent.OnCopyToClipboard(event.code))
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Код: ${event.code}")
            Text("Действий: ${event.actionsCount}")
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

/**
 * Создает JSON для экспорта
 */
private fun createExportJson(parsedMicroapp: com.example.drivenui.parser.SDUIParser.ParsedMicroappResult): String {
    return buildString {
        appendLine("{")
        appendLine("  \"microapp\": {")
        parsedMicroapp.microapp?.let {
            appendLine("    \"title\": \"${it.title}\",")
            appendLine("    \"code\": \"${it.code}\",")
            appendLine("    \"shortCode\": \"${it.shortCode}\",")
            appendLine("    \"deeplink\": \"${it.deeplink}\"")
        }
        appendLine("  },")
        appendLine("  \"statistics\": {")
        appendLine("    \"screens\": ${parsedMicroapp.screens.size},")
        appendLine("    \"textStyles\": ${parsedMicroapp.styles?.textStyles?.size ?: 0},")
        appendLine("    \"colorStyles\": ${parsedMicroapp.styles?.colorStyles?.size ?: 0},")
        appendLine("    \"queries\": ${parsedMicroapp.queries.size},")
        appendLine("    \"events\": ${parsedMicroapp.events?.events?.size ?: 0}")
        appendLine("  }")
        appendLine("}")
    }
}

// DetailsScreen.kt - добавляем новые функции

@Composable
private fun WidgetsTab(
    state: DetailsState,
    viewModel: DetailsViewModel,
    onEvent: (DetailsEvent) -> Unit
) {
    val widgets = viewModel.getWidgets()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Виджеты (${widgets.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (widgets.isEmpty()) {
            item {
                Text("Виджеты не найдены")
            }
        } else {
            items(widgets) { widget ->
                WidgetCard(widget, onEvent)
            }
        }
    }
}

@Composable
private fun LayoutsTab(
    state: DetailsState,
    viewModel: DetailsViewModel,
    onEvent: (DetailsEvent) -> Unit
) {
    val layouts = viewModel.getLayouts()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Лэйауты (${layouts.size})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (layouts.isEmpty()) {
            item {
                Text("Лэйауты не найдены")
            }
        } else {
            items(layouts) { layout ->
                LayoutCard(layout, onEvent)
            }
        }
    }
}

@Composable
private fun DetailsTab(
    state: DetailsState,
    viewModel: DetailsViewModel,
    onEvent: (DetailsEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Микроапп
        state.parsedResult?.microapp?.let { microapp ->
            item {
                ExpandableSection(
                    title = "Микроапп (детально)",
                    initiallyExpanded = state.expandedSections.contains("microapp_detail"),
                    onExpandedChange = { expanded ->
                        onEvent(DetailsEvent.OnSectionExpanded("microapp_detail", expanded))
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoRow("Название", microapp.title)
                        InfoRow("Код", microapp.code)
                        InfoRow("Short Code", microapp.shortCode)
                        InfoRow("Deeplink", microapp.deeplink)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Persistents (${microapp.persistents.size}):",
                            fontWeight = FontWeight.Medium
                        )
                        microapp.persistents.forEach { persistent ->
                            Text(
                                "  • $persistent",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Screen Queries
        val screenQueries = viewModel.getScreenQueries()
        item {
            ExpandableSection(
                title = "Экранные запросы (${screenQueries.size})",
                initiallyExpanded = state.expandedSections.contains("screen_queries"),
                onExpandedChange = { expanded ->
                    onEvent(DetailsEvent.OnSectionExpanded("screen_queries", expanded))
                }
            ) {
                if (screenQueries.isEmpty()) {
                    Text("Экранные запросы не найдены")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        screenQueries.forEach { query ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(query.code, fontWeight = FontWeight.Medium)
                                    Text("Экран: ${query.screenCode}")
                                    Text("Запрос: ${query.queryCode}")
                                    Text("Порядок: ${query.order}")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Event Actions
        val eventActions = viewModel.getEventActions()
        item {
            ExpandableSection(
                title = "Действия событий (${eventActions.size})",
                initiallyExpanded = state.expandedSections.contains("event_actions"),
                onExpandedChange = { expanded ->
                    onEvent(DetailsEvent.OnSectionExpanded("event_actions", expanded))
                }
            ) {
                if (eventActions.isEmpty()) {
                    Text("Действия событий не найдены")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        eventActions.forEach { action ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(action.title, fontWeight = FontWeight.Medium)
                                    Text("Код: ${action.code}")
                                    Text("Порядок: ${action.order}")
                                    //Text("Свойств: ${action.properties.size}")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Raw Data
        item {
            ExpandableSection(
                title = "Сырые данные",
                initiallyExpanded = state.expandedSections.contains("raw_data"),
                onExpandedChange = { expanded ->
                    onEvent(DetailsEvent.OnSectionExpanded("raw_data", expanded))
                }
            ) {
                state.parsedResult?.let { data ->
                    val json = createDetailedJson(data)
                    Column {
                        Button(
                            onClick = { onEvent(DetailsEvent.OnCopyToClipboard(json)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Копировать все данные как JSON")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            json.take(500) + if (json.length > 500) "..." else "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetCard(
    widget: WidgetItem,
    onEvent: (DetailsEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEvent(DetailsEvent.OnCopyToClipboard(widget.code))
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                widget.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Код: ${widget.code}")
            Text("Тип: ${widget.type}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Свойств: ${widget.propertiesCount}")
                Text("Стилей: ${widget.stylesCount}")
                Text("Событий: ${widget.eventsCount}")
            }
        }
    }
}

@Composable
private fun TestTab(
    model: ComponentModel
) {
    ComponentRenderer(
        model = model,
        onAction = {})
}

@Composable
private fun LayoutCard(
    layout: LayoutItem,
    onEvent: (DetailsEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEvent(DetailsEvent.OnCopyToClipboard(layout.code))
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                layout.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Код: ${layout.code}")
            Text("Свойств: ${layout.propertiesCount}")
        }
    }
}

/**
 * Создает детализированный JSON с полными данными о микроаппе
 */
private fun createDetailedJson(parsedMicroapp: com.example.drivenui.parser.SDUIParser.ParsedMicroappResult): String {
    return buildString {
        appendLine("{")

        // Microapp
        parsedMicroapp.microapp?.let { microapp ->
            appendLine("  \"microapp\": {")
            appendLine("    \"title\": \"${escapeJson(microapp.title)}\",")
            appendLine("    \"code\": \"${escapeJson(microapp.code)}\",")
            appendLine("    \"shortCode\": \"${escapeJson(microapp.shortCode)}\",")
            appendLine("    \"deeplink\": \"${escapeJson(microapp.deeplink)}\",")
            appendLine("    \"persistents\": [")
            microapp.persistents.forEachIndexed { index, persistent ->
                append("      \"${escapeJson(persistent)}\"")
                if (index < microapp.persistents.size - 1) append(",")
                appendLine()
            }
            appendLine("    ]")
            appendLine("  },")
        } ?: appendLine("  \"microapp\": null,")

        // Screens
        appendLine("  \"screens\": [")
        parsedMicroapp.screens.forEachIndexed { screenIndex, screen ->
            appendLine("    {")
            appendLine("      \"title\": \"${escapeJson(screen.title)}\",")
            appendLine("      \"code\": \"${escapeJson(screen.screenCode)}\",")
            appendLine("      \"shortCode\": \"${escapeJson(screen.screenShortCode)}\",")
            appendLine("      \"deeplink\": \"${escapeJson(screen.deeplink)}\"")
            append("    }")
            if (screenIndex < parsedMicroapp.screens.size - 1) append(",")
            appendLine()
        }
        appendLine("  ],")

        // Text Styles
        appendLine("  \"textStyles\": [")
        parsedMicroapp.styles?.textStyles?.forEachIndexed { styleIndex, style ->
            appendLine("    {")
            appendLine("      \"code\": \"${escapeJson(style.code)}\",")
            appendLine("      \"fontFamily\": \"${escapeJson(style.fontFamily ?: "")}\",")
            appendLine("      \"fontSize\": \"${escapeJson(style.fontSize?.toString() ?: "")}\",")
            appendLine("      \"fontWeight\": \"${escapeJson(style.fontWeight?.toString() ?: "")}\"")
            append("    }")
            if (styleIndex < (parsedMicroapp.styles.textStyles?.size ?: 0) - 1) append(",")
            appendLine()
        }
        appendLine("  ],")

        // Color Styles
        appendLine("  \"colorStyles\": [")
        parsedMicroapp.styles?.colorStyles?.forEachIndexed { colorIndex, colorStyle ->
            appendLine("    {")
            appendLine("      \"code\": \"${escapeJson(colorStyle.code)}\",")
            appendLine("      \"lightColor\": \"${escapeJson(colorStyle.lightTheme?.color ?: "")}\",")
            appendLine("      \"darkColor\": \"${escapeJson(colorStyle.darkTheme?.color ?: "")}\"")
            append("    }")
            if (colorIndex < (parsedMicroapp.styles.colorStyles?.size ?: 0) - 1) append(",")
            appendLine()
        }
        appendLine("  ],")

        // Queries
        appendLine("  \"queries\": [")
        parsedMicroapp.queries.forEachIndexed { queryIndex, query ->
            appendLine("    {")
            appendLine("      \"title\": \"${escapeJson(query.title)}\",")
            appendLine("      \"code\": \"${escapeJson(query.code)}\",")
            appendLine("      \"type\": \"${escapeJson(query.type)}\",")
            appendLine("      \"endpoint\": \"${escapeJson(query.endpoint)}\",")
            appendLine("      \"propertiesCount\": ${query.properties.size}")
            append("    }")
            if (queryIndex < parsedMicroapp.queries.size - 1) append(",")
            appendLine()
        }
        appendLine("  ],")

        // Events (с деталями действий и свойств)
        appendLine("  \"events\": [")
        parsedMicroapp.events?.events?.forEachIndexed { eventIndex, event ->
            appendLine("    {")
            appendLine("      \"title\": \"${escapeJson(event.title)}\",")
            appendLine("      \"code\": \"${escapeJson(event.code)}\",")
            appendLine("      \"order\": ${event.order},")
            appendLine("      \"actions\": [")
            event.eventActions.forEachIndexed { actionIndex, action ->
                appendLine("        {")
                appendLine("          \"title\": \"${escapeJson(action.title)}\",")
                appendLine("          \"code\": \"${escapeJson(action.code)}\",")
                appendLine("          \"order\": ${action.order},")
                // Для EventAction.properties используется Map<String, String>
                appendLine("          \"properties\": {")
                val actionProps = action.properties.entries.toList()
                actionProps.forEachIndexed { propIndex, entry ->
                    append("            \"${escapeJson(entry.key)}\": \"${escapeJson(entry.value)}\"")
                    if (propIndex < actionProps.size - 1) append(",")
                    appendLine()
                }
                appendLine("          }")
                append("        }")
                if (actionIndex < event.eventActions.size - 1) append(",")
                appendLine()
            }
            appendLine("      ]")
            append("    }")
            if (eventIndex < (parsedMicroapp.events?.events?.size ?: 0) - 1) append(",")
            appendLine()
        }
        appendLine("  ],")

        // Widgets
        try {
            val widgetsField = parsedMicroapp::class.java.getDeclaredField("widgets")
            widgetsField.isAccessible = true
            val widgets = widgetsField.get(parsedMicroapp) as? List<*> ?: emptyList<Any>()

            appendLine("  \"widgets\": [")
            widgets.forEachIndexed { widgetIndex, widget ->
                if (widget is com.example.drivenui.parser.models.Widget) {
                    appendLine("    {")
                    appendLine("      \"title\": \"${escapeJson(widget.title)}\",")
                    appendLine("      \"code\": \"${escapeJson(widget.code)}\",")
                    appendLine("      \"type\": \"${escapeJson(widget.type)}\",")
                    // Для Widget.properties используется Map<String, String>
                    appendLine("      \"properties\": {")
                    val widgetProps = widget.properties.entries.toList()
                    widgetProps.forEachIndexed { propIndex, entry ->
                        append("        \"${escapeJson(entry.key)}\": \"${escapeJson(entry.value)}\"")
                        if (propIndex < widgetProps.size - 1) append(",")
                        appendLine()
                    }
                    appendLine("      },")
                    appendLine("      \"stylesCount\": ${widget.styles.size},")
                    appendLine("      \"eventsCount\": ${widget.events.size}")
                    append("    }")
                    if (widgetIndex < widgets.size - 1) append(",")
                    appendLine()
                }
            }
            appendLine("  ],")
        } catch (e: Exception) {
            appendLine("  \"widgets\": [],")
        }

        // Layouts
        try {
            val layoutsField = parsedMicroapp::class.java.getDeclaredField("layouts")
            layoutsField.isAccessible = true
            val layouts = layoutsField.get(parsedMicroapp) as? List<*> ?: emptyList<Any>()

            appendLine("  \"layouts\": [")
            layouts.forEachIndexed { layoutIndex, layout ->
                if (layout is com.example.drivenui.parser.models.Layout) {
                    appendLine("    {")
                    appendLine("      \"title\": \"${escapeJson(layout.title)}\",")
                    appendLine("      \"code\": \"${escapeJson(layout.code)}\",")
                    // Для Layout.properties используется Map<String, String>
                    appendLine("      \"properties\": {")
                    val layoutProps = layout.properties.entries.toList()
                    layoutProps.forEachIndexed { propIndex, entry ->
                        append("        \"${escapeJson(entry.key)}\": \"${escapeJson(entry.value)}\"")
                        if (propIndex < layoutProps.size - 1) append(",")
                        appendLine()
                    }
                    appendLine("      }")
                    append("    }")
                    if (layoutIndex < layouts.size - 1) append(",")
                    appendLine()
                }
            }
            appendLine("  ],")
        } catch (e: Exception) {
            appendLine("  \"layouts\": [],")
        }

        // Screen Queries
        try {
            val screenQueriesField = parsedMicroapp::class.java.getDeclaredField("screenQueries")
            screenQueriesField.isAccessible = true
            val screenQueries =
                screenQueriesField.get(parsedMicroapp) as? List<*> ?: emptyList<Any>()

            appendLine("  \"screenQueries\": [")
            screenQueries.forEachIndexed { sqIndex, sq ->
                if (sq is com.example.drivenui.parser.models.ScreenQuery) {
                    appendLine("    {")
                    appendLine("      \"code\": \"${escapeJson(sq.code)}\",")
                    appendLine("      \"screenCode\": \"${escapeJson(sq.screenCode)}\",")
                    appendLine("      \"queryCode\": \"${escapeJson(sq.queryCode)}\",")
                    appendLine("      \"order\": ${sq.order},")
                    // Для ScreenQuery.properties используется Map<String, String>
                    appendLine("      \"properties\": {")
                    val screenQueryProps = sq.properties.entries.toList()
                    screenQueryProps.forEachIndexed { propIndex, entry ->
                        append("        \"${escapeJson(entry.key)}\": \"${escapeJson(entry.value)}\"")
                        if (propIndex < screenQueryProps.size - 1) append(",")
                        appendLine()
                    }
                    appendLine("      }")
                    append("    }")
                    if (sqIndex < screenQueries.size - 1) append(",")
                    appendLine()
                }
            }
            appendLine("  ],")
        } catch (e: Exception) {
            appendLine("  \"screenQueries\": [],")
        }

        // Event Actions (все действия событий микроаппа)
        try {
            val allEventActionsField =
                parsedMicroapp::class.java.getDeclaredField("allEventActions")
            allEventActionsField.isAccessible = true
            val allEventActions =
                allEventActionsField.get(parsedMicroapp) as? com.example.drivenui.parser.models.AllEventActions

            appendLine("  \"allEventActions\": [")
            allEventActions?.eventActions?.forEachIndexed { eaIndex, action ->
                appendLine("    {")
                appendLine("      \"title\": \"${escapeJson(action.title)}\",")
                appendLine("      \"code\": \"${escapeJson(action.code)}\",")
                appendLine("      \"order\": ${action.order},")
                // Для EventAction.properties используется Map<String, String>
                appendLine("      \"properties\": {")
                val actionProps = action.properties.entries.toList()
                actionProps.forEachIndexed { propIndex, entry ->
                    append("        \"${escapeJson(entry.key)}\": \"${escapeJson(entry.value)}\"")
                    if (propIndex < actionProps.size - 1) append(",")
                    appendLine()
                }
                appendLine("      }")
                append("    }")
                if (eaIndex < (allEventActions.eventActions.size) - 1) append(",")
                appendLine()
            }
            appendLine("  ]")
        } catch (e: Exception) {
            // Если нет поля allEventActions, попробуем получить eventActions
            try {
                val eventActionsField = parsedMicroapp::class.java.getDeclaredField("eventActions")
                eventActionsField.isAccessible = true
                val eventActions =
                    eventActionsField.get(parsedMicroapp) as? List<com.example.drivenui.parser.models.EventAction>

                appendLine("  \"eventActions\": [")
                eventActions?.forEachIndexed { eaIndex, action ->
                    appendLine("    {")
                    appendLine("      \"title\": \"${escapeJson(action.title)}\",")
                    appendLine("      \"code\": \"${escapeJson(action.code)}\",")
                    appendLine("      \"order\": ${action.order},")
                    // Для EventAction.properties используется Map<String, String>
                    appendLine("      \"properties\": {")
                    val actionProps = action.properties.entries.toList()
                    actionProps.forEachIndexed { propIndex, entry ->
                        append("        \"${escapeJson(entry.key)}\": \"${escapeJson(entry.value)}\"")
                        if (propIndex < actionProps.size - 1) append(",")
                        appendLine()
                    }
                    appendLine("      }")
                    append("    }")
                    if (eaIndex < (eventActions.size) - 1) append(",")
                    appendLine()
                }
                appendLine("  ]")
            } catch (e2: Exception) {
                appendLine("  \"eventActions\": []")
            }
        }

        appendLine("}")
    }
}

/**
 * Экранирует специальные символы для JSON
 */
private fun escapeJson(value: String): String {
    return value.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
        .replace("\b", "\\b")
        .replace("\u000C", "\\f")
}