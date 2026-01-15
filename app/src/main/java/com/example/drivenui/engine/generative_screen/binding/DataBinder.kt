// DataBinder.kt
package com.example.drivenui.engine.generative_screen.binding


import android.util.Log
import com.example.drivenui.engine.generative_screen.models.ScreenModel
import com.example.drivenui.engine.uirender.models.AppBarModel
import com.example.drivenui.engine.uirender.models.ButtonModel
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.LabelModel
import com.example.drivenui.engine.uirender.models.LayoutModel
import com.example.drivenui.parser.models.DataContext

class DataBinder {

    /**
     * Применяет биндинги данных к модели экрана
     */
    fun applyBindings(
        screenModel: ScreenModel,
        dataContext: DataContext
    ): ScreenModel {
        Log.d(TAG, "Applying bindings for screen: ${screenModel.id}")
        Log.d(TAG, "DataContext jsonSources: ${dataContext.jsonSources.keys}")

        val processedRootComponent = screenModel.rootComponent?.let {
            applyBindingsToComponent(it, dataContext)
        }

        return screenModel.copy(
            rootComponent = processedRootComponent
        )
    }

    /**
     * Рекурсивно применяет биндинги к компоненту и его детям
     */
    private fun applyBindingsToComponent(
        component: ComponentModel?,
        dataContext: DataContext
    ): ComponentModel? {
        return when (component) {
            is LabelModel -> applyBindingsToLabel(component, dataContext)
            is ButtonModel -> applyBindingsToButton(component, dataContext)
            is AppBarModel -> applyBindingsToAppBar(component, dataContext)
            is LayoutModel -> applyBindingsToLayout(component, dataContext)
            else -> component
        }
    }

    private fun applyBindingsToLabel(
        label: LabelModel,
        dataContext: DataContext
    ): LabelModel {
        val originalText = label.text
        val bindings = DataBindingParser.parseBindings(originalText)

        Log.d(TAG, "Label text: '$originalText'")
        Log.d(TAG, "Found bindings: $bindings")

        if (bindings.isNotEmpty()) {
            val newText = DataBindingParser.replaceBindings(originalText, bindings, dataContext)
            Log.d(TAG, "Replaced text: '$newText' (was: '$originalText')")
            return label.copy(text = newText)
        }
        return label
    }

    private fun applyBindingsToButton(
        button: ButtonModel,
        dataContext: DataContext
    ): ButtonModel {
        val bindings = DataBindingParser.parseBindings(button.text)
        if (bindings.isNotEmpty()) {
            val newText = DataBindingParser.replaceBindings(button.text, bindings, dataContext)
            return button.copy(text = newText)
        }
        return button
    }

    private fun applyBindingsToAppBar(
        appBar: AppBarModel,
        dataContext: DataContext
    ): AppBarModel {
        val title = appBar.title ?: return appBar
        val bindings = DataBindingParser.parseBindings(title)
        if (bindings.isNotEmpty()) {
            val newTitle = DataBindingParser.replaceBindings(title, bindings, dataContext)
            return appBar.copy(title = newTitle)
        }
        return appBar
    }

    private fun applyBindingsToLayout(
        layout: LayoutModel,
        dataContext: DataContext
    ): LayoutModel {
        val processedChildren = layout.children.map { child ->
            applyBindingsToComponent(child, dataContext)
        }.filterNotNull()

        return layout.copy(children = processedChildren)
    }

    companion object {
        private const val TAG = "DataBinder"
    }
}