package com.example.drivenui.app.data

import android.content.Context
import com.example.drivenui.app.domain.MicroappFileProvider
import java.io.File
import javax.inject.Inject

/**
 * Чтение microapp из файловой системы.
 *
 * @property context Контекст приложения
 */
class DirMicroappFileProvider @Inject constructor(
    private val context: Context
) : MicroappFileProvider {

    /** Корневая папка микроаппа в файловой системе. */
    private fun getRootDir(): File {
        val foundRoot = MicroappRootFinder.findMicroappRoot(context)
        return foundRoot
            ?: error("Microapp root directory not found in 'microapps'. Please download and extract microapp archive.")
    }

    override fun readMicroapp(): String =
        File(getRootDir(), "microapp.xml").readText()

    override fun readStyles(): String =
        File(getRootDir(), "resources/allStyles.xml").readText()

    override fun readMicroappOrEmpty(): String {
        val file = File(getRootDir(), "microapp.xml")
        return if (file.exists()) file.readText() else ""
    }

    override fun readScreens(): List<Pair<String, String>> =
        getRootDir().resolve("screens")
            .listFiles()
            ?.filter { it.extension == "xml" }
            ?.map { it.name to it.readText() }
            .orEmpty()
}
