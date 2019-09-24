package com.zhihu.matisse.internal.ui.widget

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.widget.AdapterView
import android.widget.CursorAdapter
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.valueOfAlbum
import com.zhihu.matisse.internal.utils.hasICS

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */

private const val MAX_SHOWN_COUNT = 6

class AlbumsSpinner constructor(val context: Context) {
    var mAdapter: CursorAdapter? = null
        set(value) {
            mListPopupWindow.setAdapter(value)
            field = value
        }
    private var mSelected: TextView? = null
    var mListPopupWindow: ListPopupWindow
    var mOnItemSelectedListener: AdapterView.OnItemSelectedListener? = null

    init {
        val density = context.resources.displayMetrics.density
        mListPopupWindow = ListPopupWindow(context, null, R.attr.listPopupWindowStyle)
        mListPopupWindow.apply {
            isModal = true
            setContentWidth((216 * density).toInt())
            horizontalOffset = (16 * density).toInt()
            verticalOffset = (-48 * density).toInt()
            setOnItemClickListener { parent, view, position, id ->
                onItemSelected(parent.context, position)
                mOnItemSelectedListener?.onItemSelected(parent, view, position, id)
            }
        }
    }

    fun setSelection(context: Context, position: Int) {
        mListPopupWindow.setSelection(position)
        onItemSelected(context, position)
    }

    private fun onItemSelected(context: Context, position: Int) {
        mListPopupWindow.dismiss()
        val cursor = mAdapter?.cursor
        requireNotNull(cursor)
        cursor.moveToPosition(position)
        val album = valueOfAlbum(cursor)
        val displayName = album.getDisplayName(context)
        if (mSelected!!.visibility == View.VISIBLE) {
            mSelected!!.text = displayName
        } else {
            if (hasICS()) {
                mSelected!!.alpha = 0.0f
                mSelected!!.visibility = View.VISIBLE
                mSelected!!.text = displayName
                mSelected!!.animate().alpha(1.0f).setDuration(context.resources.getInteger(
                        android.R.integer.config_longAnimTime).toLong()).start()
            } else {
                mSelected!!.visibility = View.VISIBLE
                mSelected!!.text = displayName
            }
        }
    }

    fun setSelectedTextView(textView: TextView) {
        mSelected = textView
        // tint dropdown arrow icon
        val drawables = mSelected!!.compoundDrawables
        val right = drawables[2]
        val ta = mSelected!!.context.theme.obtainStyledAttributes(
                intArrayOf(R.attr.album_element_color))
        val color = ta.getColor(0, 0)
        ta.recycle()
        right.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        mSelected!!.visibility = View.GONE
        mSelected!!.setOnClickListener { v ->
            val itemHeight = v.resources.getDimensionPixelSize(R.dimen.album_item_height)
            mListPopupWindow.height = if (mAdapter!!.count > MAX_SHOWN_COUNT)
                itemHeight * MAX_SHOWN_COUNT
            else
                itemHeight * mAdapter!!.count
            mListPopupWindow.show()
        }
        mSelected!!.setOnTouchListener(mListPopupWindow.createDragToOpenListener(mSelected))
    }

    fun setPopupAnchorView(view: View) {
        mListPopupWindow.anchorView = view
    }
}