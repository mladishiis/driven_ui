package com.example.drivenui.engine.generative_screen.presentation

import com.example.drivenui.engine.context.IContextManager
import com.example.drivenui.engine.generative_screen.binding.ForLayoutBinding
import com.example.drivenui.engine.generative_screen.models.ScreenDefinition
import com.example.drivenui.engine.generative_screen.models.ScreenPresentation
import com.example.drivenui.engine.generative_screen.styles.resolveScreen
import com.example.drivenui.engine.mappers.ComposeStyleRegistry
import com.example.drivenui.engine.parser.models.DataContext
import com.example.drivenui.engine.uirender.models.ComponentModel
import java.util.concurrent.atomic.AtomicLong

/**
 * Собирает [ScreenPresentation] из [ScreenDefinition] и актуального контекста данных.
 */
object PresentationBuilder {

    private val epochSequence = AtomicLong(0)

    /**
     * Строит presentation: FOR-биндинги → pre-expand FOR → полный резолв шаблонов и стилей.
     *
     * @param definition описание экрана с шаблонами
     * @param contextManager переменные микроаппа и движка
     * @param styleRegistry реестр стилей
     * @param dataContext JSON и результаты запросов
     * @param useDarkColorPalette ветка цветовой палитры
     * @return готовое дерево для Compose
     */
    fun build(
        definition: ScreenDefinition,
        contextManager: IContextManager,
        styleRegistry: ComposeStyleRegistry,
        dataContext: DataContext,
        useDarkColorPalette: Boolean,
    ): ScreenPresentation {
        val withForBindings = ForLayoutBinding.applyBindings(definition, dataContext)
        val expandContext = ForPresentationExpander.ExpandContext(
            contextManager = contextManager,
            styleRegistry = styleRegistry,
            dataContext = dataContext,
            useDarkColorPalette = useDarkColorPalette,
        )
        val expandedRoot = ForPresentationExpander.expand(withForBindings.rootComponent, expandContext)
        val resolved = resolveScreen(
            screen = withForBindings.copy(rootComponent = expandedRoot),
            contextManager = contextManager,
            styleRegistry = styleRegistry,
            dataContext = dataContext,
            useDarkColorPalette = useDarkColorPalette,
        )
        return ScreenPresentation(
            screenId = definition.id,
            dataEpoch = epochSequence.incrementAndGet(),
            rootComponent = resolved.rootComponent,
        )
    }

    /**
     * Собирает новую presentation с обновлённым корнем (точечный refresh).
     *
     * @param screenId код экрана
     * @param rootComponent обновлённое resolved-дерево
     * @return presentation с новым [ScreenPresentation.dataEpoch]
     */
    fun withUpdatedRoot(
        screenId: String,
        rootComponent: ComponentModel?,
    ): ScreenPresentation =
        ScreenPresentation(
            screenId = screenId,
            dataEpoch = epochSequence.incrementAndGet(),
            rootComponent = rootComponent,
        )
}
