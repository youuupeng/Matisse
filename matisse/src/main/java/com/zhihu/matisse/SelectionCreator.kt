package com.zhihu.matisse

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.annotation.StyleRes
import com.zhihu.matisse.engine.ImageEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.listener.OnCheckedListener
import com.zhihu.matisse.listener.OnSelectedListener
import com.zhihu.matisse.ui.MatisseActivity
import org.jetbrains.anko.intentFor
import java.util.*

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/6/2019
 */
class SelectionCreator constructor(private val matisse: Matisse, mimeTypes: Set<MimeType>, mediaTypeExclusive: Boolean) {
    private var mSelectionSpec: SelectionSpec = SelectionSpec.getCleanInstance()

    /**
     * Constructs a new specification builder on the context.
     *
     * @param mimeTypes MIME type set to select.
     */
    init {
        mSelectionSpec.apply {
            mimeTypeSet = mimeTypes
            this.mediaTypeExclusive = mediaTypeExclusive
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    enum class ScreenOrientation(val value: Int) {
        SCREEN_ORIENTATION_UNSPECIFIED(-1),
        SCREEN_ORIENTATION_LANDSCAPE(0),
        SCREEN_ORIENTATION_PORTRAIT(1),
        SCREEN_ORIENTATION_USER(2),
        SCREEN_ORIENTATION_BEHIND(3),
        SCREEN_ORIENTATION_SENSOR(4),
        SCREEN_ORIENTATION_NOSENSOR(5),
        SCREEN_ORIENTATION_SENSOR_LANDSCAPE(6),
        SCREEN_ORIENTATION_SENSOR_PORTRAIT(7),
        SCREEN_ORIENTATION_REVERSE_LANDSCAPE(8),
        SCREEN_ORIENTATION_REVERSE_PORTRAIT(9),
        SCREEN_ORIENTATION_FULL_SENSOR(10),
        SCREEN_ORIENTATION_USER_LANDSCAPE(11),
        SCREEN_ORIENTATION_USER_PORTRAIT(12),
        SCREEN_ORIENTATION_FULL_USER(13),
        SCREEN_ORIENTATION_LOCKED(14);
    }

    /**
     * Whether to show only one media type if choosing medias are only images or videos.
     *
     * @param showSingleMediaType whether to show only one media type, either images or videos.
     * @return {@link SelectionCreator} for fluent API.
     * @see SelectionSpec#onlyShowImages()
     * @see SelectionSpec#onlyShowVideos()
     */
    fun showSingleMediaType(showSingleMediaType: Boolean): SelectionCreator {
        mSelectionSpec.showSingleMediaType = showSingleMediaType
        return this
    }

    /**
     * Theme for media selecting Activity.
     * <p>
     * There are two built-in themes:
     * 1. com.zhihu.matisse.R.style.Matisse_Zhihu;
     * 2. com.zhihu.matisse.R.style.Matisse_Dracula
     * you can define a custom theme derived from the above ones or other themes.
     *
     * @param themeId theme resource id. Default value is com.zhihu.matisse.R.style.Matisse_Zhihu.
     * @return {@link SelectionCreator} for fluent API.
     */
    fun theme(@StyleRes themeId: Int): SelectionCreator {
        mSelectionSpec.themeId = themeId
        return this
    }

    /**
     * Show a auto-increased number or a check mark when user select media.
     *
     * @param countable true for a auto-increased number from 1, false for a check mark. Default
     *                  value is false.
     * @return {@link SelectionCreator} for fluent API.
     */
    fun countable(countable: Boolean): SelectionCreator {
        mSelectionSpec.countable = countable
        return this
    }

    /**
     * Maximum selectable count.
     *
     * @param maxSelectable Maximum selectable count. Default value is 1.
     * @return {@link SelectionCreator} for fluent API.
     */
    fun maxSelectable(maxSelectable: Int): SelectionCreator {
        require(maxSelectable >= 1) { "maxSelectable must be greater than or equal to one" }
        check(!(mSelectionSpec.maxImageSelectable > 0 || mSelectionSpec.maxVideoSelectable > 0)) { "already set maxImageSelectable and maxVideoSelectable" }
        mSelectionSpec.maxSelectable = maxSelectable
        return this
    }

    /**
     * Only useful when {@link SelectionSpec#mediaTypeExclusive} set true and you want to set different maximum
     * selectable files for image and video media types.
     *
     * @param maxImageSelectable Maximum selectable count for image.
     * @param maxVideoSelectable Maximum selectable count for video.
     * @return {@link SelectionCreator} for fluent API.
     */
    fun maxSelectablePerMediaType(maxImageSelectable: Int, maxVideoSelectable: Int): SelectionCreator {
        require(!(maxImageSelectable < 1 || maxVideoSelectable < 1)) { "max selectable must be greater than or equal to one" }
        mSelectionSpec.maxSelectable = -1
        mSelectionSpec.maxImageSelectable = maxImageSelectable
        mSelectionSpec.maxVideoSelectable = maxVideoSelectable
        return this
    }

    /**
     * Add filter to filter each selecting item.
     *
     * @param filter {@link Filter}
     * @return {@link SelectionCreator} for fluent API.
     */
    fun addFilter(filter: Filter): SelectionCreator {
        if (mSelectionSpec.filters == null) {
            mSelectionSpec.filters = ArrayList()
        }
        mSelectionSpec.filters!!.add(filter)
        return this
    }

    /**
     * Determines whether the photo capturing is enabled or not on the media grid view.
     * <p>
     * If this value is set true, photo capturing entry will appear only on All Media's page.
     *
     * @param enable Whether to enable capturing or not. Default value is false;
     * @return {@link SelectionCreator} for fluent API.
     */
    fun capture(enable: Boolean): SelectionCreator {
        mSelectionSpec.capture = enable
        return this
    }

    /**
     * Show a original photo check options.Let users decide whether use original photo after select
     *
     * @param enable Whether to enable original photo or not
     * @return {@link SelectionCreator} for fluent API.
     */
    fun originalEnable(enable: Boolean): SelectionCreator {
        mSelectionSpec.originalable = enable
        return this
    }

    /**
     * Determines Whether to hide top and bottom toolbar in PreView mode ,when user tap the picture
     *
     * @param enable
     * @return {@link SelectionCreator} for fluent API.
     */
    fun autoHideToolbarOnSingleTap(enable: Boolean): SelectionCreator {
        mSelectionSpec.autoHideToobar = enable
        return this
    }

    /**
     * Maximum original size,the unit is MB. Only useful when {link@originalEnable} set true
     *
     * @param size Maximum original size. Default value is Integer.MAX_VALUE
     * @return {@link SelectionCreator} for fluent API.
     */
    fun maxOriginalSize(size: Int): SelectionCreator {
        mSelectionSpec.originalMaxSize = size
        return this
    }

    /**
     * Capture strategy provided for the location to save photos including internal and external
     * storage and also a authority for {@link androidx.core.content.FileProvider}.
     *
     * @param captureStrategy {@link CaptureStrategy}, needed only when capturing is enabled.
     * @return {@link SelectionCreator} for fluent API.
     */
    fun captureStrategy(captureStrategy: CaptureStrategy): SelectionCreator {
        mSelectionSpec.captureStrategy = captureStrategy
        return this
    }

    /**
     * Set the desired orientation of this activity.
     *
     * @param orientation An orientation constant as used in {@link ScreenOrientation}.
     *                    Default value is {@link android.content.pm.ActivityInfo#SCREEN_ORIENTATION_PORTRAIT}.
     * @return {@link SelectionCreator} for fluent API.
     * @see Activity#setRequestedOrientation(int)
     */
    fun restrictOrientation(orientation: ScreenOrientation): SelectionCreator {
        mSelectionSpec.orientation = orientation.value
        return this
    }

    /**
     * Set a fixed span count for the media grid. Same for different screen orientations.
     * <p>
     * This will be ignored when {@link #gridExpectedSize(int)} is set.
     *
     * @param spanCount Requested span count.
     * @return {@link SelectionCreator} for fluent API.
     */
    fun spanCount(spanCount: Int): SelectionCreator {
        require(spanCount >= 1) { "spanCount cannot be less than 1" }
        mSelectionSpec.spanCount = spanCount
        return this
    }

    /**
     * Set expected size for media grid to adapt to different screen sizes. This won't necessarily
     * be applied cause the media grid should fill the view container. The measured media grid's
     * size will be as close to this value as possible.
     *
     * @param size Expected media grid size in pixel.
     * @return {@link SelectionCreator} for fluent API.
     */
    fun gridExpectedSize(size: Int): SelectionCreator {
        mSelectionSpec.gridExpectedSize = size
        return this
    }

    /**
     * Photo thumbnail's scale compared to the View's size. It should be a float value in (0.0,
     * 1.0].
     *
     * @param scale Thumbnail's scale in (0.0, 1.0]. Default value is 0.5.
     * @return {@link SelectionCreator} for fluent API.
     */
    fun thumbnailScale(scale: Float): SelectionCreator {
        require(!(scale <= 0f || scale > 1f)) { "Thumbnail scale must be between (0.0, 1.0]" }
        mSelectionSpec.thumbnailScale = scale
        return this
    }

    /**
     * Provide an image engine.
     * <p>
     * There are two built-in image engines:
     * 1. {@link com.zhihu.matisse.engine.impl.GlideEngine}
     * 2. {@link com.zhihu.matisse.engine.impl.PicassoEngine}
     * And you can implement your own image engine.
     *
     * @param imageEngine {@link ImageEngine}
     * @return {@link SelectionCreator} for fluent API.
     */
    fun imageEngine(imageEngine: ImageEngine): SelectionCreator {
        mSelectionSpec.imageEngine = imageEngine
        return this
    }

    /**
     * Set listener for callback immediately when user select or unselect something.
     * <p>
     * It's a redundant API with {@link Matisse#obtainResult(Intent)},
     * we only suggest you to use this API when you need to do something immediately.
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link SelectionCreator} for fluent API.
     */
    fun setOnSelectedListener(listener: OnSelectedListener?): SelectionCreator {
        mSelectionSpec.onSelectedListener = listener
        return this
    }

    /**
     * Set listener for callback immediately when user check or uncheck original.
     *
     * @param listener [OnSelectedListener]
     * @return [SelectionCreator] for fluent API.
     */
    fun setOnCheckedListener(listener: OnCheckedListener?): SelectionCreator {
        mSelectionSpec.onCheckedListener = listener
        return this
    }

    /**
     * Start to select media and wait for result.
     *
     * @param requestCode Identity of the request Activity or Fragment.
     */
    fun forResult(requestCode: Int) {
        val activity = matisse.getActivity() ?: return
        val intent = activity.intentFor<MatisseActivity>()
        val fragment = matisse.getFragment()
        fragment?.startActivityForResult(intent, requestCode)
                ?: activity.startActivityForResult(intent, requestCode)
    }
}