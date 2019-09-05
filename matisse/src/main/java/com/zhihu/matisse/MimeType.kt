package com.zhihu.matisse

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.collection.ArraySet
import com.zhihu.matisse.internal.utils.getUriPath
import java.util.*

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */

fun ofAll(): Set<MimeType> = EnumSet.allOf(MimeType::class.java)

fun ofMimeType(type: MimeType, vararg rest: MimeType): Set<MimeType> = EnumSet.of(type, *rest)

fun ofImageMimeType(): Set<MimeType> = EnumSet.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF, MimeType.BMP, MimeType.WEBP)

fun ofImageMimeType(onlyGif: Boolean) = EnumSet.of(MimeType.GIF)

fun ofGifMimeType(): Set<MimeType> = ofImageMimeType(true)

fun ofVideoMimeType(): Set<MimeType> = EnumSet.of(MimeType.MPEG, MimeType.MP4, MimeType.QUICKTIME, MimeType.THREEGPP, MimeType.THREEGPP2, MimeType.MKV, MimeType.WEBM, MimeType.TS, MimeType.AVI)

fun isImageMimeType(mimeType: String?): Boolean {
    return mimeType?.startsWith("image") ?: false
}

fun isVideoMimeType(mimeType: String?): Boolean {
    return mimeType?.startsWith("video") ?: false
}

fun isGifMimeType(mimeType: String?): Boolean {
    return if (mimeType == null){
        false
    } else {
        mimeType == MimeType.GIF.toString()
    }
}

private fun arraySetOf(vararg suffixes: String): Set<String> = ArraySet(listOf(*suffixes))


enum class MimeType constructor(private val mimeTypeName: String, private val extensions: Set<String>) {
    // ============== images ==============
    JPEG("image/jpeg", setOf("jpg", "jpeg")),
    PNG("image/png", setOf("png")),
    GIF("image/gif", setOf("gif")),
    BMP("image/x-ms-bmp", setOf("bmp")),
    WEBP("image/webp", setOf("webp")),

    // ============== videos ==============
    MPEG("video/mpeg", setOf("mpeg", "mpg")),
    MP4("video/mp4", setOf("mp4", "m4v")),
    QUICKTIME("video/quicktime", setOf("mov")),
    THREEGPP("video/3gpp", setOf("3gp", "3gpp")),
    THREEGPP2("video/3gpp2", setOf("3g2", "3gpp2")),
    MKV("video/x-matroska", setOf("mkv")),
    WEBM("video/webm", setOf("webm")),
    TS("video/mp2ts", setOf("ts")),
    AVI("video/avi", setOf("avi"));

    override fun toString() = mimeTypeName

    fun checkType(resolver: ContentResolver, uri: Uri?): Boolean {
        val map = MimeTypeMap.getSingleton()
        if (uri == null) {
            return false
        }
        val type = map.getExtensionFromMimeType(resolver.getType(uri))
        var path: String? = null
        // lazy load the path and prevent resolve for multiple times
        var pathParsed = false
        extensions.forEach { extension ->
            if (extension == type) {
                return true
            }
            if (!pathParsed) {
                // we only resolve the path for one time
                path = getUriPath(resolver, uri)
                if (!path.isNullOrEmpty()) {
                    path = path!!.toLowerCase(Locale.US)
                }
                pathParsed = true
            }
            path?.let {
                if (it.endsWith(extension)) {
                    return true
                }
            }
        }
        return false
    }
}