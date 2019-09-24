package com.zhihu.matisse.internal.ui

import android.app.Activity
import android.database.Cursor
import android.os.Bundle
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.entity.valueOfItem
import com.zhihu.matisse.internal.model.AlbumMediaCollection
import com.zhihu.matisse.internal.ui.adapter.PreviewPagerAdapter
import kotlinx.android.synthetic.main.activity_media_preview.*

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */

const val EXTRA_ITEM = "extra_item"

class AlbumPreviewActivity : BasePreviewActivity(), AlbumMediaCollection.AlbumMediaCallbacks {

    private var mCollection: AlbumMediaCollection = AlbumMediaCollection()
    private var mIsAlreadySetPosition = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SelectionSpec.getInstance().hasInited) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        mCollection.onCreate(this, this)
        val album = intent.getParcelableExtra<Album>(EXTRA_ALBUM)
        mCollection.load(album, false)

        val item = intent.getParcelableExtra<Item>(EXTRA_ITEM)
        if (mSpec.countable) {
            check_view.mCheckedNum = mSelectedCollection.checkedNumOf(item)
        } else {
            check_view.mChecked = mSelectedCollection.isSelected(item)
        }
        updateSize(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        mCollection.onDestroy()
    }

    override fun onAlbumMediaLoad(cursor: Cursor?) {
        val items = arrayListOf<Item>()
        while (cursor!!.moveToNext()) {
            items.add(valueOfItem(cursor))
        }
        if (items.isEmpty()) {
            return
        }
        val adapter = pager.adapter as PreviewPagerAdapter
        adapter.addAll(items)
        adapter.notifyDataSetChanged()
        if (!mIsAlreadySetPosition) {
            //onAlbumMediaLoad is called many times..
            mIsAlreadySetPosition = true
            val selected = intent.getParcelableExtra<Item>(EXTRA_ITEM)
            val selectedIndex = items.indexOf(selected)
            pager.setCurrentItem(selectedIndex, false)
            mPreviousPos = selectedIndex
        }
    }

    override fun onAlbumMediaReset() {
    }
}