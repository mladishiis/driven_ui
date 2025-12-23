package com.example.drivenui.presentation.details.model



/**
 * Расширенная информация о компоненте
 */
data class ComponentDetailItem(
    val id: String,
    val title: String,
    val code: String,
    val type: String,
    val depth: Int,
    val propertiesCount: Int,
    val stylesCount: Int,
    val eventsCount: Int,
    val childrenCount: Int
)