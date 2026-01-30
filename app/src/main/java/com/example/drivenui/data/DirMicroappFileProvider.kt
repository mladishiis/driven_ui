package com.example.drivenui.data

import android.content.Context
import com.example.drivenui.domain.MicroappFileProvider
import java.io.File
import javax.inject.Inject

/**
 * Чтение microapp из файловой системы
 */
internal class DirMicroappFileProvider @Inject constructor(
    context: Context
) : MicroappFileProvider {

    private val rootDir =
        File(context.filesDir, "assets_simulation/microappTavrida")

    override fun readMicroapp(): String =
        File(rootDir, "microapp.xml").readText()

    override fun readStyles(): String =
        File(rootDir, "resources/allStyles.xml").readText()

    override fun readQueries(): String =
        File(rootDir, "queries/allQueries.xml").readText()

    override fun readScreens(): List<Pair<String, String>> =
        rootDir.resolve("screens")
            .listFiles()
            ?.filter { it.extension == "xml" }
            ?.map { it.name to it.readText() }
            .orEmpty()
}
