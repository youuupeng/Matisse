package com.zhihu.matisse.internal.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Point
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.IncapableCause
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */

private val TAG = "PhotoMetadataUtilsKt"
private const val MAX_WIDTH = 1600
private const val SCHEME_CONTENT = "content"

fun getPixelsCount(resolver: ContentResolver, uri: Uri): Int {
    val size = getBitmapBound(resolver, uri)
    with(size) {
        return x * y
    }
}

fun getBitmapSize(uri: Uri, activity: Activity): Point {
    val resolver = activity.contentResolver
    val imageSize = getBitmapBound(resolver, uri)
    var w = imageSize.x
    var h = imageSize.y
    if (shouldRotate(resolver, uri)) {
        w = imageSize.y
        h = imageSize.x
    }
    if (h == 0) {
        return Point(MAX_WIDTH, MAX_WIDTH)
    }
    val metrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(metrics)
    val screenWidth = metrics.widthPixels.toFloat()
    val screenHeight = metrics.heightPixels.toFloat()
    val widthScale = screenWidth / w
    val heightScale = screenHeight / h
    if (widthScale > heightScale) {
        return Point((w * widthScale).toInt(), (h * heightScale).toInt())
    }
    return Point((w * widthScale).toInt(), (h * heightScale).toInt())
}

fun getBitmapBound(resolver: ContentResolver, uri: Uri?): Point {
    var inputStream: InputStream? = null
    try {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        inputStream = resolver.openInputStream(uri!!)
        BitmapFactory.decodeStream(inputStream, null, options)
        //kotlin optimization with opera?
        val width = options.outWidth
        val height = options.outHeight
        return Point(width, height)
    } catch (e: FileNotFoundException) {
        return Point(0, 0)
    } finally {
        inputStream?.let {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

fun getUriPath(resolver: ContentResolver, uri: Uri?): String? {
    if (uri == null) {
        return null
    }

    if (SCHEME_CONTENT == uri.scheme) {
        var cursor: Cursor? = null
        try {
            cursor = resolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
            if (cursor == null || !cursor.moveToFirst()) {
                return null
            }
            return cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
        } finally {
            cursor?.close()
        }
    }
    return uri.path
}

fun isAcceptableWithContext(context: Context, item: Item): IncapableCause? {
    if (!isSelectableType(context, item)) {
        return IncapableCause(context.getString(R.string.error_file_type))
    }

    SelectionSpec.getInstance().filters?.forEach { element ->
        val incapableCause = element.filter(context, item)
        incapableCause?.let {
            return incapableCause
        }
    }
    return null
}

private fun isSelectableType(context: Context?, item: Item): Boolean {
    if (context == null) {
        return false
    }

    val resolver = context.contentResolver
    for (type in SelectionSpec.getInstance().mimeTypeSet) {
        if (type.checkType(resolver, item.uri)) {
            return true
        }
    }
    return false
}

private fun shouldRotate(resolver: ContentResolver, uri: Uri): Boolean {
    val exif: ExifInterface?
    try {
        exif = newExifInterfaceInstance(getUriPath(resolver, uri))
    } catch (e: NullPointerException) {
        Log.e(TAG, "could not read exif info of the image: $uri")
        return false
    }
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
    return orientation == ExifInterface.ORIENTATION_ROTATE_90
            || orientation == ExifInterface.ORIENTATION_ROTATE_270
}

fun getSizeInMB(sizeInBytes: Long): Float {
    val df = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
    df.applyPattern("0.0")
    var result = df.format((sizeInBytes.toFloat() / 1024f / 1024f).toDouble())
    Log.e(TAG, "getSizeInMB: $result")
    result = result.replace(",".toRegex(), ".") // in some case , 0.0 will be 0,0
    return java.lang.Float.valueOf(result)
}

class PhotoMetadataUtils private constructor() {

    init {
        throw AssertionError("oops! the utility class is about to be instantiated...")
    }
}