package com.example.drivenui.engine.cache

import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.LayoutType
import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Сериализуемая иерархия компонентов (как ComponentModel, но ModifierParams вместо Modifier,
 * TextStyle/Color заменены на коды).
 */
sealed interface CachedComponentModel {
    val modifierParams: ModifierParams
    val alignmentStyle: String
    val visibility: Boolean
    val visibilityCode: String?
}

data class CachedLayoutModel(
    override val modifierParams: ModifierParams,
    val type: LayoutType,
    val children: List<CachedComponentModel>,
    val onCreateActions: List<UiAction>,
    val onTapActions: List<UiAction>,
    val backgroundColorStyleCode: String?,
    val roundStyleCode: String?,
    override val alignmentStyle: String,
    val forIndexName: String?,
    val maxForIndex: String?,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel

data class CachedLabelModel(
    override val modifierParams: ModifierParams,
    val text: String,
    val widgetCode: String,
    val textStyleCode: String?,
    val colorStyleCode: String?,
    val tapActions: List<UiAction>,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel

data class CachedButtonModel(
    override val modifierParams: ModifierParams,
    val enabled: Boolean,
    val text: String,
    val roundedCornerSize: Int?,
    val roundStyleCode: String?,
    val textStyleCode: String?,
    val colorStyleCode: String?,
    val backgroundColorStyleCode: String?,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel

data class CachedImageModel(
    override val modifierParams: ModifierParams,
    val url: String?,
    val widgetCode: String,
    val tapActions: List<UiAction>,
    val colorStyleCode: String?,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel

data class CachedAppBarModel(
    override val modifierParams: ModifierParams,
    val title: String?,
    val iconLeftUrl: String?,
    val textStyleCode: String?,
    val colorStyleCode: String?,
    val tapActions: List<UiAction>,
    val widgetCode: String,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel

data class CachedInputModel(
    override val modifierParams: ModifierParams,
    val text: String,
    val hint: String,
    val readOnly: Boolean,
    val widgetCode: String,
    val finishTypingActions: List<UiAction>,
    override val alignmentStyle: String,
    override val visibility: Boolean = true,
    override val visibilityCode: String? = null
) : CachedComponentModel
