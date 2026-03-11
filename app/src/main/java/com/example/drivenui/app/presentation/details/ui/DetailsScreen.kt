package com.example.drivenui.app.presentation.details.view

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.drivenui.R
import com.example.drivenui.app.presentation.details.model.DetailsEvent
import com.example.drivenui.app.presentation.details.model.DetailsState
import com.example.drivenui.app.presentation.details.model.DetailsTabData
import com.example.drivenui.app.presentation.details.model.EventItem
import com.example.drivenui.app.presentation.details.model.LayoutItem
import com.example.drivenui.app.presentation.details.model.QueryItem
import com.example.drivenui.app.presentation.details.model.ScreenItem
import com.example.drivenui.app.presentation.details.model.WidgetItem
import com.example.drivenui.app.theme.DrivenUITheme

/**
 * Экран деталей парсинга микроаппа.
 *
 * Отображает вкладки с обзором, экранами, стилями, запросами, событиями, виджетами и лейаутами.
 * Поддерживает экспорт данных в JSON.
 *
 * @param state состояние экрана (результат парсинга, вкладки, загрузка)
 * @param onEvent колбэк для отправки событий (назад, обновить, экспорт и т.д.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailsScreen(
    state: DetailsState,
    onEvent: (DetailsEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.microappTitle.ifEmpty { stringResource(R.string.parsing_details) },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(DetailsEvent.OnBackClick) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(DetailsEvent.OnExportData) },
                        enabled = state.hasData && !state.isLoading
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = stringResource(R.string.export))
                    }
                    IconButton(
                        onClick = { onEvent(DetailsEvent.OnRefreshClick) },
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.refresh))
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
                        contentDescription = stringResource(R.string.error),
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        stringResource(R.string.no_data_to_display),
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
                ScrollableTabRow(
                    selectedTabIndex = state.selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    edgePadding = 0.dp
                ) {
                    state.tabResourceIds.forEachIndexed { index, resId ->
                        Tab(
                            selected = state.selectedTabIndex == index,
                            onClick = { onEvent(DetailsEvent.OnTabSelected(index)) },
                            text = { Text(stringResource(resId)) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                when (state.selectedTabIndex) {
                    0 -> OverviewTab(state = state, onEvent = onEvent)
                    1 -> ScreensTab(tabData = state.tabData, state = state, onEvent = onEvent)
                    2 -> StylesTab(tabData = state.tabData, state = state, onEvent = onEvent)
                    3 -> QueriesTab(tabData = state.tabData, state = state, onEvent = onEvent)
                    4 -> EventsTab(tabData = state.tabData, state = state, onEvent = onEvent)
                    5 -> WidgetsTab(tabData = state.tabData, state = state, onEvent = onEvent)
                    6 -> LayoutsTab(tabData = state.tabData, state = state, onEvent = onEvent)
                    7 -> DetailsTab(state = state, onEvent = onEvent)
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(
    state: DetailsState,
    onEvent: (DetailsEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        stringResource(R.string.statistics),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(stringResource(R.string.screens), state.screensCount.toString())
                        StatCard(stringResource(R.string.text_styles), state.textStylesCount.toString())
                        StatCard(stringResource(R.string.color_styles), state.colorStylesCount.toString())
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(stringResource(R.string.queries), state.queriesCount.toString())
                        StatCard(stringResource(R.string.events), state.eventsCount.toString())
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        state.parsedResult?.microapp?.let { microapp ->
            item {
                ExpandableSection(
                    title = stringResource(R.string.microapp_info),
                    initiallyExpanded = state.expandedSections.contains("microapp"),
                    onExpandedChange = { expanded ->
                        onEvent(DetailsEvent.OnSectionExpanded("microapp", expanded))
                    }
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow(stringResource(R.string.name), microapp.title)
                        InfoRow(stringResource(R.string.code), microapp.code)
                        InfoRow(stringResource(R.string.short_code), microapp.shortCode)
                        InfoRow(stringResource(R.string.deeplink), microapp.deeplink)
                        InfoRow(stringResource(R.string.persistents), microapp.persistents.size.toString())
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.quick_actions),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            icon = Icons.Filled.ContentCopy,
                            text = stringResource(R.string.copy_json),
                            onClick = {
                                val json = createExportJson(state.parsedResult!!)
                                onEvent(DetailsEvent.OnCopyToClipboard(json))
                            }
                        )
                        ActionButton(
                            icon = Icons.Filled.Share,
                            text = stringResource(R.string.share),
                            onClick = {
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
    tabData: DetailsTabData,
    state: DetailsState,
    onEvent: (DetailsEvent) -> Unit,
) {
    val screens = tabData.screens

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.screens_count, screens.size),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (screens.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.screens_not_found),
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
    tabData: DetailsTabData,
    state: DetailsState,
    onEvent: (DetailsEvent) -> Unit,
) {
    val textStyles = tabData.textStyles
    val colorStyles = tabData.colorStyles

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

        item {
            ExpandableSection(
                title = "Стили текста (${textStyles.size})",
                initiallyExpanded = state.expandedSections.contains("textStyles"),
                onExpandedChange = { expanded ->
                    onEvent(DetailsEvent.OnSectionExpanded("textStyles", expanded))
                }
            ) {
                if (textStyles.isEmpty()) {
                    Text(stringResource(R.string.text_styles_not_found))
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
                                    Text(stringResource(R.string.font, style.fontFamily))
                                    Text(stringResource(R.string.size, style.fontSize))
                                    Text(stringResource(R.string.weight, style.fontWeight))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            ExpandableSection(
                title = stringResource(R.string.color_styles_count, colorStyles.size),
                initiallyExpanded = state.expandedSections.contains("colorStyles"),
                onExpandedChange = { expanded ->
                    onEvent(DetailsEvent.OnSectionExpanded("colorStyles", expanded))
                }
            ) {
                if (colorStyles.isEmpty()) {
                    Text(stringResource(R.string.color_styles_not_found))
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
                                        Text(stringResource(R.string.light, style.lightColor))
                                        Text(stringResource(R.string.dark, style.darkColor))
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
    tabData: DetailsTabData,
    state: DetailsState,
    onEvent: (DetailsEvent) -> Unit,
) {
    val queries = tabData.queries

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.queries_api_count, queries.size),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (queries.isEmpty()) {
            item {
                Text(stringResource(R.string.queries_not_found))
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
    tabData: DetailsTabData,
    state: DetailsState,
    onEvent: (DetailsEvent) -> Unit,
) {
    val events = tabData.events

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.events_count, events.size),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (events.isEmpty()) {
            item {
                Text(stringResource(R.string.events_not_found))
            }
        } else {
            items(events) { event ->
                EventCard(event, onEvent)
            }
        }
    }
}


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
    screen: ScreenItem,
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
            Text(stringResource(R.string.code_label, screen.code))
            Text(stringResource(R.string.short_code_label, screen.shortCode))
            Text(stringResource(R.string.deeplink_label, screen.deeplink))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.events_count_label, screen.eventsCount))
                Text(stringResource(R.string.layouts_count_label, screen.layoutsCount))
            }
        }
    }
}

@Composable
private fun QueryCard(
    query: QueryItem,
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
            Text(stringResource(R.string.code_label, query.code))
            Text(stringResource(R.string.method, query.type))
            Text(stringResource(R.string.endpoint, query.endpoint))
            Text(stringResource(R.string.params_count, query.propertiesCount))
        }
    }
}

@Composable
private fun EventCard(
    event: EventItem,
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
            Text(stringResource(R.string.code_label, event.code))
            Text(stringResource(R.string.actions_count, event.actionsCount))
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
 * Создаёт JSON для экспорта.
 *
 * @param parsedMicroapp Результат парсинга микроаппа
 * @return JSON-строка
 */
