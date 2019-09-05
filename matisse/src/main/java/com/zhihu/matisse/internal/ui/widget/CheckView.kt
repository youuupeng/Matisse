package com.zhihu.matisse.internal.ui.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.zhihu.matisse.R

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */

const val UNCHECKED = Int.MIN_VALUE
private const val STROKE_WIDTH = 3.0f // dp
private const val SHADOW_WIDTH = 6.0f // dp
private const val SIZE = 48 // dp
private const val STROKE_RADIUS = 11.5f // dp
private const val BG_RADIUS = 11.0f // dp
private const val CONTENT_SIZE = 16 // dp

class CheckView : View {
    var mCountable = true
    var mChecked = false
        set(value) {
            check(!mCountable) { "CheckView is countable, call setCheckedNum() instead." }
            field = value
            invalidate()
        }
    private var mEnabled = true
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var mCheckedNum = 0
        set(value) {
            check(mCountable) { "CheckView is not countable, call setChecked() instead." }
            require(!(value != UNCHECKED && value <= 0)) { "checked num can't be negative." }
            field = value
            invalidate()
        }
    private var mDensity = 0f

    private lateinit var mStrokePaint: Paint
    private var mBackgroundPaint: Paint? = null
    private var mTextPaint: TextPaint? = null
    private var mShadowPaint: Paint? = null
    private lateinit var mCheckDrawable: Drawable
    private var mCheckRect: Rect? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init(context)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        init(context)
    }

    fun init(context: Context) {
        mDensity = context.resources.displayMetrics.density
        mStrokePaint = Paint()
        mStrokePaint.isAntiAlias = true
        mStrokePaint.style = Paint.Style.STROKE
        mStrokePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        mStrokePaint.strokeWidth = STROKE_WIDTH * mDensity
        val typeArray = getContext().theme.obtainStyledAttributes(intArrayOf(R.attr.item_checkCircle_borderColor))
        val defaultColor = resources.getColor(R.color.zhihu_item_checkCircle_borderColor, context.theme)
        val color = typeArray.getColor(0, defaultColor)
        typeArray.recycle()
        mStrokePaint.color = color
        mCheckDrawable = context.resources.getDrawable(R.drawable.ic_check_white_18dp, context.theme)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // fixed size 48dp x 48dp
        val sizeSpec = MeasureSpec.makeMeasureSpec((SIZE * mDensity).toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(sizeSpec, sizeSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // draw outer and inner shadow
        initShadowPaint()
        canvas.drawCircle(SIZE * mDensity / 2, SIZE * mDensity / 2, (STROKE_RADIUS + STROKE_WIDTH / 2 + SHADOW_WIDTH) * mDensity, mShadowPaint!!)

        // draw white stroke
        canvas.drawCircle(SIZE * mDensity / 2, SIZE * mDensity / 2, STROKE_RADIUS * mDensity, mStrokePaint)

        // draw content
        if (mCountable) {
            if (mCheckedNum != UNCHECKED) {
                initBackgroundPaint()
                canvas.drawCircle(SIZE * mDensity / 2, SIZE * mDensity / 2,
                        BG_RADIUS * mDensity, mBackgroundPaint!!)
                initTextPaint()
                val text = mCheckedNum.toString()
                val baseX = (width - mTextPaint!!.measureText(text)) / 2
                val baseY = (height - mTextPaint!!.descent() - mTextPaint!!.ascent()) / 2
                canvas.drawText(text, baseX, baseY, mTextPaint!!)
            }
        } else {
            if (mChecked) {
                initBackgroundPaint()
                canvas.drawCircle(SIZE * mDensity / 2, SIZE * mDensity / 2, BG_RADIUS * mDensity, mBackgroundPaint!!)
                mCheckDrawable.bounds = getCheckRect()
                mCheckDrawable.draw(canvas)
            }
        }

        // enable hint
        alpha = if (mEnabled) {
            1.0f
        } else {
            0.5f
        }
    }

    private fun initShadowPaint() {
        if (mShadowPaint == null) {
            mShadowPaint = Paint()
            mShadowPaint!!.isAntiAlias = true
            // all in dp
            val outerRadius = STROKE_RADIUS + STROKE_WIDTH / 2
            val innerRadius = outerRadius - STROKE_WIDTH
            val gradientRadius = outerRadius + SHADOW_WIDTH
            val stop0 = (innerRadius - SHADOW_WIDTH) / gradientRadius
            val stop1 = innerRadius / gradientRadius
            val stop2 = outerRadius / gradientRadius
            val stop3 = 1.0f
            mShadowPaint!!.shader = RadialGradient(SIZE * mDensity / 2,
                    SIZE * mDensity / 2, gradientRadius * mDensity,
                    intArrayOf(Color.parseColor("#00000000"), Color.parseColor("#0D000000"),
                            Color.parseColor("#0D000000"), Color.parseColor("#00000000")),
                    floatArrayOf(stop0, stop1, stop2, stop3),
                    Shader.TileMode.CLAMP)
        }
    }

    private fun initBackgroundPaint() {
        if (mBackgroundPaint == null) {
            mBackgroundPaint = Paint()
            mBackgroundPaint!!.isAntiAlias = true
            mBackgroundPaint!!.style = Paint.Style.FILL
            val typedArray = context.theme.obtainStyledAttributes(intArrayOf(R.attr.item_checkCircle_backgroundColor))
            val defaultColor = resources.getColor(R.color.zhihu_item_checkCircle_backgroundColor, context.theme)
            val color = typedArray.getColor(0, defaultColor)
            typedArray.recycle()
            mBackgroundPaint!!.color = color
        }
    }

    private fun initTextPaint() {
        if (mTextPaint == null) {
            mTextPaint = TextPaint()
            mTextPaint!!.isAntiAlias = true
            mTextPaint!!.color = Color.WHITE
            mTextPaint!!.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            mTextPaint!!.textSize = 12.0f * mDensity
        }
    }

    // rect for drawing checked number or mark
    private fun getCheckRect(): Rect {
        if (mCheckRect == null) {
            val rectPadding = (SIZE * mDensity / 2 - CONTENT_SIZE * mDensity / 2).toInt()
            mCheckRect = Rect(rectPadding, rectPadding, (SIZE * mDensity - rectPadding).toInt(), (SIZE * mDensity - rectPadding).toInt())
        }
        return mCheckRect!!
    }

}