package com.example.drivenui.data

import android.content.Context
import com.example.drivenui.domain.MicroappFileProvider
import java.io.File
import javax.inject.Inject

/**
 * Чтение microapp из файловой системы
 */
internal class DirMicroappFileProvider @Inject constructor(
    private val context: Context
) : MicroappFileProvider {

    /**
     * Динамически определяет корневую папку микроаппа при каждом обращении.
     * Если микроапп не найден, выбрасывает исключение —
     * это означает, что архив ещё не был загружен/распакован.
     */
    private fun getRootDir(): File {
        val foundRoot = MicroappRootFinder.findMicroappRoot(context)
        return foundRoot
            ?: error("Microapp root directory not found in 'microapps'. Please download and extract microapp archive.")
    }

    override fun readMicroapp(): String =
        File(getRootDir(), "microapp.xml").readText()

    override fun readStyles(): String =
        File(getRootDir(), "resources/allStyles.xml").readText()

    override fun readQueries(): String =
        File(getRootDir(), "queries/allQueries.xml").readText()

    override fun readMicroappOrEmpty(): String {
        val file = File(getRootDir(), "microapp.xml")
        return if (file.exists()) file.readText() else ""
    }

    override fun readQueriesOrEmpty(): String {
        val file = File(getRootDir(), "queries/allQueries.xml")
        return if (file.exists()) file.readText() else ""
    }

    override fun readScreens(): List<Pair<String, String>> =
        getRootDir().resolve("screens")
            .listFiles()
            ?.filter { it.extension == "xml" }
            ?.map { it.name to it.readText() }
            .orEmpty()
}