private fun createExportJson(parsedMicroapp: com.example.drivenui.engine.parser.SDUIParser.ParsedMicroappResult): String {
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


@Composable
private fun WidgetsTab(
    tabData: DetailsTabData,
    state: DetailsState,
    onEvent: (DetailsEvent) -> Unit,
) {
    val widgets = tabData.widgets

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
                Text(stringResource(R.string.widgets_not_found))
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
    tabData: DetailsTabData,
    state: DetailsState,
    onEvent: (DetailsEvent) -> Unit,
) {
    val layouts = tabData.layouts

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
                Text(stringResource(R.string.layouts_not_found))
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
    onEvent: (DetailsEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        val screenQueries = state.tabData.screenQueries
        item {
            ExpandableSection(
                title = "Экранные запросы (${screenQueries.size})",
                initiallyExpanded = state.expandedSections.contains("screen_queries"),
                onExpandedChange = { expanded ->
                    onEvent(DetailsEvent.OnSectionExpanded("screen_queries", expanded))
                }
            ) {
                if (screenQueries.isEmpty()) {
                    Text(stringResource(R.string.screen_queries_not_found))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        screenQueries.forEach { query ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(query.code, fontWeight = FontWeight.Medium)
                                    Text(stringResource(R.string.screen_label, query.screenCode))
                                    Text(stringResource(R.string.query_label, query.queryCode))
                                    Text(stringResource(R.string.order_label, query.order))
                                }
                            }
                        }
                    }
                }
            }
        }

        val eventActions = state.tabData.eventActions
        item {
            ExpandableSection(
                title = "Действия событий (${eventActions.size})",
                initiallyExpanded = state.expandedSections.contains("event_actions"),
                onExpandedChange = { expanded ->
                    onEvent(DetailsEvent.OnSectionExpanded("event_actions", expanded))
                }
            ) {
                if (eventActions.isEmpty()) {
                    Text(stringResource(R.string.event_actions_not_found))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        eventActions.forEach { action ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(action.title, fontWeight = FontWeight.Medium)
                                    Text(stringResource(R.string.code_label, action.code))
                                    Text(stringResource(R.string.order_label, action.order))
                                }
                            }
                        }
                    }
                }
            }
        }

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
                            Text(stringResource(R.string.copy_all_as_json))
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
            Text(stringResource(R.string.code_label, widget.code))
            Text(stringResource(R.string.type, widget.type))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.props_count, widget.propertiesCount))
                Text(stringResource(R.string.styles_count, widget.stylesCount))
                Text(stringResource(R.string.widget_events_count, widget.eventsCount))
            }
        }
    }
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
            Text(stringResource(R.string.code_label, layout.code))
            Text(stringResource(R.string.props_count, layout.propertiesCount))
        }
    }
}

