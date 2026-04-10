package com.example.drivenui.engine.uirender.renderer

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivenui.app.theme.DrivenUITheme
import com.example.drivenui.engine.generative_screen.models.UiAction
import com.example.drivenui.engine.uirender.models.ComponentModel
import com.example.drivenui.engine.uirender.models.LayoutModel

/**
 * Длинный `verticalFor`: [RecyclerView] + [ComposeView] в ячейке — виртуализация на стороне View,
 * без типичных артефактов быстрого скролла у Compose [androidx.compose.foundation.lazy.LazyColumn].
 *
 * @param modifier Модификатор контейнера списка (как у прежнего LazyColumn).
 */
@Composable
internal fun VerticalForRecyclerList(
    modifier: Modifier,
    forIndexName: String,
    maxForIndex: Int,
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter?,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)?,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val adapter = VerticalForRecyclerAdapter(
                itemCount = maxForIndex,
                forIndexName = forIndexName,
                model = model,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent,
            )
            RecyclerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                layoutManager = LinearLayoutManager(context)
                this.adapter = adapter
                clipToPadding = true
                itemAnimator = null
                adapter.attachRecyclerView(this)
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as VerticalForRecyclerAdapter).update(
                count = maxForIndex,
                forIndexName = forIndexName,
                model = model,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent,
            )
        },
    )
}

/**
 * Длинный `horizontalFor`: [RecyclerView] с горизонтальным [LinearLayoutManager].
 */
@Composable
internal fun HorizontalForRecyclerList(
    modifier: Modifier,
    forIndexName: String,
    maxForIndex: Int,
    model: LayoutModel,
    onActions: (List<UiAction>) -> Unit,
    onWidgetValueChange: WidgetValueSetter?,
    applyBindingsForComponent: ((ComponentModel) -> ComponentModel)?,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val adapter = HorizontalForRecyclerAdapter(
                itemCount = maxForIndex,
                forIndexName = forIndexName,
                model = model,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent,
            )
            RecyclerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = adapter
                clipToPadding = true
                itemAnimator = null
                adapter.attachRecyclerView(this)
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as HorizontalForRecyclerAdapter).update(
                count = maxForIndex,
                forIndexName = forIndexName,
                model = model,
                onActions = onActions,
                onWidgetValueChange = onWidgetValueChange,
                applyBindingsForComponent = applyBindingsForComponent,
            )
        },
    )
}

private class VerticalForViewHolder(
    private val composeView: ComposeView,
) : RecyclerView.ViewHolder(composeView) {

    fun bind(
        index: Int,
        forIndexName: String,
        model: LayoutModel,
        onActions: (List<UiAction>) -> Unit,
        onWidgetValueChange: WidgetValueSetter?,
        applyBindingsForComponent: ((ComponentModel) -> ComponentModel)?,
    ) {
        composeView.setContent {
            DrivenUITheme {
                VerticalForRowContent(
                    index = index,
                    forIndexName = forIndexName,
                    model = model,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                    applyBindingsForComponent = applyBindingsForComponent,
                )
            }
        }
    }
}

private class HorizontalForViewHolder(
    private val composeView: ComposeView,
) : RecyclerView.ViewHolder(composeView) {

    fun bind(
        index: Int,
        forIndexName: String,
        model: LayoutModel,
        onActions: (List<UiAction>) -> Unit,
        onWidgetValueChange: WidgetValueSetter?,
        applyBindingsForComponent: ((ComponentModel) -> ComponentModel)?,
    ) {
        composeView.setContent {
            DrivenUITheme {
                HorizontalForRowContent(
                    index = index,
                    forIndexName = forIndexName,
                    model = model,
                    onActions = onActions,
                    onWidgetValueChange = onWidgetValueChange,
                    applyBindingsForComponent = applyBindingsForComponent,
                )
            }
        }
    }
}

