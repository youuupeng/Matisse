package com.zhihu.matisse.internal.utils

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/4/2019
 */

fun failIllegalArgument(message: String): Nothing = throw IllegalArgumentException(message)

fun failIllegalStateException(message: String): Nothing = throw IllegalStateException(message)