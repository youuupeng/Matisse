package com.zhihu.matisse.ui

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.Album
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.entity.valueOfAlbum
import com.zhihu.matisse.internal.model.*
import com.zhihu.matisse.internal.ui.*
import com.zhihu.matisse.internal.ui.adapter.AlbumMediaAdapter
import com.zhihu.matisse.internal.ui.adapter.AlbumsAdapter
import com.zhihu.matisse.internal.ui.widget.AlbumsSpinner
import com.zhihu.matisse.internal.ui.widget.IncapableDialog
import com.zhihu.matisse.internal.ui.widget.newDialogInstance
import com.zhihu.matisse.internal.utils.MediaStoreCompat
import com.zhihu.matisse.internal.utils.getPath
import com.zhihu.matisse.internal.utils.getSizeInMB
import kotlinx.android.synthetic.main.activity_matisse.*
import org.jetbrains.anko.intentFor
import java.util.*

/**
 * Description
 * <p>
 *     Main Activity to display albums and media content (images/videos) in each album
 *     and also support media selecting operations.
 *
 * @author peyo
 * @date 9/6/2019
 */

const val EXTRA_RESULT_SELECTION = "extra_result_selection"
const val EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path"
const val EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable"
private const val REQUEST_CODE_PREVIEW = 23
private const val REQUEST_CODE_CAPTURE = 24
const val CHECK_STATE = "checkState"

