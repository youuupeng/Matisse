package com.zhihu.matisse.internal.model

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.loader.AlbumMediaLoader
import java.lang.ref.WeakReference

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/4/2019
 */

private const val LOADER_ID = 2
private const val ARGS_ALBUM = "args_album"
private const val ARGS_ENABLE_CAPTURE = "args_enable_capture"

class AlbumMediaCollection : LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var mContext: WeakReference<Context>
    private var mLoaderManager: LoaderManager ?= null
    private var mCallbacks: AlbumMediaCallbacks? = null

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val context = requireNotNull(mContext?.get())
        val album: Album? = args?.getParcelable(ARGS_ALBUM)
        requireNotNull(album)
        return AlbumMediaLoader.newInstance(context, album,
                album.isAll() && args.getBoolean(ARGS_ENABLE_CAPTURE, false))
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        requireNotNull(mContext.get())
        mCallbacks?.onAlbumMediaLoad(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        requireNotNull(mContext.get())
        mCallbacks?.onAlbumMediaReset()
    }

    fun onCreate(context: FragmentActivity, callbacks: AlbumMediaCallbacks) {
        mContext = WeakReference(context)
        mLoaderManager = context.supportLoaderManager
        mCallbacks = callbacks
    }

    fun onDestroy() {
        mLoaderManager?.destroyLoader(LOADER_ID)
        mCallbacks = null
    }

    fun load(target: Album, enableCapture: Boolean = false) {
        val args = Bundle()
        args.putParcelable(ARGS_ALBUM, target)
        args.putBoolean(ARGS_ENABLE_CAPTURE, enableCapture)
        mLoaderManager?.initLoader(LOADER_ID, args, this)
    }

    interface AlbumMediaCallbacks {

        fun onAlbumMediaLoad(cursor: Cursor?)

        fun onAlbumMediaReset()
    }
}