package com.example.drivenui.app.data

import android.content.Context
import com.example.drivenui.app.domain.MicroappFileProvider

class AssetsMicroappFileProvider(
    private val context: Context
) : MicroappFileProvider {

    override fun readMicroapp() =
        read("microapp.xml")

    override fun readStyles() =
        read("resources/allStyles.xml")

    override fun readQueries() =
        read("resources/queries/allQueries.xml")

    override fun readMicroappOrEmpty(): String =
        readOrEmpty("microapp.xml")

    override fun readQueriesOrEmpty(): String =
        readOrEmpty("resources/queries/allQueries.xml")

    override fun readScreens(): List<Pair<String, String>> {
        return context.assets.list("screens")
            ?.filter { it.endsWith(".xml") }
            ?.map { it to read("screens/$it") }
            ?: emptyList()
    }

    private fun read(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }

    private fun readOrEmpty(path: String): String =
        try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            ""
        }
}