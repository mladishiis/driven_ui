package com.example.drivenui.engine.uirender.renderer

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.drivenui.engine.mappers.ComposeStyleRegistry

val LocalStyleRegistry = staticCompositionLocalOf<ComposeStyleRegistry?> { null }

val LocalIsDarkTheme = compositionLocalOf { false }