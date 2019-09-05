package com.zhihu.matisse.internal.ui.adapter

import android.content.Context
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.entity.valueOfAlbum
import kotlinx.android.synthetic.main.album_list_item.view.*
import java.io.File

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
class AlbumsAdapter : CursorAdapter {
    var mPlaceholder: Drawable
    lateinit var view: View

    constructor(context: Context, cursor: Cursor?, autoRequery: Boolean) : super(context, cursor, autoRequery) {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(R.attr.album_thumbnail_placeholder))
        mPlaceholder = typedArray.getDrawable(0)
        typedArray.recycle()
    }

    constructor(context: Context, cursor: Cursor, flags: Int) : super(context, cursor, flags) {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(R.attr.album_thumbnail_placeholder))
        mPlaceholder = typedArray.getDrawable(0)
        typedArray.recycle()
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        view = LayoutInflater.from(context).inflate(R.layout.album_list_item, parent, false)
        return view
    }


    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val album = valueOfAlbum(cursor)
        view.album_name.text = album.getDisplayName(context)
        view.album_media_count.text = album.count.toString()

        // do not need to load animated Gif
        SelectionSpec.getInstance().imageEngine.loadThumbnail(context, context.resources.getDimensionPixelSize(R.dimen.media_grid_size), mPlaceholder,
                view.album_cover, Uri.fromFile(File(album.coverPath)))
    }
}