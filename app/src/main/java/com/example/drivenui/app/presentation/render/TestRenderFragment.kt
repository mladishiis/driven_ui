package com.example.drivenui.app.presentation.render

import android.graphics.Bitmap
import android.graphics.Rect
import android.media.MediaActionSound
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.drivenui.app.data.TemplateApi
import com.example.drivenui.app.navigation.NavigationManager
import com.example.drivenui.engine.generative_screen.GenerativeScreen
import com.example.drivenui.engine.generative_screen.GenerativeScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

private const val TAG = "TestRenderFragment"
private const val SCREENSHOT_DELAY_MS = 500L
private const val SCREENSHOT_MESSAGE_DURATION_MS = 1400L

/**
 * Фрагмент тестового рендеринга микроаппа.
 *
 * Отображает [GenerativeScreen] с данными, переданными через [NavigationManager].
 * В режиме шаблона (TEMPLATE_JSON) делает скриншот при каждом переходе на экран
 * и отправляет его на бэкенд.
 */
@AndroidEntryPoint
internal class TestRenderFragment : Fragment() {

    private val viewModel: GenerativeScreenViewModel by viewModels()

    @Inject
    lateinit var templateApi: TemplateApi

    private var composeView: ComposeView? = null
    private var templateInfo: Pair<String, String>? = null
    private val uploadedScreenIds = mutableSetOf<String>()
    private val screenshotSound = MediaActionSound()
    private var screenshotMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenshotSound.load(MediaActionSound.SHUTTER_CLICK)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mappedData = NavigationManager.getAndClearMappedData()
        if (mappedData != null) {
            viewModel.setMappedResult(mappedData)
        }

        templateInfo = NavigationManager.getTemplateInfo()
        Log.d(TAG, "templateInfo=$templateInfo, SDK=${Build.VERSION.SDK_INT}")

        return ComposeView(requireContext()).also { view ->
            composeView = view
            view.setContent {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val styleRegistry by viewModel.styleRegistryState.collectAsStateWithLifecycle()
                Box(modifier = Modifier.fillMaxSize()) {
                    GenerativeScreen(
                        state = state,
                        bottomSheetState = viewModel.bottomSheetState,
                        onActions = viewModel::handleActions,
                        onBack = { handleMicroappBackPress() },
                        onWidgetValueChange = viewModel::onWidgetValueChange,
                        applyBindingsForComponent = viewModel::applyBindingsToComponent,
                        getSheetCornerRadiusDp = viewModel::getSheetCornerRadiusDp,
                        styleRegistry = styleRegistry,
                    )
                    ScreenshotStatusOverlay(
                        message = screenshotMessage,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
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

        if (templateInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Запуск наблюдения за экранами для скриншотов")
            observeScreenChangesForScreenshots()
        } else {
            Log.w(TAG, "Скриншоты отключены: templateInfo=$templateInfo, SDK=${Build.VERSION.SDK_INT}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }

    override fun onDestroy() {
        super.onDestroy()
        screenshotSound.release()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeScreenChangesForScreenshots() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentScreenId
                    .collect { screenId ->
                        Log.d(TAG, "currentScreenId emitted: $screenId")
                        if (screenId == null) return@collect
                        if (screenId in uploadedScreenIds) {
                            Log.d(TAG, "Экран $screenId уже отправлен, пропускаем")
                            return@collect
                        }
                        uploadedScreenIds.add(screenId)
                        val cvRef = composeView ?: return@collect
                        cvRef.postDelayed({
                            takeAndUploadScreenshot(screenId)
                        }, SCREENSHOT_DELAY_MS)
                    }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun takeAndUploadScreenshot(screenId: String) {
        val view = composeView ?: return
        val (_, microappCode) = templateInfo ?: return
        if (view.width == 0 || view.height == 0) return

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val rect = Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)

        PixelCopy.request(
            requireActivity().window,
            rect,
            bitmap,
            { result ->
                if (result == PixelCopy.SUCCESS) {
                    screenshotSound.play(MediaActionSound.SHUTTER_CLICK)
                    showScreenshotMessage("Скриншот снят: $screenId")
                    uploadBitmap(bitmap, microappCode, screenId)
                } else {
                    bitmap.recycle()
                    Log.w(TAG, "PixelCopy failed for screen $screenId, result=$result")
                }
            },
            Handler(Looper.getMainLooper()),
        )
    }

    private fun uploadBitmap(
        bitmap: Bitmap,
        microappCode: String,
        screenId: String,
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                bitmap.recycle()
                val bytes = stream.toByteArray()
                templateApi.uploadScreenshot(microappCode, screenId, bytes)
                    .onSuccess {
                        Log.d(TAG, "Скриншот загружен: $screenId")
                        withContext(Dispatchers.Main) {
                            showScreenshotMessage("Скриншот отправлен: $screenId")
                        }
                    }
                    .onFailure {
                        Log.w(TAG, "Ошибка загрузки скриншота: $screenId", it)
                        withContext(Dispatchers.Main) {
                            showScreenshotMessage("Ошибка отправки: $screenId")
                        }
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { bitmap.recycle() }
                Log.e(TAG, "Ошибка создания скриншота", e)
            }
        }
    }

    private fun showScreenshotMessage(message: String) {
        screenshotMessage = message
        viewLifecycleOwner.lifecycleScope.launch {
            delay(SCREENSHOT_MESSAGE_DURATION_MS)
            if (screenshotMessage == message) {
                screenshotMessage = null
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