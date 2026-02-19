package com.example.drivenui.presentation.openFile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.drivenui.navigation.NavigationManager
import com.example.drivenui.parser.models.AllStyles
import com.example.drivenui.parser.models.ParsedScreen
import com.example.drivenui.presentation.details.ui.DetailsFragment
import com.example.drivenui.presentation.openFile.model.OpenFileEffect
import com.example.drivenui.presentation.openFile.model.OpenFileEvent
import com.example.drivenui.presentation.openFile.vm.OpenFileViewModel
import com.example.drivenui.presentation.render.TestRenderFragment
import com.example.drivenui.theme.DrivenUITheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
internal class OpenFileFragment : Fragment() {

    private val viewModel: OpenFileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Наблюдаем за эффектами
        observeEffects()

        return ComposeView(requireContext()).apply {
            setContent {
                DrivenUITheme {
                    val openFileState by viewModel.uiState.collectAsState()
                    OpenFileScreen(
                        state = openFileState,
                        onEvent = { event ->
                            viewModel.handleEvent(event)
                        }
                    )
                }
            }
        }
    }

    private fun observeEffects() {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is OpenFileEffect.GoBack -> {
                    activity?.onBackPressed()
                }

                is OpenFileEffect.NavigateToParsingDetails -> {
                    // Сохраняем данные в NavigationManager
                    NavigationManager.setDataForNextScreen(effect.result)

                    // Переходим к DetailsFragment
                    navigateToDetails()
                }

                is OpenFileEffect.NavigateToTestScreen -> {
                    // Сохраняем данные в NavigationManager
                    NavigationManager.setDataForNextScreen(effect.result)

                    // Переходим к DetailsFragment
                    navigateToTestFragment(
                        parsedScreen = effect.result.screens,
                        styles = effect.result.styles,
                        microappCode = effect.result.microapp?.code
                    )
                }

                is OpenFileEffect.ShowError -> {
                    showErrorDialog(effect.message)
                }

                is OpenFileEffect.ShowSuccess -> {
                    showSuccessSnackbar(effect.message)
                }

                is OpenFileEffect.ShowSuccessWithBindings -> {
                    showSuccessWithBindings(effect)
                }

                is OpenFileEffect.ShowBindingStats -> {
                    showBindingStatsDialog(effect)
                }

                is OpenFileEffect.ShowJsonFileSelectionDialog -> {
                    showJsonFileSelectionDialog(effect)
                }

                is OpenFileEffect.OpenQrScanner -> {
                    val options = ScanOptions().apply {
                        setPrompt("Отсканируйте QR-код с ссылкой на microapp")
                        setBeepEnabled(false)
                        setOrientationLocked(true)
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setCameraId(0) // задняя камера
                    }

                    qrScannerLauncher.launch(options)
                }
            }
        }.launchIn(lifecycleScope)
    }

    private val qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            // QR успешно считан
            viewModel.handleEvent(
                OpenFileEvent.OnQrScanned(result.contents)
            )
        } else {
            // Пользователь отменил
            showErrorDialog("Сканирование отменено")
        }
    }

    private fun navigateToDetails() {
        val detailsFragment = DetailsFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, detailsFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToTestFragment(
        parsedScreen: List<ParsedScreen>?,
        styles: AllStyles?,
        microappCode: String? = null
    ) {
        val testFragment = TestRenderFragment.newInstance(
            parsedScreen, styles, microappCode
        )
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, testFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSuccessSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setAction("OK") {}
                .show()
        }
    }

    private fun showSuccessWithBindings(effect: OpenFileEffect.ShowSuccessWithBindings) {
        val message = buildString {
            append(effect.message)
            append("\n\n")
            effect.bindingStats?.let { stats ->
                append("Статистика биндингов:\n")
                stats.forEach { (key, value) ->
                    when (key) {
                        "resolvedValues" -> {
                            append("  $key: ${(value as Map<*, *>).size} значений\n")
                        }
                        "resolutionRate" -> {
                            val rate = value as Float
                            append("  $key: ${String.format("%.1f", rate * 100)}%\n")
                        }
                        else -> {
                            append("  $key: $value\n")
                        }
                    }
                }
                if (effect.resolvedValues.isNotEmpty()) {
                    append("\nПримеры разрешенных значений:\n")
                    effect.resolvedValues.entries.take(3).forEach { (key, value) ->
                        append("  $key = $value\n")
                    }
                }
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Парсинг с биндингами завершен")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Детали биндингов") { _, _ ->
                viewModel.handleEvent(OpenFileEvent.OnShowBindingStats)
            }
            .show()
    }

    private fun showBindingStatsDialog(effect: OpenFileEffect.ShowBindingStats) {
        val message = buildString {
            append("Детальная статистика биндингов:\n\n")

            effect.stats?.let { stats ->
                stats.forEach { (key, value) ->
                    when (key) {
                        "resolvedValues" -> {
                            val resolvedMap = value as Map<*, *>
                            append("$key: ${resolvedMap.size} значений\n")
                            if (resolvedMap.isNotEmpty()) {
                                append("\nРазрешенные значения:\n")
                                resolvedMap.entries.take(10).forEach { (k, v) ->
                                    append("  $k = $v\n")
                                }
                                if (resolvedMap.size > 10) {
                                    append("  ... и еще ${resolvedMap.size - 10} значений\n")
                                }
                            }
                        }
                        "resolutionRate" -> {
                            val rate = value as Float
                            append("$key: ${String.format("%.1f", rate * 100)}%\n")
                        }
                        else -> {
                            append("$key: $value\n")
                        }
                    }
                }
            } ?: run {
                append("Статистика не доступна\n")
            }

            if (effect.resolvedValues.isNotEmpty()) {
                append("\nВсе разрешенные значения (${effect.resolvedValues.size}):\n")
                effect.resolvedValues.entries.take(15).forEach { (key, value) ->
                    append("  $key = $value\n")
                }
                if (effect.resolvedValues.size > 15) {
                    append("  ... и еще ${effect.resolvedValues.size - 15} значений\n")
                }
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Статистика биндингов")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showJsonFileSelectionDialog(effect: OpenFileEffect.ShowJsonFileSelectionDialog) {
        val items = effect.availableFiles.toTypedArray()
        val checkedItems = BooleanArray(items.size) { index ->
            effect.selectedFiles.contains(items[index])
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выберите JSON файлы для биндингов")
            .setMultiChoiceItems(items, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Применить") { _, _ ->
                val selectedFiles = items.filterIndexed { index, _ -> checkedItems[index] }
                viewModel.handleEvent(OpenFileEvent.OnSelectJsonFiles(selectedFiles))
            }
            .setNegativeButton("Отмена", null)
            .setNeutralButton("Выбрать все") { _, _ ->
                val allSelected = effect.availableFiles
                viewModel.handleEvent(OpenFileEvent.OnSelectJsonFiles(allSelected))
            }
            .show()
    }

    companion object {
        fun newInstance() = OpenFileFragment()
    }
}