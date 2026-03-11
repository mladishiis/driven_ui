package com.example.drivenui.app.presentation.render

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.drivenui.app.navigation.NavigationManager
import com.example.drivenui.engine.generative_screen.GenerativeScreen
import com.example.drivenui.engine.generative_screen.GenerativeScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Фрагмент тестового рендеринга микроаппа.
 *
 * Отображает [GenerativeScreen] с данными, переданными через [NavigationManager].
 */
@AndroidEntryPoint
internal class TestRenderFragment : Fragment() {

    private val viewModel: GenerativeScreenViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        val mappedData = NavigationManager.getAndClearMappedData()
        if (mappedData != null) {
            viewModel.setMappedResult(mappedData)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                GenerativeScreen(
                    state = state,
                    bottomSheetState = viewModel.bottomSheetState,
                    onActions = viewModel::handleActions,
                    onBack = {
                        val handled = viewModel.navigateBack()
                        if (!handled) {
                            parentFragmentManager.popBackStack()
                        }
                    },
                    onWidgetValueChange = viewModel::onWidgetValueChange,
                    applyBindingsForComponent = viewModel::applyBindingsToComponent,
                    getSheetCornerRadiusDp = viewModel::getSheetCornerRadiusDp,
                )
            }
        }
    }

    companion object {
        /**
         * Создаёт новый экземпляр фрагмента.
         *
         * @return экземпляр [TestRenderFragment]
         */
        fun newInstance() = TestRenderFragment()
    }
}