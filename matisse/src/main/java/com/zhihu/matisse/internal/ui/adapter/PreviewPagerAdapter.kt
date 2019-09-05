package com.zhihu.matisse.internal.ui.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.ui.newPreviewItemInstance

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */

class PreviewPagerAdapter(manager: FragmentManager, private val listener: OnPrimaryItemSetListener?) : FragmentPagerAdapter(manager) {
    private var mItems = arrayListOf<Item>()

    override fun getItem(position: Int): Fragment = newPreviewItemInstance(mItems[position])

    override fun getCount(): Int = mItems.size

    //kotlin
    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        listener?.onPrimaryItemSet(position)
    }

    fun getMediaItem(position: Int) = mItems[position]

    fun addAll(items: List<Item>) {
        mItems.addAll(items)
    }

    interface OnPrimaryItemSetListener {

        fun onPrimaryItemSet(position: Int)
    }
}