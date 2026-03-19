package com.example.drivenui.engine.cache

import com.example.drivenui.engine.uirender.models.ModifierParams

/**
 * Сериализуемая иерархия компонентов (как ComponentModel, но ModifierParams вместо Modifier,
 * TextStyle/Color заменены на коды).
 *
 * @property modifierParams параметры модификатора (padding, размеры)
 * @property alignment выравнивание (topLeft, center, и т.д.)
 * @property visibility видимость компонента
 * @property visibilityCode код условной видимости (для выражений вроде ${...})
 */
sealed interface CachedComponentModel {
    val modifierParams: ModifierParams
    val alignment: String
    val visibility: Boolean
    val visibilityCode: String?
}
