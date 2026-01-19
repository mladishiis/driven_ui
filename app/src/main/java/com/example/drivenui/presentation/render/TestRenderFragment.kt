package com.example.drivenui.presentation.render

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.drivenui.engine.generative_screen.GenerativeScreen
import com.example.drivenui.engine.generative_screen.GenerativeScreenViewModel
import com.example.drivenui.parser.models.AllStyles
import com.example.drivenui.parser.models.ParsedScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class TestRenderFragment : Fragment() {

    private val viewModel: GenerativeScreenViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parsedScreens = arguments?.getParcelableArrayList<ParsedScreen>("parsedScreen")
        val allStyles = arguments?.getParcelable<AllStyles>("allStyles")
        val microappCode = arguments?.getString("microappCode")
        if (parsedScreens != null) {
            viewModel.setParsedResult(parsedScreens, allStyles, microappCode)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                GenerativeScreen(viewModel = viewModel)
            }
        }
    }

    companion object {
        fun newInstance(
            parsedScreen: List<ParsedScreen>?,
            styles: AllStyles?,
            microappCode: String? = null
        ) = TestRenderFragment().apply {
            arguments = bundleOf(
                "allStyles" to styles,
                "parsedScreen" to parsedScreen,
                "microappCode" to microappCode
            )
        }
    }
}