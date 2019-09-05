package com.zhihu.matisse.internal.ui.adapter

import android.content.Context
import android.database.Cursor
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.*
import com.zhihu.matisse.internal.model.SelectedItemCollection
import com.zhihu.matisse.internal.ui.widget.CheckView
import com.zhihu.matisse.internal.ui.widget.CheckView.UNCHECKED
import com.zhihu.matisse.internal.ui.widget.MediaGrid

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/4/2019
 */
private const val VIEW_TYPE_CAPTURE = 0x01
private const val VIEW_TYPE_MEDIA = 0x02

class AlbumMediaAdapter(context: Context, private val selectedCollection: SelectedItemCollection, private val recyclerView: RecyclerView) : RecyclerViewCursorAdapter<RecyclerView.ViewHolder>(null), MediaGrid.OnMediaGridClickListener {
    private var mImageResize = 0
    private var mPlaceholder: Drawable? = null
    private var mSelectionSpec: SelectionSpec = SelectionSpec.getInstance()
    private var mCheckStateListener: CheckStateListener? = null
    private var mOnMediaClickListener: OnMediaClickListener? = null

    init {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(R.attr.item_placeholder))
        mPlaceholder = typedArray.getDrawable(0)
        typedArray.recycle()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_CAPTURE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.photo_capture_item, parent, false)
                val holder = CaptureViewHolder(view)
                holder.itemView.setOnClickListener { v ->
                    if (v.context is OnPhotoCapture) {
                        (v.context as OnPhotoCapture).capture()
                    }
                }
                return holder
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.media_grid_item, parent, false)
                return MediaViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, cursor: Cursor) {
        when (holder) {
            is CaptureViewHolder -> {
                val drawables = holder.mHint.compoundDrawables
                val typedArray = holder.itemView.context.theme.obtainStyledAttributes(intArrayOf(R.attr.capture_textColor))
                val color = typedArray.getColor(0, 0)
                typedArray.recycle()

                drawables.filterNotNull().forEachIndexed { index, it ->
                    val state = it.constantState ?: return@forEachIndexed
                    val newDrawable = state.newDrawable().mutate()
                    newDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                    newDrawable.bounds = it.bounds
                    drawables[index] = newDrawable
                }
                holder.mHint.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3])
            }
            is MediaViewHolder -> {
                val item = valueOfItem(cursor)
                holder.mMediaGrid.preBindMedia(MediaGrid.PreBindInfo(
                        getImageResize(holder.mMediaGrid.context),
                        mPlaceholder,
                        mSelectionSpec.countable,
                        holder)
                )
                holder.mMediaGrid.bindMedia(item)
                holder.mMediaGrid.mListener = this
                setCheckStatus(item, holder.mMediaGrid)
            }
        }
    }

    private fun setCheckStatus(item: Item, mediaGrid: MediaGrid) {
        when {
            mSelectionSpec.countable -> {
                val checkedNum = selectedCollection.checkedNumOf(item)
                when {
                    checkedNum > 0 -> {
                        mediaGrid.setCheckEnabled(true)
                        mediaGrid.setCheckedNum(checkedNum)
                    }
                    else -> {
                        when {
                            selectedCollection.maxSelectableReached() -> {
                                mediaGrid.setCheckEnabled(false)
                                mediaGrid.setCheckedNum(UNCHECKED)
                            }
                            else -> {
                                mediaGrid.setCheckEnabled(true)
                                mediaGrid.setCheckedNum(checkedNum)
                            }
                        }
                    }
                }
            }
            else -> {
                val selected = selectedCollection.isSelected(item)
                when {
                    selected -> {
                        mediaGrid.setCheckEnabled(true)
                        mediaGrid.setChecked(true)
                    }
                    else -> {
                        when {
                            selectedCollection.maxSelectableReached() -> {
                                mediaGrid.setCheckEnabled(false)
                                mediaGrid.setChecked(false)
                            }
                            else -> {
                                mediaGrid.setCheckEnabled(true)
                                mediaGrid.setChecked(false)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onThumbnailClicked(thumbnail: ImageView, item: Item, holder: RecyclerView.ViewHolder) {
        mOnMediaClickListener?.onMediaClick(null, item, holder.adapterPosition)
    }

    override fun onCheckViewClicked(checkView: CheckView, item: Item, holder: RecyclerView.ViewHolder) {
        when {
            mSelectionSpec.countable -> {
                when (selectedCollection.checkedNumOf(item)) {
                    UNCHECKED -> {
                        if (assertAddSelection(holder.itemView.context, item)) {
                            selectedCollection.add(item)
                            notifyCheckStateChanged()
                        }
                    }
                    else -> {
                        selectedCollection.remove(item)
                        notifyCheckStateChanged()
                    }
                }
            }
            else -> {
                when {
                    selectedCollection.isSelected(item) -> {
                        selectedCollection.remove(item)
                        notifyCheckStateChanged()
                    }
                    else -> {
                        if (assertAddSelection(holder.itemView.context, item)) {
                            selectedCollection.add(item)
                            notifyCheckStateChanged()
                        }
                    }
                }
            }
        }
    }

    private fun notifyCheckStateChanged() {
        notifyDataSetChanged()
        mCheckStateListener?.onUpdate()
    }

    override fun getItemViewType(position: Int, cursor: Cursor): Int = if (valueOfItem(cursor).isCapture()) {
        VIEW_TYPE_CAPTURE
    } else {
        VIEW_TYPE_MEDIA
    }

    private fun assertAddSelection(context: Context, item: Item): Boolean {
        val cause = selectedCollection.isAcceptable(item)
        handleCause(context, cause)
        return cause == null
    }

    fun registerCheckStateListener(listener: CheckStateListener) {
        mCheckStateListener = listener
    }

    fun unregisterCheckStateListener() {
        mCheckStateListener = null
    }

    fun registerOnMediaClickListener(listener: OnMediaClickListener) {
        mOnMediaClickListener = listener
    }

    fun unregisterOnMediaClickListener() {
        mOnMediaClickListener = null
    }

    fun refreshSelection() {
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        if (first == -1 || last == -1) {
            return
        }
        val cursor = getCursor()
        for (i in first..last) {
            val holder = recyclerView.findViewHolderForAdapterPosition(first)
            if (holder is MediaViewHolder) {
                if (cursor!!.moveToPosition(i)) {
                    setCheckStatus(valueOfItem(cursor), holder.mMediaGrid)
                }
            }
        }
    }

    private fun getImageResize(context: Context): Int {
        if (mImageResize == 0) {
            val layoutManager = recyclerView.layoutManager
            val spanCount = (layoutManager as GridLayoutManager).spanCount
            val screenWidth = context.resources.displayMetrics.widthPixels
            val availableWidth = screenWidth - context.resources.getDimensionPixelSize(
                    R.dimen.media_grid_spacing
            ) * (spanCount - 1)
            mImageResize = availableWidth / spanCount
            mImageResize = (mImageResize * mSelectionSpec.thumbnailScale).toInt()
        }
        return mImageResize
    }

    interface CheckStateListener {
        fun onUpdate()
    }

    interface OnMediaClickListener {
        fun onMediaClick(album: Album?, item: Item, adapterPosition: Int)
    }

    interface OnPhotoCapture {
        fun capture()
    }

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mMediaGrid: MediaGrid = itemView as MediaGrid
    }

    class CaptureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mHint: TextView = itemView.findViewById(R.id.hint)
    }

}