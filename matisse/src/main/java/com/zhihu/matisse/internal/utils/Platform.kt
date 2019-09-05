package com.zhihu.matisse.internal.utils

import android.os.Build

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
fun hasICS() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH

fun hasKitKat() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
class Platform {
}