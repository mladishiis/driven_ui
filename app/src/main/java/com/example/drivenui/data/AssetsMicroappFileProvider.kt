import android.content.Context
import com.example.drivenui.domain.MicroappFileProvider

class AssetsMicroappFileProvider(
    private val context: Context
) : MicroappFileProvider {

    override fun readMicroapp() =
        read("microapp.xml")

    override fun readStyles() =
        read("resources/allStyles.xml")

    override fun readQueries() =
        read("queries/allQueries.xml")

    override fun readScreens(): List<Pair<String, String>> {
        return context.assets.list("screens")
            ?.filter { it.endsWith(".xml") }
            ?.map { it to read("screens/$it") }
            ?: emptyList()
    }

    private fun read(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }
}
