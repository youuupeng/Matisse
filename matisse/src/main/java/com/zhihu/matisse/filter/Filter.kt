package com.zhihu.matisse.filter

import android.content.Context
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.internal.entity.IncapableCause
import com.zhihu.matisse.internal.entity.Item

/**
 * Description
 * <p>
 *     Filter for choosing a {@link Item}. You can add multiple Filters through
 *     {@link SelectionCreator#addFilter(Filter)}.
 *
 * @author peyo
 * @date 9/4/2019
 */

/**
 * Convenient constant for a minimum value.
 */
const val MIN = 0
/**
 * Convenient constant for a maximum value.
 */
const val MAX = Integer.MAX_VALUE
/**
 * Convenient constant for 1024.
 */
const val K = 1024

abstract class Filter {

    /**
     * Against what mime types this filter applies.
     */
    protected abstract fun constraintTypes(): Set<MimeType>

    /**
     * Invoked for filtering each item.
     *
     * @return null if selectable, {@link IncapableCause} if not selectable.
     */
    abstract fun filter(context: Context, item: Item): IncapableCause?

    /**
     * Whether an {@link Item} need filtering.
     */
    protected fun needFiltering(context: Context, item: Item): Boolean {
        constraintTypes().forEach {
            if (it.checkType(context.contentResolver, item.uri)) {
                return true
            }
        }
        return false
    }
}