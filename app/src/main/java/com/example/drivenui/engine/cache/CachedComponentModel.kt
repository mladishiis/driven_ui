package com.example.drivenui.engine.cache

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
