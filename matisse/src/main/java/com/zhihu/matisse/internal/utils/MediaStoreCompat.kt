package com.zhihu.matisse.internal.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import androidx.fragment.app.Fragment
import com.zhihu.matisse.internal.entity.CaptureStrategy
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */

/**
 * Checks whether the device has a camera feature or not.
 *
 * @param context a context to check for camera feature.
 * @return true if the device has a camera feature. false otherwise.
 */
fun hasCameraFeature(context: Context): Boolean {
    val packageManager = context.applicationContext.packageManager
    return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
}

class MediaStoreCompat constructor(activity: Activity, fragment: Fragment? = null) {
    private var mContext: WeakReference<Activity> = WeakReference(activity)
    private var mFragment: WeakReference<Fragment>? = null
    var mCaptureStrategy: CaptureStrategy? = null
    var mCurrentPhotoUri: Uri? = null
    var mCurrentPhotoPath: String? = null

    init {
        mFragment = if (fragment == null) {
            null
        } else {
            WeakReference(fragment)
        }
    }

    fun dispatchCaptureIntent(context: Context, requestCode: Int) {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureIntent.resolveActivity(context.packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            photoFile?.let {
                mCurrentPhotoPath = it.absolutePath
                mCurrentPhotoUri = FileProvider.getUriForFile(mContext.get()!!,
                        mCaptureStrategy!!.authority, it)
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri)
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                //kotlin optimization if
                when {
                    mFragment != null -> mFragment!!.get()!!.startActivityForResult(captureIntent, requestCode)
                    else -> mContext.get()!!.startActivityForResult(captureIntent, requestCode)
                }
            }
        }
    }

    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = String.format("JPEG_%s.jpg", timeStamp)
        var storageDir: File?
        if (mCaptureStrategy!!.isPublic) {
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES)
            if (!storageDir!!.exists()) storageDir.mkdirs()
        } else {
            storageDir = mContext.get()!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        }
        if (mCaptureStrategy!!.directory != null) {
            storageDir = File(storageDir, mCaptureStrategy!!.directory)
            if (!storageDir.exists()) storageDir.mkdirs()
        }

        // Avoid joining path components manually
        val tempFile = File(storageDir, imageFileName)

        // Handle the situation that user's external storage is not ready
        return if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
            null
        } else tempFile
    }
}