/**
 * Создаёт детализированный JSON с полными данными о микроаппе.
 *
 * @param parsedMicroapp Результат парсинга микроаппа
 * @return JSON-строка
 */
private fun createDetailedJson(parsedMicroapp: com.example.drivenui.engine.parser.SDUIParser.ParsedMicroappResult): String {
    return buildString {
        appendLine("{")

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

        try {
            val widgetsField = parsedMicroapp::class.java.getDeclaredField("widgets")
            widgetsField.isAccessible = true
            val widgets = widgetsField.get(parsedMicroapp) as? List<*> ?: emptyList<Any>()

            appendLine("  \"widgets\": [")
            widgets.forEachIndexed { widgetIndex, widget ->
                if (widget is com.example.drivenui.engine.parser.models.Widget) {
                    appendLine("    {")
                    appendLine("      \"title\": \"${escapeJson(widget.title)}\",")
                    appendLine("      \"code\": \"${escapeJson(widget.code)}\",")
                    appendLine("      \"type\": \"${escapeJson(widget.type)}\",")
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

        try {
            val layoutsField = parsedMicroapp::class.java.getDeclaredField("layouts")
            layoutsField.isAccessible = true
            val layouts = layoutsField.get(parsedMicroapp) as? List<*> ?: emptyList<Any>()

            appendLine("  \"layouts\": [")
            layouts.forEachIndexed { layoutIndex, layout ->
                if (layout is com.example.drivenui.engine.parser.models.Layout) {
                    appendLine("    {")
                    appendLine("      \"title\": \"${escapeJson(layout.title)}\",")
                    appendLine("      \"code\": \"${escapeJson(layout.code)}\",")
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

        try {
            val screenQueriesField = parsedMicroapp::class.java.getDeclaredField("screenQueries")
            screenQueriesField.isAccessible = true
            val screenQueries =
                screenQueriesField.get(parsedMicroapp) as? List<*> ?: emptyList<Any>()

            appendLine("  \"screenQueries\": [")
            screenQueries.forEachIndexed { sqIndex, sq ->
                if (sq is com.example.drivenui.engine.parser.models.ScreenQuery) {
                    appendLine("    {")
                    appendLine("      \"code\": \"${escapeJson(sq.code)}\",")
                    appendLine("      \"screenCode\": \"${escapeJson(sq.screenCode)}\",")
                    appendLine("      \"queryCode\": \"${escapeJson(sq.queryCode)}\",")
                    appendLine("      \"order\": ${sq.order},")
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

        try {
            val allEventActionsField =
                parsedMicroapp::class.java.getDeclaredField("allEventActions")
            allEventActionsField.isAccessible = true
            val allEventActions =
                allEventActionsField.get(parsedMicroapp) as? com.example.drivenui.engine.parser.models.AllEventActions

            appendLine("  \"allEventActions\": [")
            allEventActions?.eventActions?.forEachIndexed { eaIndex, action ->
                appendLine("    {")
                appendLine("      \"title\": \"${escapeJson(action.title)}\",")
                appendLine("      \"code\": \"${escapeJson(action.code)}\",")
                appendLine("      \"order\": ${action.order},")
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
            try {
                val eventActionsField = parsedMicroapp::class.java.getDeclaredField("eventActions")
                eventActionsField.isAccessible = true
                val eventActions =
                    eventActionsField.get(parsedMicroapp) as? List<com.example.drivenui.engine.parser.models.EventAction>

                appendLine("  \"eventActions\": [")
                eventActions?.forEachIndexed { eaIndex, action ->
                    appendLine("    {")
                    appendLine("      \"title\": \"${escapeJson(action.title)}\",")
                    appendLine("      \"code\": \"${escapeJson(action.code)}\",")
                    appendLine("      \"order\": ${action.order},")
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
 * Экранирует специальные символы для JSON.
 *
 * @param value Исходная строка
 * @return Экранированная строка
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


@Preview(name = "Loading")
@Composable
private fun DetailsScreenPreviewLoading() {
    DrivenUITheme {
        DetailsScreen(
            state = DetailsState(isLoading = true),
            onEvent = {},
        )
    }
}

@Preview(name = "No Data")
@Composable
private fun DetailsScreenPreviewNoData() {
    DrivenUITheme {
        DetailsScreen(
            state = DetailsState(errorMessage = "Нет данных"),
            onEvent = {},
        )
    }
}

@Preview(name = "Content")
@Composable
private fun DetailsScreenPreviewContent() {
    val parsedResult = com.example.drivenui.engine.parser.SDUIParser.ParsedMicroappResult(
        microapp = com.example.drivenui.engine.parser.models.Microapp(
            title = "Тест",
            code = "test",
            shortCode = "t",
            deeplink = "test://",
            persistents = emptyList(),
        ),
        screens = listOf(
            com.example.drivenui.engine.parser.models.ParsedScreen(
                title = "Экран 1",
                screenCode = "s1",
                screenShortCode = "s1",
                deeplink = "dl1",
            ),
        ),
    )
    DrivenUITheme {
        DetailsScreen(
            state = DetailsState(
                parsedResult = parsedResult,
                tabData = DetailsTabData(
                    screens = listOf(
                        ScreenItem("1", "Экран 1", "s1", "s1", "dl1", 0, 0, false, 0),
                    ),
                ),
            ),
            onEvent = {},
        )
    }
}