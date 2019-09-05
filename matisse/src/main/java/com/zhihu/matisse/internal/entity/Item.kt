package com.zhihu.matisse.internal.entity

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import com.zhihu.matisse.isGifMimeType
import com.zhihu.matisse.isImageMimeType
import com.zhihu.matisse.isVideoMimeType

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/4/2019
 */

const val ITEM_ID_CAPTURE = -1L
const val ITEM_DISPLAY_NAME_CAPTURE = "Capture"

fun valueOfItem(cursor: Cursor): Item = Item(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
        cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
        cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)),
        cursor.getLong(cursor.getColumnIndex("duration")))

class Item() : Parcelable {
    private var id: Long = 0
    var mimeType: String? = null
    var uri: Uri? = null
    var size: Long = 0
    // only for video, in ms
    var duration: Long = 0

    constructor(_id: Long, _mimeType: String?, _size: Long, _duration: Long) : this() {
        id = _id
        mimeType = _mimeType
        size = _size
        duration = _duration
        val contentUri = when {
            isImage() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            isVideo() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }
        uri = ContentUris.withAppendedId(contentUri, id)
    }

    private constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        mimeType = parcel.readString()
        uri = parcel.readParcelable(Uri::class.java.classLoader)
        size = parcel.readLong()
        duration = parcel.readLong()
    }

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)

        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(mimeType)
        dest.writeParcelable(uri, 0)
        dest.writeLong(size)
        dest.writeLong(duration)
    }

    override fun describeContents(): Int = 0

    fun isCapture() = id == ITEM_ID_CAPTURE

    fun isImage() = isImageMimeType(mimeType)

    fun isGif() = isGifMimeType(mimeType)

    fun isVideo() = isVideoMimeType(mimeType)

    override fun equals(other: Any?): Boolean {
        return if (other !is Item) {
            false
        } else {
            id == other.id
                    && (mimeType != null && mimeType == other.mimeType
                    || (mimeType == null && other.mimeType == null))
                    && (uri != null && uri == other.uri
                    || (uri == null && other.uri == null))
                    && size == other.size
                    && duration == other.duration
        }
    }

    override fun hashCode(): Int {
        var result = 1
        result = 31 * result + id.hashCode()
        if (mimeType != null) {
            result = 31 * result + mimeType.hashCode()
        }
        result = 31 * result + uri.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + duration.hashCode()
        return result
    }
}