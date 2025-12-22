package com.example.drivenui.presentation.details.ui

import android.os.Bundle
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
import com.example.drivenui.navigation.NavigationManager
import com.example.drivenui.parser.SDUIParser
import com.example.drivenui.presentation.details.model.DetailsEffect
import com.example.drivenui.presentation.details.view.DetailsScreen
import com.example.drivenui.presentation.details.vm.DetailsViewModel
import com.example.drivenui.theme.DrivenUITheme
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
        // Получаем данные из NavigationManager
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
        NavigationManager.clearData()
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
            }
        }.launchIn(lifecycleScope)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    companion object {
        fun newInstance(): DetailsFragment {
            return DetailsFragment()
        }
    }
}