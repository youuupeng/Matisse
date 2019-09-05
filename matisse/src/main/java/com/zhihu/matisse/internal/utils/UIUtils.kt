package com.zhihu.matisse.internal.utils

import android.content.Context
import kotlin.math.roundToInt

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */

fun spanCount(context: Context, gridExpectedSize: Int): Int {
    val screenWidth = context.resources.displayMetrics.widthPixels
    val expected = screenWidth.toFloat() / gridExpectedSize.toFloat()
    var spanCount = expected.roundToInt()
    if (spanCount == 0) {
        spanCount = 1
    }
    return spanCount
}

class UIUtils {
}