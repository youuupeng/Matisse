package com.zhihu.matisse.internal.loader

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.ITEM_DISPLAY_NAME_CAPTURE
import com.zhihu.matisse.internal.entity.ITEM_ID_CAPTURE
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.utils.MediaStoreCompat.hasCameraFeature

/**
 * Description
 * <p>
 *     Load images and videos into a single cursor.
 *
 * @author peyo
 * @date 9/4/2019
 */

private val QUERY_URI = MediaStore.Files.getContentUri("external")
private val PROJECTION = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.SIZE,
        "duration")

// === params for album ALL && showSingleMediaType: false ===
private const val SELECTION_ALL = (
        "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")

private val SELECTION_ALL_ARGS = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
// ===========================================================

// === params for album ALL && showSingleMediaType: true ===
private const val SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE = (
        MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")

private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> = arrayOf(mediaType.toString())
// =========================================================

// === params for ordinary album && showSingleMediaType: false ===
private const val SELECTION_ALBUM = (
        "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                + " AND "
                + " bucket_id=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")

private fun getSelectionAlbumArgs(albumId: String): Array<String> = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        albumId)
// ===============================================================

// === params for ordinary album && showSingleMediaType: true ===
private const val SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE = (
        MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND "
                + " bucket_id=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0"
        )

private fun getSelectionAlbumArgsForSingleMediaType(mediaType: Int, albumId: String): Array<String> = arrayOf(
        mediaType.toString(),
        albumId)
// ===============================================================

// === params for album ALL && showSingleMediaType: true && MineType=="image/gif"
private const val SELECTION_ALL_FOR_GIF = (
        MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND "
                + MediaStore.MediaColumns.MIME_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")

private fun getSelectionArgsForGifType(mediaType: Int): Array<String> {
    return arrayOf(mediaType.toString(), "image/gif")
}
// ===============================================================

// === params for ordinary album && showSingleMediaType: true  && MineType=="image/gif" ===
private const val SELECTION_ALBUM_FOR_GIF = (
        MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND "
                + " bucket_id=?"
                + " AND "
                + MediaStore.MediaColumns.MIME_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")

private fun getSelectionAlbumArgsForGifType(mediaType: Int, albumId: String): Array<String> {
    return arrayOf(mediaType.toString(), albumId, "image/gif")
}
// ===============================================================

private const val ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC"

//kotlin out?
class AlbumMediaLoader(context: Context, selection: String?, selectionArgs: Array<out String>?, private val capture: Boolean) : CursorLoader(context, QUERY_URI, PROJECTION, selection, selectionArgs, ORDER_BY)  {

    companion object {
        fun newInstance(context: Context, album: Album, capture: Boolean): CursorLoader {
            lateinit var selection: String
            lateinit var selectionArgs: Array<String>
            val enableCapture: Boolean

            when {
                album.isAll() -> {
                    when {
                        SelectionSpec.getInstance().onlyShowGif() -> {
                            selection = SELECTION_ALL_FOR_GIF
                            selectionArgs = getSelectionArgsForGifType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                        }
                        SelectionSpec.getInstance().onlyShowImages() -> {
                            selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
                            selectionArgs = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                        }
                        SelectionSpec.getInstance().onlyShowVideos() -> {
                            selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
                            selectionArgs = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                        }
                        else -> {
                            selection = SELECTION_ALL
                            selectionArgs = SELECTION_ALL_ARGS
                        }
                    }
                    enableCapture = capture
                }
                else -> {
                    when {
                        SelectionSpec.getInstance().onlyShowGif() -> {
                            selection = SELECTION_ALBUM_FOR_GIF
                            selectionArgs = getSelectionAlbumArgsForGifType(
                                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, album.id)
                        }
                        SelectionSpec.getInstance().onlyShowImages() -> {
                            selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE
                            selectionArgs = getSelectionAlbumArgsForSingleMediaType(
                                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, album.id)
                        }
                        SelectionSpec.getInstance().onlyShowVideos() -> {
                            selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE
                            selectionArgs = getSelectionAlbumArgsForSingleMediaType(
                                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, album.id)
                        }
                        else -> {
                            selection = SELECTION_ALBUM
                            selectionArgs = getSelectionAlbumArgs(album.id)
                        }
                    }
                    enableCapture = false
                }
            }
            return AlbumMediaLoader(context, selection, selectionArgs, enableCapture)
        }
    }

    override fun loadInBackground(): Cursor? {
        val result = super.loadInBackground()
        //kotlin why is -Kt?
        if (!capture || !hasCameraFeature(context)) {
            return result
        }
        val dummy = MatrixCursor(PROJECTION)
        dummy.addRow(arrayOf(ITEM_ID_CAPTURE, ITEM_DISPLAY_NAME_CAPTURE, "", 0, 0))
        return MergeCursor(arrayOf(dummy, result))
    }

    override fun onContentChanged() {
        // FIXME a dirty way to fix loading multiple times
    }
}