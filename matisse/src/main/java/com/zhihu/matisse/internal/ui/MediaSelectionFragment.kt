package com.zhihu.matisse.internal.ui

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.model.AlbumMediaCollection
import com.zhihu.matisse.internal.model.SelectedItemCollection
import com.zhihu.matisse.internal.ui.adapter.AlbumMediaAdapter
import com.zhihu.matisse.internal.ui.widget.MediaGridInset
import com.zhihu.matisse.internal.utils.spanCount
import kotlinx.android.synthetic.main.fragment_media_selection.*

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */

const val EXTRA_ALBUM = "extra_album"

fun newMediaSelectionInstance(album: Album): MediaSelectionFragment {
    val fragment = MediaSelectionFragment()
    val args = Bundle()
    args.putParcelable(EXTRA_ALBUM, album)
    fragment.arguments = args
    return fragment
}

class MediaSelectionFragment : Fragment(), AlbumMediaCollection.AlbumMediaCallbacks,
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener {

    private val mAlbumMediaCollection = AlbumMediaCollection()
    private lateinit var mAdapter: AlbumMediaAdapter
    private lateinit var mSelectionProvider: SelectionProvider
    private var mCheckStateListener: AlbumMediaAdapter.CheckStateListener? = null
    private var mOnMediaClickListener: AlbumMediaAdapter.OnMediaClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SelectionProvider) {
            mSelectionProvider = context
        } else {
            throw IllegalStateException("Context must implement SelectionProvider.")
        }
        if (context is AlbumMediaAdapter.CheckStateListener) {
            mCheckStateListener = context
        }
        if (context is AlbumMediaAdapter.OnMediaClickListener) {
            mOnMediaClickListener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val album = arguments?.getParcelable<Album>(EXTRA_ALBUM) ?: return
        mAdapter = AlbumMediaAdapter(context!!, mSelectionProvider.provideSelectedItemCollection(), recyclerview)
        mAdapter.apply {
            mAdapter.registerCheckStateListener(this@MediaSelectionFragment)
            mAdapter.registerOnMediaClickListener(this@MediaSelectionFragment)
        }
        recyclerview.setHasFixedSize(true)

        val selectionSpec = SelectionSpec.getInstance()
        val spanCount = when {
            selectionSpec.gridExpectedSize > 0 -> spanCount(context!!, selectionSpec.gridExpectedSize)
            else -> selectionSpec.spanCount
        }
        recyclerview.layoutManager = GridLayoutManager(context!!, spanCount)
        val spacing = resources.getDimensionPixelSize(R.dimen.media_grid_spacing)
        recyclerview.addItemDecoration(MediaGridInset(spanCount, spacing, false))
        recyclerview.adapter = mAdapter
        mAlbumMediaCollection.onCreate(activity!!, this)
        mAlbumMediaCollection.load(album, selectionSpec.capture)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAlbumMediaCollection.onDestroy()
    }

    fun refreshMediaGrid() {
        mAdapter.notifyDataSetChanged()
    }

    fun refreshSelection() {
        mAdapter.refreshSelection()
    }

    override fun onAlbumMediaLoad(cursor: Cursor?) {
        mAdapter.swapCursor(cursor)
    }

    override fun onAlbumMediaReset() {
        mAdapter.swapCursor(null)
    }

    override fun onUpdate() {
        // notify outer Activity that check state changed
        mCheckStateListener?.onUpdate()
    }

    override fun onMediaClick(album: Album?, item: Item, adapterPosition: Int) {
        mOnMediaClickListener?.onMediaClick(arguments!!.getParcelable(EXTRA_ALBUM), item, adapterPosition)
    }

    interface SelectionProvider {
        fun provideSelectedItemCollection(): SelectedItemCollection
    }
}