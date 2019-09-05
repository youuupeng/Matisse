package com.zhihu.matisse

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.zhihu.matisse.ui.MatisseActivity
import com.zhihu.matisse.ui.MatisseActivity.EXTRA_RESULT_SELECTION_PATH
import java.lang.ref.WeakReference

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
class Matisse {
    private var mContext: WeakReference<Activity>? = null
    private var mFragment: WeakReference<Fragment?>? = null

    private constructor(activity: Activity) : this(activity, null)

    private constructor(fragment: Fragment) : this(fragment.activity as Activity, fragment)

    private constructor(activity: Activity, fragment: Fragment?) {
        mContext = WeakReference(activity)
        mFragment = WeakReference(fragment)
    }

    companion object {
        /**
         * Start Matisse from an Activity.
         * <p>
         * This Activity's {@link Activity#onActivityResult(int, int, Intent)} will be called when user
         * finishes selecting.
         *
         * @param activity Activity instance.
         * @return Matisse instance.
         */
        fun from(activity: Activity): Matisse = Matisse(activity)

        /**
         * Start Matisse from a Fragment.
         * <p>
         * This Fragment's {@link Fragment#onActivityResult(int, int, Intent)} will be called when user
         * finishes selecting.
         *
         * @param fragment Fragment instance.
         * @return Matisse instance.
         */
        fun from(fragment: Fragment) = Matisse(fragment)

        /**
         * Obtain user selected media' {@link Uri} list in the starting Activity or Fragment.
         *
         * @param data Intent passed by {@link Activity#onActivityResult(int, int, Intent)} or
         *             {@link Fragment#onActivityResult(int, int, Intent)}.
         * @return User selected media' {@link Uri} list.
         */
        fun obtainResult(data: Intent): List<Uri> = data.getParcelableArrayListExtra(MatisseActivity.EXTRA_RESULT_SELECTION)

        /**
         * Obtain user selected media path list in the starting Activity or Fragment.
         *
         * @param data Intent passed by {@link Activity#onActivityResult(int, int, Intent)} or
         *             {@link Fragment#onActivityResult(int, int, Intent)}.
         * @return User selected media path list.
         */
        fun obtainPathResult(data: Intent): List<String> = data.getStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH)

        /**
         * Obtain state whether user decide to use selected media in original
         *
         * @param data Intent passed by [Activity.onActivityResult] or
         * [Fragment.onActivityResult].
         * @return Whether use original photo
         */
        fun obtainOriginalState(data: Intent) = data.getBooleanExtra(MatisseActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false)
    }

    /**
     * MIME types the selection constrains on.
     * <p>
     * Types not included in the set will still be shown in the grid but can't be chosen.
     *
     * @param mimeTypes MIME types set user can choose from.
     * @return {@link SelectionCreator} to build select specifications.
     * @see MimeType
     * @see SelectionCreator
     */
    fun choose(mimeTypes: Set<MimeType>): SelectionCreator = this.choose(mimeTypes, true)

    /**
     * MIME types the selection constrains on.
     * <p>
     * Types not included in the set will still be shown in the grid but can't be chosen.
     *
     * @param mimeTypes          MIME types set user can choose from.
     * @param mediaTypeExclusive Whether can choose images and videos at the same time during one single choosing
     *                           process. true corresponds to not being able to choose images and videos at the same
     *                           time, and false corresponds to being able to do this.
     * @return {@link SelectionCreator} to build select specifications.
     * @see MimeType
     * @see SelectionCreator
     */
    fun choose(mimeTypes: Set<MimeType>, mediaTypeExclusive: Boolean) = SelectionCreator(this, mimeTypes, mediaTypeExclusive)

    fun getActivity(): Activity? = mContext!!.get()

    fun getFragment(): Fragment? = if (mFragment != null) {
        mFragment!!.get()
    } else {
        null
    }
}