private class VerticalForRecyclerAdapter(
    private var itemCount: Int,
    private var forIndexName: String,
    private var model: LayoutModel,
    private var onActions: (List<UiAction>) -> Unit,
    private var onWidgetValueChange: WidgetValueSetter?,
    private var applyBindingsForComponent: ((ComponentModel) -> ComponentModel)?,
) : RecyclerView.Adapter<VerticalForViewHolder>() {

    private var recyclerView: RecyclerView? = null

    fun attachRecyclerView(rv: RecyclerView) {
        recyclerView = rv
    }

    fun update(
        count: Int,
        forIndexName: String,
        model: LayoutModel,
        onActions: (List<UiAction>) -> Unit,
        onWidgetValueChange: WidgetValueSetter?,
        applyBindingsForComponent: ((ComponentModel) -> ComponentModel)?,
    ) {
        val countChanged = count != itemCount
        val modelOrIndexChanged =
            model !== this.model || forIndexName != this.forIndexName
        val callbacksChanged =
            onActions !== this.onActions ||
                onWidgetValueChange !== this.onWidgetValueChange ||
                applyBindingsForComponent !== this.applyBindingsForComponent

        itemCount = count
        this.forIndexName = forIndexName
        this.model = model
        this.onActions = onActions
        this.onWidgetValueChange = onWidgetValueChange
        this.applyBindingsForComponent = applyBindingsForComponent

        when {
            countChanged -> notifyDataSetChanged()
            modelOrIndexChanged -> notifyItemRangeChanged(0, itemCount)
            callbacksChanged -> notifyVisibleItemsChanged()
        }
    }

    private fun notifyVisibleItemsChanged() {
        val rv = recyclerView ?: return
        fun runNotify() {
            val lm = rv.layoutManager as? LinearLayoutManager ?: return
            val first = lm.findFirstVisibleItemPosition()
            val last = lm.findLastVisibleItemPosition()
            if (first == RecyclerView.NO_POSITION || last == RecyclerView.NO_POSITION) {
                return
            }
            notifyItemRangeChanged(first, last - first + 1)
        }
        if (rv.isLayoutRequested || !rv.isLaidOut) {
            rv.post { runNotify() }
        } else {
            runNotify()
        }
    }

    override fun getItemCount(): Int = itemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalForViewHolder {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        }
        return VerticalForViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: VerticalForViewHolder, position: Int) {
        holder.bind(
            index = position,
            forIndexName = forIndexName,
            model = model,
            onActions = onActions,
            onWidgetValueChange = onWidgetValueChange,
            applyBindingsForComponent = applyBindingsForComponent,
        )
    }
}

private class HorizontalForRecyclerAdapter(
    private var itemCount: Int,
    private var forIndexName: String,
    private var model: LayoutModel,
    private var onActions: (List<UiAction>) -> Unit,
    private var onWidgetValueChange: WidgetValueSetter?,
    private var applyBindingsForComponent: ((ComponentModel) -> ComponentModel)?,
) : RecyclerView.Adapter<HorizontalForViewHolder>() {

    private var recyclerView: RecyclerView? = null

    fun attachRecyclerView(rv: RecyclerView) {
        recyclerView = rv
    }

    fun update(
        count: Int,
        forIndexName: String,
        model: LayoutModel,
        onActions: (List<UiAction>) -> Unit,
        onWidgetValueChange: WidgetValueSetter?,
        applyBindingsForComponent: ((ComponentModel) -> ComponentModel)?,
    ) {
        val countChanged = count != itemCount
        val modelOrIndexChanged =
            model !== this.model || forIndexName != this.forIndexName
        val callbacksChanged =
            onActions !== this.onActions ||
                onWidgetValueChange !== this.onWidgetValueChange ||
                applyBindingsForComponent !== this.applyBindingsForComponent

        itemCount = count
        this.forIndexName = forIndexName
        this.model = model
        this.onActions = onActions
        this.onWidgetValueChange = onWidgetValueChange
        this.applyBindingsForComponent = applyBindingsForComponent

        when {
            countChanged -> notifyDataSetChanged()
            modelOrIndexChanged -> notifyItemRangeChanged(0, itemCount)
            callbacksChanged -> notifyVisibleItemsChanged()
        }
    }

    private fun notifyVisibleItemsChanged() {
        val rv = recyclerView ?: return
        fun runNotify() {
            val lm = rv.layoutManager as? LinearLayoutManager ?: return
            val first = lm.findFirstVisibleItemPosition()
            val last = lm.findLastVisibleItemPosition()
            if (first == RecyclerView.NO_POSITION || last == RecyclerView.NO_POSITION) {
                return
            }
            notifyItemRangeChanged(first, last - first + 1)
        }
        if (rv.isLayoutRequested || !rv.isLaidOut) {
            rv.post { runNotify() }
        } else {
            runNotify()
        }
    }

    override fun getItemCount(): Int = itemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalForViewHolder {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        }
        return HorizontalForViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: HorizontalForViewHolder, position: Int) {
        holder.bind(
            index = position,
            forIndexName = forIndexName,
            model = model,
            onActions = onActions,
            onWidgetValueChange = onWidgetValueChange,
            applyBindingsForComponent = applyBindingsForComponent,
        )
    }
}
