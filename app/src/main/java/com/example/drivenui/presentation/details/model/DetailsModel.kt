package com.example.drivenui.presentation.details.model

import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.utile.VtbEffect
import com.example.drivenui.utile.VtbEvent
import com.example.drivenui.utile.VtbState

/** События на экране деталей */
internal sealed interface DetailsEvent : VtbEvent {

    /** Жмак по кнопке назад */
    data object OnBackClick : DetailsEvent

    /** Жмак по кнопке обновления */
    data object OnRefreshClick : DetailsEvent

    /** Выбор вкладки */
    data class OnTabSelected(val tabIndex: Int) : DetailsEvent

    /** Развернуть/свернуть секцию */
    data class OnSectionExpanded(val sectionId: String, val isExpanded: Boolean) : DetailsEvent

    /** Копировать в буфер обмена */
    data class OnCopyToClipboard(val text: String) : DetailsEvent

    /** Экспорт данных */
    data object OnExportData : DetailsEvent
}

/** События с вью-модели на экран */
internal sealed interface DetailsEffect : VtbEffect {

    /** Жмак по кнопке назад */
    data object GoBack : DetailsEffect

    /** Показать сообщение */
    data class ShowMessage(val message: String) : DetailsEffect

    /** Скопировано в буфер обмена */
    data class ShowCopiedMessage(val text: String) : DetailsEffect

    /** Экспорт завершен */
    data class ShowExportSuccess(val filePath: String) : DetailsEffect
}

/**
 * Состояние экрана деталей
 */
internal data class DetailsState(
    val parsedMicroapp: SDUIParser.ParsedMicroapp? = null,
    val isLoading: Boolean = false,
    val selectedTabIndex: Int = 0,
    val expandedSections: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val message: String? = null,
    val hasData: Boolean = false
) : VtbState {

    /** Доступные вкладки */
    val tabs = listOf("Обзор", "Экраны", "Стили", "Запросы", "События", "Виджеты", "Лэйауты", "Детали")

    /** Название микроаппа */
    val microappTitle: String get() = parsedMicroapp?.microapp?.title ?: "Неизвестный микроапп"

    /** Количество экранов */
    val screensCount: Int get() = parsedMicroapp?.screens?.size ?: 0

    /** Количество стилей текста */
    val textStylesCount: Int get() = parsedMicroapp?.styles?.textStyles?.size ?: 0

    /** Количество стилей цвета */
    val colorStylesCount: Int get() = parsedMicroapp?.styles?.colorStyles?.size ?: 0

    /** Количество стилей скругления */
    val roundStylesCount: Int get() = parsedMicroapp?.styles?.roundStyles?.size ?: 0

    /** Количество стилей отступов */
    val paddingStylesCount: Int get() = parsedMicroapp?.styles?.paddingStyles?.size ?: 0

    /** Количество стилей выравнивания */
    val alignmentStylesCount: Int get() = parsedMicroapp?.styles?.alignmentStyles?.size ?: 0

    /** Количество запросов */
    val queriesCount: Int get() = parsedMicroapp?.queries?.size ?: 0

    /** Количество экранных запросов */
    val screenQueriesCount: Int get() = parsedMicroapp?.screenQueries?.size ?: 0

    /** Количество событий */
    val eventsCount: Int get() = parsedMicroapp?.events?.events?.size ?: 0

    /** Количество действий событий */
    val eventActionsCount: Int get() = parsedMicroapp?.eventActions?.eventActions?.size ?: 0

    /** Количество виджетов */
    val widgetsCount: Int get() = parsedMicroapp?.widgets?.size ?: 0

    /** Количество лэйаутов */
    val layoutsCount: Int get() = parsedMicroapp?.layouts?.size ?: 0

    fun copyAndUpdate(
        isLoading: Boolean,
        error: String?
    ): DetailsState = copy(
        isLoading = isLoading,
        errorMessage = error
    )
}

/**
 * Модель для отображения экрана
 */
data class ScreenItem(
    val id: String,
    val title: String,
    val code: String,
    val shortCode: String,
    val deeplink: String,
    val eventsCount: Int,
    val layoutsCount: Int
)

/**
 * Модель для отображения стиля текста
 */
data class TextStyleItem(
    val code: String,
    val fontFamily: String,
    val fontSize: Int,
    val fontWeight: Int
)

/**
 * Модель для отображения стиля цвета
 */
data class ColorStyleItem(
    val code: String,
    val lightColor: String,
    val darkColor: String,
    val lightOpacity: Int,
    val darkOpacity: Int
)

/**
 * Модель для отображения стиля скругления
 */
data class RoundStyleItem(
    val code: String,
    val radiusValue: Int
)

/**
 * Модель для отображения стиля отступа
 */
data class PaddingStyleItem(
    val code: String,
    val paddingLeft: Int,
    val paddingTop: Int,
    val paddingRight: Int,
    val paddingBottom: Int
)

/**
 * Модель для отображения стиля выравнивания
 */
data class AlignmentStyleItem(
    val code: String
)

/**
 * Модель для отображения запроса
 */
data class QueryItem(
    val title: String,
    val code: String,
    val type: String,
    val endpoint: String,
    val propertiesCount: Int
)

/**
 * Модель для отображения экранного запроса
 */
data class ScreenQueryItem(
    val id: String,
    val code: String,
    val screenCode: String,
    val queryCode: String,
    val order: Int
)

/**
 * Модель для отображения события
 */
data class EventItem(
    val title: String,
    val code: String,
    val actionsCount: Int
)

/**
 * Модель для отображения действия события
 */
data class EventActionItem(
    val id: String,
    val title: String,
    val code: String,
    val order: Int,
    val propertiesCount: Int
)

/**
 * Модель для отображения виджета
 */
data class WidgetItem(
    val id: String,
    val title: String,
    val code: String,
    val type: String,
    val propertiesCount: Int,
    val stylesCount: Int,
    val eventsCount: Int
)

/**
 * Модель для отображения лэйаута
 */
data class LayoutItem(
    val id: String,
    val title: String,
    val code: String,
    val propertiesCount: Int
)

/**
 * Модель для отображения микроаппа
 */
data class MicroappItem(
    val id: String,
    val title: String,
    val code: String,
    val shortCode: String,
    val deeplink: String,
    val persistents: List<String>
)

/**
 * Модель для отображения всех стилей
 */
data class AllStylesItem(
    val textStylesCount: Int,
    val colorStylesCount: Int,
    val roundStylesCount: Int,
    val paddingStylesCount: Int,
    val alignmentStylesCount: Int
)