class MatisseActivity : AppCompatActivity(), AlbumCollection.AlbumCallbacks, AdapterView.OnItemSelectedListener,
        MediaSelectionFragment.SelectionProvider, AlbumMediaAdapter.OnPhotoCapture,
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener {

    private val mAlbumCollection = AlbumCollection()
    private val mSelectedCollection = SelectedItemCollection(this)
    private lateinit var mSpec: SelectionSpec
    private lateinit var mAlbumsAdapter: AlbumsAdapter
    private lateinit var mAlbumsSpinner: AlbumsSpinner
    private var mMediaStoreCompat: MediaStoreCompat? = null

    private var mOriginalEnable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // programmatically set theme before super.onCreate()
        mSpec = SelectionSpec.getInstance()
        setTheme(mSpec.themeId)
        super.onCreate(savedInstanceState)
        if (!mSpec.hasInited) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        setContentView(R.layout.activity_matisse)

        if (mSpec.needOrientationRestriction()) {
            requestedOrientation = mSpec.orientation
        }

        if (mSpec.capture) {
            mMediaStoreCompat = MediaStoreCompat(this, null)
            if (mSpec.captureStrategy == null)
                throw RuntimeException("Don't forget to set CaptureStrategy.")
            mMediaStoreCompat!!.mCaptureStrategy = mSpec.captureStrategy
        }

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayHomeAsUpEnabled(true)
        val navigationIcon = toolbar.navigationIcon
        val typedArray = theme.obtainStyledAttributes(intArrayOf(R.attr.album_element_color))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        navigationIcon?.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        button_preview.setOnClickListener {
            startActivityForResult(intentFor<SelectedPreviewActivity>(
                    EXTRA_DEFAULT_BUNDLE to mSelectedCollection.getDataWithBundle(),
                    EXTRA_RESULT_ORIGINAL_ENABLE to mOriginalEnable),
                    REQUEST_CODE_PREVIEW)
        }
        button_apply.setOnClickListener {
            val selectedUris = mSelectedCollection.asListOfUri() as ArrayList<Uri>
            val selectedPaths = mSelectedCollection.asListOfString() as ArrayList<String>
            val result = Intent()
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris)
            result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths)
            result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
        originalLayout.setOnClickListener {
            val count = countOverMaxSize()
            if (count > 0) {
                val incapableDialog = newDialogInstance("",
                        getString(R.string.error_over_original_count, count, mSpec.originalMaxSize))
                incapableDialog.show(supportFragmentManager,
                        IncapableDialog::class.java.name)
                return@setOnClickListener
            }

            mOriginalEnable = !mOriginalEnable
            original.setChecked(mOriginalEnable)

            mSpec.onCheckedListener?.onCheck(mOriginalEnable)
        }

        mSelectedCollection.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE)
        }
        updateBottomToolbar()

        mAlbumsAdapter = AlbumsAdapter(this, null, false)
        mAlbumsSpinner = AlbumsSpinner(this)
        mAlbumsSpinner.mOnItemSelectedListener = this
        mAlbumsSpinner.setSelectedTextView(selected_album)
        mAlbumsSpinner.setPopupAnchorView(toolbar)
        mAlbumsSpinner.mAdapter = mAlbumsAdapter
        mAlbumCollection.onCreate(this, this)
        mAlbumCollection.onRestoreInstanceState(savedInstanceState)
        mAlbumCollection.loadAlbums()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mSelectedCollection.onSaveInstanceState(outState)
        mAlbumCollection.onSaveInstanceState(outState)
        outState.putBoolean("checkState", mOriginalEnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAlbumCollection.onDestroy()
        mSpec.onCheckedListener = null
        mSpec.onSelectedListener = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        requireNotNull(data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_PREVIEW) {
            val resultBundle = data.getBundleExtra(EXTRA_RESULT_BUNDLE)
            val selected = resultBundle.getParcelableArrayList<Item>(STATE_SELECTION)
            mOriginalEnable = data.getBooleanExtra(EXTRA_RESULT_ORIGINAL_ENABLE, false)
            val collectionType = resultBundle.getInt(STATE_COLLECTION_TYPE, COLLECTION_UNDEFINED)
            if (data.getBooleanExtra(EXTRA_RESULT_APPLY, false)) {
                val result = Intent()
                val selectedUris = ArrayList<Uri>()
                val selectedPaths = ArrayList<String>()
                selected?.let {
                    it.forEach { item ->
                        selectedUris.add(item.uri!!)
                        selectedPaths.add(getPath(this, item.uri!!)!!)
                    }
                }
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris)
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths)
                result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
                setResult(Activity.RESULT_OK, result)
                finish()
            } else {
                mSelectedCollection.overwrite(selected, collectionType)
                val mediaSelectionFragment = supportFragmentManager.findFragmentByTag(
                        MediaSelectionFragment::class.java.simpleName)
                if (mediaSelectionFragment is MediaSelectionFragment) {
                    mediaSelectionFragment.refreshMediaGrid()
                }
                updateBottomToolbar()
            }
        } else if (requestCode == REQUEST_CODE_CAPTURE) {
            // Just pass the data back to previous calling Activity.
            val contentUri = mMediaStoreCompat?.mCurrentPhotoUri
            val path = mMediaStoreCompat?.mCurrentPhotoPath
            val selected = ArrayList<Uri>()
            selected.add(contentUri!!)
            val selectedPath = ArrayList<String>()
            selectedPath.add(path!!)
            val result = Intent()
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selected)
            result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPath)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    private fun updateBottomToolbar() {

        val selectedCount = mSelectedCollection.count()
        if (selectedCount == 0) {
            button_preview.isEnabled = false
            button_apply.isEnabled = false
            button_apply.text = getString(R.string.button_sure_default)
        } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
            button_preview.isEnabled = true
            button_apply.setText(R.string.button_sure_default)
            button_apply.isEnabled = true
        } else {
            button_preview.isEnabled = true
            button_apply.isEnabled = true
            button_apply.text = getString(R.string.button_sure, selectedCount)
        }


        if (mSpec.originalable) {
            originalLayout.visibility = View.VISIBLE
            updateOriginalState()
        } else {
            originalLayout.visibility = View.INVISIBLE
        }
    }

    private fun updateOriginalState() {
        original.setChecked(mOriginalEnable)
        if (countOverMaxSize() > 0) {

            if (mOriginalEnable) {
                val incapableDialog = newDialogInstance("",
                        getString(R.string.error_over_original_size, mSpec.originalMaxSize))
                incapableDialog.show(supportFragmentManager,
                        IncapableDialog::class.java.name)

                original.setChecked(false)
                mOriginalEnable = false
            }
        }
    }

    private fun countOverMaxSize(): Int {
        var count = 0
        val selectedCount = mSelectedCollection.count()
        for (i in 0 until selectedCount) {
            val item = mSelectedCollection.asList()[i]

            if (item.isImage()) {
                val size = getSizeInMB(item.size)
                if (size > mSpec.originalMaxSize) {
                    count++
                }
            }
        }
        return count
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mAlbumCollection.mCurrentSelection = position
        mAlbumsAdapter.cursor.moveToPosition(position)
        val album = valueOfAlbum(mAlbumsAdapter.cursor)
        if (album.isAll() && SelectionSpec.getInstance().capture) {
            album.addCaptureCount()
        }
        onAlbumSelected(album)
    }

    override fun onAlbumLoad(cursor: Cursor?) {
        mAlbumsAdapter.swapCursor(cursor)
        // select default album.
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            cursor?.moveToPosition(mAlbumCollection.mCurrentSelection)
            mAlbumsSpinner.setSelection(this@MatisseActivity,
                    mAlbumCollection.mCurrentSelection)
            val album = valueOfAlbum(cursor!!)
            if (album.isAll() && SelectionSpec.getInstance().capture) {
                album.addCaptureCount()
            }
            onAlbumSelected(album)
        }
    }

    override fun onAlbumReset() {
        mAlbumsAdapter.swapCursor(null)
    }

    private fun onAlbumSelected(album: Album) {
        if (album.isAll() && album.isEmpty()) {
            container.visibility = View.GONE
            empty_view.visibility = View.VISIBLE
        } else {
            container.visibility = View.VISIBLE
            empty_view.visibility = View.GONE
            val fragment = newMediaSelectionInstance(album)
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, fragment, MediaSelectionFragment::class.java.simpleName)
                    .commitAllowingStateLoss()
        }
    }

    override fun onUpdate() {
        // notify bottom toolbar that check state changed.
        updateBottomToolbar()
        mSpec.onSelectedListener?.onSelected(mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString())
    }

    override fun onMediaClick(album: Album?, item: Item, adapterPosition: Int) {
        startActivityForResult(intentFor<AlbumPreviewActivity>(
                EXTRA_ALBUM to album,
                EXTRA_ITEM to item,
                EXTRA_DEFAULT_BUNDLE to mSelectedCollection.getDataWithBundle(),
                EXTRA_RESULT_ORIGINAL_ENABLE to mOriginalEnable
        ), REQUEST_CODE_PREVIEW)
    }

    override fun provideSelectedItemCollection() = mSelectedCollection

    override fun capture() {
        mMediaStoreCompat?.dispatchCaptureIntent(this, REQUEST_CODE_CAPTURE)
    }
}