package com.zhihu.matisse.internal.utils

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/4/2019
 */
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

fun failIllegalArgument(message: String): Nothing = throw IllegalArgumentException(message)

fun failIllegalStateException(message: String): Nothing = throw IllegalStateException(message)