package com.example.drivenui.app.presentation.render

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.drivenui.app.navigation.NavigationManager
import com.example.drivenui.engine.generative_screen.GenerativeScreen
import com.example.drivenui.engine.generative_screen.GenerativeScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        savedInstanceState: Bundle?
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
                    onBack = { handleMicroappBackPress() },
                    onWidgetValueChange = viewModel::onWidgetValueChange,
                    applyBindingsForComponent = viewModel::applyBindingsToComponent,
                    getSheetCornerRadiusDp = viewModel::getSheetCornerRadiusDp,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleMicroappBackPress()
                }
            },
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.exitMicroappEvents.collect {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun handleMicroappBackPress() {
        if (!viewModel.navigateBack()) {
            parentFragmentManager.popBackStack()
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