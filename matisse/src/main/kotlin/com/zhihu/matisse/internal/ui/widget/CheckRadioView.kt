package com.zhihu.matisse.internal.ui.widget

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.zhihu.matisse.R

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
class CheckRadioView(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {
    private var mSelectedColor = context.getColor(R.color.zhihu_item_checkCircle_backgroundColor)
    private var mUnSelectUdColor = context.getColor(R.color.zhihu_check_original_radio_disable)
    private var mDrawable: Drawable? = null

    init {
        init()
    }

    private fun init() {
        mSelectedColor = ResourcesCompat.getColor(
                resources, R.color.zhihu_item_checkCircle_backgroundColor,
                context.theme)
        mUnSelectUdColor = ResourcesCompat.getColor(
                resources, R.color.zhihu_check_original_radio_disable,
                context.theme)
        setChecked(false)
    }

    fun setChecked(enable: Boolean) {
        if (enable) {
            setImageResource(R.drawable.ic_preview_radio_on)
            mDrawable = drawable
            mDrawable?.setColorFilter(mSelectedColor, PorterDuff.Mode.SRC_IN)
        } else {
            setImageResource(R.drawable.ic_preview_radio_off)
            mDrawable = drawable
            mDrawable?.setColorFilter(mUnSelectUdColor, PorterDuff.Mode.SRC_IN)
        }
    }

    fun setColor(color: Int) {
        mDrawable?.let {
            mDrawable = drawable
            setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }
}