package com.zhihu.matisse.internal.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import it.sephiroth.android.library.imagezoom.ImageViewTouch

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
class PreviewViewPager(context: Context, attributeSet: AttributeSet) : ViewPager(context, attributeSet) {

    override fun canScroll(v: View?, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        if (v is ImageViewTouch) {
            return v.canScroll(dx) || super.canScroll(v, checkV, dx, x, y)
        }
        return super.canScroll(v, checkV, dx, x, y)
    }
}