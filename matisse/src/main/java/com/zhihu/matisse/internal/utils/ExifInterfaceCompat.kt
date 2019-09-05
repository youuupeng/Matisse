package com.zhihu.matisse.internal.utils

import android.media.ExifInterface
import android.util.Log
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Description
 * <p>
 *     Bug fixture for ExifInterface constructor.
 *
 * @author peyo
 * @date 9/5/2019
 */

private const val EXIF_DEGREE_FALLBACK_VALUE = -1
private const val TAG = "ExifInterfaceCompat"

/**
 * Creates new instance of {@link ExifInterface}.
 * Original constructor won't check filename value, so if null value has been passed,
 * the process will be killed because of SIGSEGV.
 * Google Play crash report system cannot perceive this crash, so this method will throw
 * {@link NullPointerException} when the filename is null.
 *
 * @param filename a JPEG filename.
 * @return {@link ExifInterface} instance.
 * @throws IOException something wrong with I/O.
 */
@Throws(IOException::class)
fun newExifInterfaceInstance(filename: String?): ExifInterface {
    if (filename == null) throw NullPointerException("filename should not be null")
    return ExifInterface(filename)
}

fun getExifDateTime(filepath: String): Date? {
    val exif: ExifInterface
    try {
        // ExifInterface does not check whether file path is null or not,
        // so passing null file path argument to its constructor causing SIGSEGV.
        // We should avoid such a situation by checking file path string.
        exif = newExifInterfaceInstance(filepath)
    } catch (ex: IOException) {
        Log.e(TAG, "cannot read exif", ex)
        return null
    }
    val date = exif.getAttribute(ExifInterface.TAG_DATETIME)
    if (date.isNullOrEmpty()) {
        return null
    }
    try {
        val formatter = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.parse(date)
    } catch (e: ParseException) {
        Log.d(TAG, "failed to parse date taken", e)
    }
    return null
}

/**
 * Read exif info and get datetime value of the photo.
 *
 * @param filepath to get datetime
 * @return when a photo taken.
 */
fun getExifDateTimeInMillis(filepath: String): Long {
    val datetime = getExifDateTime(filepath) ?: return -1
    return datetime.time
}

/**
 * Read exif info and get orientation value of the photo.
 *
 * @param filepath to get exif.
 * @return exif orientation value
 */
fun getExifOrientation(filepath: String): Int {
    val exif: ExifInterface
    try {
        // ExifInterface does not check whether file path is null or not,
        // so passing null file path argument to its constructor causing SIGSEGV.
        // We should avoid such a situation by checking file path string.
        exif = newExifInterfaceInstance(filepath)
    } catch (ex: IOException) {
        Log.e(TAG, "cannot read exif", ex)
        return EXIF_DEGREE_FALLBACK_VALUE
    }
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, EXIF_DEGREE_FALLBACK_VALUE)
    if (orientation == EXIF_DEGREE_FALLBACK_VALUE) {
        return 0
    }
    // We only recognize a subset of orientation tag values.
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}

class ExifInterfaceCompat private constructor()