package com.zhihu.matisse.internal.ui.adapter

import android.database.Cursor
import android.provider.MediaStore
import androidx.recyclerview.widget.RecyclerView
import com.zhihu.matisse.internal.utils.failIllegalStateException

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
abstract class RecyclerViewCursorAdapter<VH : RecyclerView.ViewHolder> constructor(cursor: Cursor?) : RecyclerView.Adapter<VH>()  {
    private var mCursor: Cursor? = null
    private var mRowIDColumn = 0

    init {
        setHasStableIds(true)
        swapCursor(cursor)
    }

    protected abstract fun onBindViewHolder(holder: VH, cursor: Cursor)

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (!isDataValid(mCursor)) {
            failIllegalStateException("Cannot bind view holder when cursor is in invalid state.")
        }
        if (!mCursor!!.moveToPosition(position)) {
            failIllegalStateException("Could not move cursor to position $position when trying to bind view holder")
        }
        onBindViewHolder(holder, mCursor!!)
    }

    protected abstract fun getItemViewType(position: Int, cursor: Cursor): Int

    override fun getItemCount() = if (isDataValid(mCursor)) {
        mCursor!!.count
    } else {
        0
    }

    override fun getItemId(position: Int): Long {
        if (!isDataValid(mCursor)) {
            failIllegalStateException("Cannot lookup item id when cursor is in invalid state.")
        }
        if (!mCursor!!.moveToPosition(position)) {
            failIllegalStateException("Could not move cursor to position $position  when trying to get an item id")
        }
        return mCursor!!.getLong(mRowIDColumn)
    }

    fun swapCursor(newCursor: Cursor?) {
        if (newCursor == mCursor) {
            return
        }
        newCursor?.let {
            mCursor = it
            mRowIDColumn = mCursor!!.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            // notify the observers about the new cursor
            notifyDataSetChanged()
        } ?: clearData()
    }

    //kotlin optimization 直接放在elvis符后作为一个函数体
    private fun clearData() {
        notifyItemRangeRemoved(0, itemCount)
        mCursor = null
        mRowIDColumn = -1
    }

    private fun isDataValid(cursor: Cursor?) = cursor != null && !cursor.isClosed

    fun getCursor(): Cursor? {
        return mCursor
    }

}