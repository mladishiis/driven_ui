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
import com.example.drivenui.presentation.details.ui.DetailsFragment
import com.example.drivenui.presentation.openFile.model.OpenFileEffect
import com.example.drivenui.presentation.openFile.model.OpenFileEvent
import com.example.drivenui.presentation.openFile.vm.OpenFileViewModel
import com.example.drivenui.theme.DrivenUITheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
                        onUploadFile = { event ->
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

                is OpenFileEffect.ShowError -> {
                    showErrorDialog(effect.message)
                }

                is OpenFileEffect.ShowSuccess -> {
                    showSuccessSnackbar(effect.message)
                }

                is OpenFileEffect.ShowParsingResultDialog -> {
                    showParsingResultDialog(effect)
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun navigateToDetails() {
        val detailsFragment = DetailsFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, detailsFragment)
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
        // Можно использовать Snackbar или Toast
        android.widget.Toast.makeText(
            requireContext(),
            message,
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    private fun showParsingResultDialog(effect: OpenFileEffect.ShowParsingResultDialog) {
        val message = """
            Результат парсинга:
            
            Микроапп: ${effect.title}
            Экран${if (effect.screensCount != 1) "ов" else ""}: ${effect.screensCount}
            Стилей текста: ${effect.textStylesCount}
            Стилей цвета: ${effect.colorStylesCount}
            Запросов API: ${effect.queriesCount}
            
            Нажмите "Показать результат парсинга" для деталей.
        """.trimIndent()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Парсинг завершен")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Детали") { _, _ ->
                viewModel.handleEvent(OpenFileEvent.OnShowParsingDetails)
            }
            .show()
    }

    companion object {
        fun newInstance() = OpenFileFragment()
    }
}