package com.example.drivenui.engine.mappers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

fun Modifier.applyPaddingStyle(paddingStyle: PaddingValues): Modifier =
    this.padding(paddingStyle)