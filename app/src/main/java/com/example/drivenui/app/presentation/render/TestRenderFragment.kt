package com.example.drivenui.app.presentation.render

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.drivenui.engine.generative_screen.GenerativeScreen
import com.example.drivenui.engine.generative_screen.GenerativeScreenViewModel
import com.example.drivenui.app.navigation.NavigationManager
import dagger.hilt.android.AndroidEntryPoint

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
                GenerativeScreen(
                    viewModel = viewModel,
                    onExit = {
                        parentFragmentManager.popBackStack()
                    }
                )
            }
        }
    }

    companion object {
        fun newInstance() = TestRenderFragment()
    }
}