package com.zhihu.matisse.internal.entity

import android.content.pm.ActivityInfo
import androidx.annotation.StyleRes
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.R
import com.zhihu.matisse.engine.ImageEngine
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.listener.OnCheckedListener
import com.zhihu.matisse.listener.OnSelectedListener

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/4/2019
 */
class SelectionSpec private constructor(){
    var mimeTypeSet: Set<MimeType> = emptySet()
    var mediaTypeExclusive = true
    var showSingleMediaType = false
    @StyleRes var themeId = R.style.Matisse_Zhihu
    var orientation = 0
    var countable = false
    var maxSelectable = 1
    var maxImageSelectable = 0
    var maxVideoSelectable = 0
    var filters: MutableList<Filter>? = null
    var capture = false
    var captureStrategy: CaptureStrategy? = null
    var spanCount = 3
    var gridExpectedSize = 0
    var thumbnailScale = 0.5f
    var imageEngine: ImageEngine = GlideEngine()
    var hasInited = false
    var onSelectedListener: OnSelectedListener? = null
    var originalable = false
    var autoHideToobar = false
    var originalMaxSize = Int.MAX_VALUE
    var onCheckedListener: OnCheckedListener? = null

    companion object {
        fun getInstance(): SelectionSpec {
            return InstanceHolder.INSTANCE
        }

        fun getCleanInstance(): SelectionSpec {
            val selectionSpec = getInstance()
            selectionSpec.reset()
            return selectionSpec
        }
    }

    private fun reset() {
        mimeTypeSet = emptySet()
        mediaTypeExclusive = true
        showSingleMediaType = false
        themeId = R.style.Matisse_Zhihu
        orientation = 0
        countable = false
        maxSelectable = 1
        maxImageSelectable = 0
        maxVideoSelectable = 0
        filters = null
        capture = false
        captureStrategy = null
        spanCount = 3
        gridExpectedSize = 0
        thumbnailScale = 0.5f
        imageEngine = GlideEngine()
        hasInited = true
        originalable = false
        autoHideToobar = false
        originalMaxSize = Integer.MAX_VALUE
    }

    private object InstanceHolder {
        val INSTANCE = SelectionSpec()
    }

    fun singleSelectionModeEnabled() = !countable && (maxSelectable == 1 || (maxImageSelectable == 1 && maxVideoSelectable == 1))

    fun needOrientationRestriction() = orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    fun onlyShowImages() = showSingleMediaType && MimeType.ofImage().containsAll(mimeTypeSet)

    fun onlyShowVideos() = showSingleMediaType && MimeType.ofVideo().containsAll(mimeTypeSet)

    fun onlyShowGif() = showSingleMediaType && MimeType.ofGif() == mimeTypeSet
}