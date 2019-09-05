package com.zhihu.matisse.internal.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
class RoundedRectangleImageView constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    var mRadius = 0F
    lateinit var mRoundedRectPath: Path
    lateinit var mRectF: RectF

    init {
        init(context)
    }

    private fun init(context: Context) {
        val density = context.resources.displayMetrics.density
        mRadius = 2.0f * density
        mRoundedRectPath = Path()
        mRectF = RectF()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mRectF.set(0.0f, 0.0f, measuredWidth.toFloat(), measuredHeight.toFloat())
        mRoundedRectPath.addRoundRect(mRectF, mRadius, mRadius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(mRoundedRectPath)
        super.onDraw(canvas)
    }

}