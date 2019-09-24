package com.zhihu.matisse.internal.loader

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import com.zhihu.matisse.internal.entity.ALBUM_ID_ALL
import com.zhihu.matisse.internal.entity.ALBUM_NAME_ALL
import com.zhihu.matisse.internal.entity.SelectionSpec

/**
 * Description
 * <p>
 *     Load all albums (grouped by bucket_id) into a single cursor.
 *
 * @author peyo
 * @date 9/4/2019
 */

const val COLUMN_COUNT = "count"
private val QUERY_URI = MediaStore.Files.getContentUri("external")
private val COLUMNS = arrayOf(
        MediaStore.Files.FileColumns._ID,
        "bucket_id",
        "bucket_display_name",
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.MIME_TYPE,
        COLUMN_COUNT)
private val PROJECTION = arrayOf(
        MediaStore.Files.FileColumns._ID,
        "bucket_id",
        "bucket_display_name",
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.MIME_TYPE,
        "COUNT(*) AS $COLUMN_COUNT")

// === params for showSingleMediaType: false ===
private const val SELECTION = (
        "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                + ") GROUP BY (bucket_id")
private val SELECTION_ARGS = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
// =============================================

// === params for showSingleMediaType: true ===
private const val SELECTION_FOR_SINGLE_MEDIA_TYPE = (
        MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                + ") GROUP BY (bucket_id")

private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
    return arrayOf(mediaType.toString())
}
// =============================================

// === params for showSingleMediaType: true ===
private const val SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE = (
        MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                + " AND " + MediaStore.MediaColumns.MIME_TYPE + "=?"
                + ") GROUP BY (bucket_id")

private fun getSelectionArgsForSingleMediaGifType(mediaType: Int): Array<String> {
    return arrayOf(mediaType.toString(), "image/gif")
}
// =============================================

private const val BUCKET_ORDER_BY = "datetaken DESC"


class AlbumLoader private constructor(context: Context, selection: String, selectionArgs: Array<String>) : CursorLoader(context, QUERY_URI, PROJECTION, selection, selectionArgs, BUCKET_ORDER_BY) {

    companion object {
        fun newInstance(context: Context): CursorLoader {
            val selection: String
            val selectionArgs: Array<String>
            when {
                SelectionSpec.getInstance().onlyShowGif() -> {
                    selection = SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE
                    selectionArgs = getSelectionArgsForSingleMediaGifType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                }
                SelectionSpec.getInstance().onlyShowImages() -> {
                    selection = SELECTION_FOR_SINGLE_MEDIA_TYPE
                    selectionArgs = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                }
                SelectionSpec.getInstance().onlyShowVideos() -> {
                    selection = SELECTION_FOR_SINGLE_MEDIA_TYPE
                    selectionArgs = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                }
                else -> {
                    selection = SELECTION
                    selectionArgs = SELECTION_ARGS
                }
            }
            return AlbumLoader(context, selection, selectionArgs)
        }
    }

    override fun loadInBackground(): Cursor? {
        val albums = super.loadInBackground()
        val allAlbum = MatrixCursor(COLUMNS)
        var totalCount = 0
        var allAlbumCoverPath = ""
        albums?.let {
            while (it.moveToNext()) {
                totalCount += it.getInt(it.getColumnIndex(COLUMN_COUNT))
            }
            if (it.moveToFirst()) {
                allAlbumCoverPath = it.getString(it.getColumnIndex(MediaStore.MediaColumns.DATA))
            }
        }

        allAlbum.addRow(arrayOf(ALBUM_ID_ALL, ALBUM_ID_ALL, ALBUM_NAME_ALL, allAlbumCoverPath, "",
                totalCount.toString()))
        return MergeCursor(arrayOf(allAlbum, albums))
    }
}