package com.zhihu.matisse.internal.entity

import android.content.Context
import android.database.Cursor
import android.os.Parcelable
import android.provider.MediaStore
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.loader.AlbumLoader
import kotlinx.android.parcel.Parcelize

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/4/2019
 */

const val ALBUM_ID_ALL = (-1).toString()
const val ALBUM_NAME_ALL = "All"

/**
 * Constructs a new {@link Album} entity from the {@link Cursor}.
 * This method is not responsible for managing cursor resource, such as close, iterate, and so on.
 */
fun valueOfAlbum(cursor: Cursor): Album = Album(
        cursor.getString(cursor.getColumnIndex("bucket_id")),
        cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)),
        cursor.getString(cursor.getColumnIndex("bucket_display_name")),
        cursor.getLong(cursor.getColumnIndex(AlbumLoader.COLUMN_COUNT))
)

@Parcelize
class Album(val id: String, val coverPath: String, private val albumName: String, var count: Long) : Parcelable {

    fun addCaptureCount() {
        count++
    }

    fun isAll(): Boolean {
        return ALBUM_ID_ALL == id
    }

    fun isEmpty(): Boolean {
        return count == 0L
    }

    fun getDisplayName(context: Context): String {
        return (if (isAll()) {
            context.getString(R.string.album_name_all)
        } else {
            albumName
        })
    }
}