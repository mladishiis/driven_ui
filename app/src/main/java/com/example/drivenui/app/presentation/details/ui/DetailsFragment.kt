package com.example.drivenui.app.presentation.details.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.drivenui.app.navigation.NavigationManager
import com.example.drivenui.app.presentation.details.model.ComponentTreeItem
import com.example.drivenui.app.presentation.details.model.DetailsEffect
import com.example.drivenui.app.presentation.details.ui.DetailsScreen
import com.example.drivenui.app.presentation.details.vm.DetailsViewModel
import com.example.drivenui.app.theme.DrivenUITheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
internal class DetailsFragment : Fragment() {

    private val viewModel: DetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Получаем данные из NavigationManager (новая структура)
        val parsedResult = NavigationManager.getAndClearData()

        // Передаем данные в ViewModel
        viewModel.setParsedResult(parsedResult)

        // Наблюдаем за эффектами
        observeEffects()

        return ComposeView(requireContext()).apply {
            setContent {
                DrivenUITheme {
                    val state by viewModel.uiState.collectAsState()

                    DetailsScreen(
                        state = state,
                        viewModel = viewModel,
                        onEvent = { event ->
                            viewModel.handleEvent(event)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем данные при уничтожении вью
        NavigationManager.clearAllData()
    }

    private fun observeEffects() {
        viewModel.effect.onEach { effect ->
            when (effect) {
                DetailsEffect.GoBack -> {
                    requireActivity().onBackPressed()
                }

                is DetailsEffect.ShowMessage -> {
                    showToast(effect.message)
                }

                is DetailsEffect.ShowCopiedMessage -> {
                    showToast(effect.text)
                }

                is DetailsEffect.ShowExportSuccess -> {
                    showToast("Экспорт завершен: ${effect.filePath}")
                }

                is DetailsEffect.ShowComponentStructure -> {
                    showComponentStructureDialog(effect.title, effect.structureInfo)
                }

                is DetailsEffect.ShowScreenComponents -> {
                    showScreenComponentsDialog(effect.screenTitle, effect.components)
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showComponentStructureDialog(title: String, structureInfo: String) {
        // Замените использование @Composable функции на Context
        val context = requireContext()
        Toast.makeText(
            context,
            "$title\n(см. логи для деталей)",
            Toast.LENGTH_LONG
        ).show()

        // Логируем в консоль
        Log.d("DetailsFragment", structureInfo)
    }

    private fun showScreenComponentsDialog(screenTitle: String, components: List<ComponentTreeItem>) {
        // TODO: Реализовать показ диалога или перейти на новый экран
        // с отображением дерева компонентов
        Toast.makeText(
            requireContext(),
            "$screenTitle: ${components.size} компонентов",
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        fun newInstance(): DetailsFragment {
            return DetailsFragment()
        }
    }
}