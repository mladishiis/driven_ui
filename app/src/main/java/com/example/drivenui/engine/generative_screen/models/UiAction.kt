package com.example.drivenui.engine.generative_screen.models

/**
 * Действие UI, полученное из разметки событий (`eventAction`) и обрабатываемое `ActionHandler`.
 *
 * Соответствует кодам из `EventAction` после `mapToUiAction`.
 */
sealed interface UiAction {

    /**
     * Открыть экран по коду из реестра экранов микроаппа.
     *
     * @param screenCode код экрана, как `ParsedScreen.screenCode`
     */
    data class OpenScreen(val screenCode: String) : UiAction

    /**
     * Показать нижнюю шторку с корнем из указанного экрана.
     *
     * @param screenCode код экрана-шаблона для содержимого шторки
     */
    data class OpenBottomSheet(val screenCode: String) : UiAction

    /**
     * Пересобрать текущий экран: биндинги FOR и полный резолв стилей/шаблонов.
     *
     * @param screenCode зарезервировано под выбор экрана; сейчас обновляется текущий экран
     */
    data class RefreshScreen(val screenCode: String) : UiAction

    /**
     * Обновить отображение виджета (сейчас реализовано как обновление всего экрана).
     *
     * @param widgetCode код виджета на экране
     */
    data class RefreshWidget(val widgetCode: String) : UiAction

    /**
     * Обновить layout (сейчас реализовано как обновление всего экрана).
     *
     * @param layoutCode код layout на экране
     */
    data class RefreshLayout(val layoutCode: String) : UiAction

    /**
     * Обработать внешний или внутренний deeplink.
     *
     * @param deeplink строка deeplink
     */
    data class OpenDeeplink(val deeplink: String) : UiAction

    /**
     * Выполнение запроса (на данном этапе только загрузка mock JSON по [mockFile], если [mockEnabled]).
     *
     * @param queryCode Ключ результата в DataContext для биндингов `${queryCode.path}`
     * @param type HTTP-метод (для будущей сети)
     * @param endpoint Путь (для будущей сети)
     * @param mockEnabled Загружать ли [mockFile] в контекст
     * @param mockFile Имя JSON в assets / каталоге моков
     */
    data class ExecuteQuery(
        val queryCode: String,
        val type: String = "GET",
        val endpoint: String = "",
        val mockEnabled: Boolean = true,
        val mockFile: String? = null,
    ) : UiAction

    /**
     * Преобразование значения переменной (логика движка по коду сценария).
     *
     * @param variableName имя переменной
     * @param newValue новое значение
     */
    data class DataTransform(val variableName: String, val newValue: String) : UiAction

    /**
     * Сохранить значение в контекст микроаппа (`@{microapp.x.y}`) или движка (`@@{z}`).
     *
     * @param valueTo целевая ссылка на переменную контекста
     * @param valueFrom источник значения (строка или выражение виджета)
     */
    data class SaveToContext(val valueTo: String, val valueFrom: String) : UiAction

    /**
     * Вызов нативного кода хоста по зарегистрированному коду.
     *
     * @param actionCode код действия
     * @param parameters параметры, переданные из разметки
     */
    data class NativeCode(
        val actionCode: String,
        val parameters: Map<String, String> = emptyMap(),
    ) : UiAction

    /** Навигация назад в стеке экранов микроаппа. */
    data object Back : UiAction

    /** Пустое действие. */
    data object Empty : UiAction
}
