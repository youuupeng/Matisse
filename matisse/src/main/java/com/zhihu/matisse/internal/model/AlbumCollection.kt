package com.zhihu.matisse.internal.model

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.zhihu.matisse.internal.loader.AlbumLoader
import java.lang.ref.WeakReference

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/4/2019
 */
private const val LOADER_ID = 1
private const val STATE_CURRENT_SELECTION = "state_current_selection"

class AlbumCollection : LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var mContext: WeakReference<Context>
    private var mLoaderManager: LoaderManager? = null
    private var mLoadFinished = false
    var mCurrentSelection = 0
    private var mCallbacks: AlbumCallbacks? = null

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val context = requireNotNull(mContext.get()) { "context is null." }
        mLoadFinished = false
        return AlbumLoader.newInstance(context)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        requireNotNull(mContext.get())
        if (!mLoadFinished) {
            mLoadFinished = true
            mCallbacks?.onAlbumLoad(data)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        requireNotNull(mContext.get())
        mCallbacks?.onAlbumReset()
    }

    fun onCreate(activity: FragmentActivity, callbacks: AlbumCallbacks) {
        mContext = WeakReference(activity)
        mLoaderManager = activity.supportLoaderManager
        mCallbacks = callbacks
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            return
        }
        mCurrentSelection = savedInstanceState.getInt(STATE_CURRENT_SELECTION)
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_CURRENT_SELECTION, mCurrentSelection)
    }

    fun onDestroy() {
        mLoaderManager?.destroyLoader(LOADER_ID)
        mCallbacks = null
    }

    fun loadAlbums() = mLoaderManager?.initLoader(LOADER_ID, null, this)

    interface AlbumCallbacks {
        fun onAlbumLoad(cursor: Cursor?)

        fun onAlbumReset()
    }
}