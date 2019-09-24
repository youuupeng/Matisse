package com.zhihu.matisse.internal.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.entity.handleCause
import com.zhihu.matisse.internal.model.SelectedItemCollection
import com.zhihu.matisse.internal.ui.adapter.PreviewPagerAdapter
import com.zhihu.matisse.internal.ui.widget.IncapableDialog
import com.zhihu.matisse.internal.ui.widget.UNCHECKED
import com.zhihu.matisse.internal.ui.widget.newDialogInstance
import com.zhihu.matisse.internal.utils.getSizeInMB
import com.zhihu.matisse.internal.utils.hasKitKat
import com.zhihu.matisse.listener.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.activity_media_preview.*

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */

const val EXTRA_DEFAULT_BUNDLE = "extra_default_bundle"
const val EXTRA_RESULT_BUNDLE = "extra_result_bundle"
const val EXTRA_RESULT_APPLY = "extra_result_apply"
const val EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable"
const val CHECK_STATE = "checkState"

abstract class BasePreviewActivity : AppCompatActivity(), ViewPager.OnPageChangeListener, OnFragmentInteractionListener {

    protected var mPreviousPos = -1

    protected var mIsToolbarHide = false
    protected lateinit var mSpec: SelectionSpec
    protected lateinit var mAdapter: PreviewPagerAdapter
    protected var mOriginalEnable: Boolean = false
    protected lateinit var mSelectedCollection: SelectedItemCollection

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(SelectionSpec.getInstance().themeId)
        super.onCreate(savedInstanceState)
        if (!SelectionSpec.getInstance().hasInited) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        setContentView(R.layout.activity_media_preview)
        if (hasKitKat()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        mSpec = SelectionSpec.getInstance()
        if (mSpec.needOrientationRestriction()) {
            requestedOrientation = mSpec.orientation
        }

        mSelectedCollection = SelectedItemCollection(this)
        mOriginalEnable = if (savedInstanceState == null) {
            mSelectedCollection.onCreate(intent.getBundleExtra(EXTRA_DEFAULT_BUNDLE))
            intent.getBooleanExtra(EXTRA_RESULT_ORIGINAL_ENABLE, false)
        } else {
            mSelectedCollection.onCreate(savedInstanceState)
            savedInstanceState.getBoolean(CHECK_STATE)
        }
        button_back.setOnClickListener { onBackPressed() }
        button_apply.setOnClickListener {
            sendBackResult(true)
            finish()
        }
        pager.addOnPageChangeListener(this)
        mAdapter = PreviewPagerAdapter(supportFragmentManager, null)
        pager.adapter = mAdapter
        check_view.mCountable = mSpec.countable

        check_view.setOnClickListener {
            val item = mAdapter.getMediaItem(pager.currentItem)
            if (mSelectedCollection.isSelected(item)) {
                mSelectedCollection.remove(item)
                if (mSpec.countable) {
                    check_view.mCheckedNum = UNCHECKED
                } else {
                    check_view.mChecked = false
                }
            } else {
                if (assertAddSelection(item)) {
                    mSelectedCollection.add(item)
                    if (mSpec.countable) {
                        check_view.mCheckedNum = mSelectedCollection.checkedNumOf(item)
                    } else {
                        check_view.mChecked = true
                    }
                }
            }
            updateApplyButton()

            mSpec.onSelectedListener?.onSelected(mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString())

            originalLayout.setOnClickListener {

            }
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
            if (!mOriginalEnable) {
                original.setColor(Color.WHITE)
            }

            mSpec.onCheckedListener?.onCheck(mOriginalEnable)
        }

        updateApplyButton()
    }

    override fun onClick() {
        if (!mSpec.autoHideToobar) {
            return
        }

        if (mIsToolbarHide) {
            top_toolbar.animate()
                    .setInterpolator(FastOutSlowInInterpolator())
                    .translationYBy(top_toolbar.measuredHeight.toFloat())
                    .start()
            bottom_toolbar.animate()
                    .translationYBy((-bottom_toolbar.getMeasuredHeight()).toFloat())
                    .setInterpolator(FastOutSlowInInterpolator())
                    .start()
        } else {
            top_toolbar.animate()
                    .setInterpolator(FastOutSlowInInterpolator())
                    .translationYBy((-top_toolbar.measuredHeight).toFloat())
                    .start()
            bottom_toolbar.animate()
                    .setInterpolator(FastOutSlowInInterpolator())
                    .translationYBy(bottom_toolbar.measuredHeight.toFloat())
                    .start()
        }

        mIsToolbarHide = !mIsToolbarHide
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        val adapter = pager.adapter as PreviewPagerAdapter
        if (mPreviousPos != -1 && mPreviousPos != position) {
            (adapter.instantiateItem(pager, mPreviousPos) as PreviewItemFragment).resetView()

            val item = adapter.getMediaItem(position)
            if (mSpec.countable) {
                val checkedNum = mSelectedCollection.checkedNumOf(item)
                check_view.mCheckedNum = checkedNum
                if (checkedNum > 0) {
                    check_view.isEnabled = true
                } else {
                    check_view.isEnabled = !mSelectedCollection.maxSelectableReached()
                }
            } else {
                val checked = mSelectedCollection.isSelected(item)
                check_view.mChecked = checked
                if (checked) {
                    check_view.isEnabled = true
                } else {
                    check_view.isEnabled = !mSelectedCollection.maxSelectableReached()
                }
            }
            updateSize(item)
        }
        mPreviousPos = position
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        mSelectedCollection.onSaveInstanceState(outState!!)
        outState.putBoolean("checkState", mOriginalEnable)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        sendBackResult(false)
        super.onBackPressed()
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

    private fun updateOriginalState() {
        original.setChecked(mOriginalEnable)
        if (!mOriginalEnable) {
            original.setColor(Color.WHITE)
        }

        if (countOverMaxSize() > 0) {

            if (mOriginalEnable) {
                val incapableDialog = newDialogInstance("",
                        getString(R.string.error_over_original_size, mSpec.originalMaxSize))
                incapableDialog.show(supportFragmentManager,
                        IncapableDialog::class.java.name)

                original.setChecked(false)
                original.setColor(Color.WHITE)
                mOriginalEnable = false
            }
        }
    }

    private fun updateApplyButton() {
        val selectedCount = mSelectedCollection.count()
        if (selectedCount == 0) {
            button_apply.setText(R.string.button_sure_default)
            button_apply.isEnabled = false
        } else if (selectedCount == 1 && mSpec.singleSelectionModeEnabled()) {
            button_apply.setText(R.string.button_sure_default)
            button_apply.isEnabled = true
        } else {
            button_apply.isEnabled = true
            button_apply.text = getString(R.string.button_sure, selectedCount)
        }

        if (mSpec.originalable) {
            originalLayout.visibility = View.VISIBLE
            updateOriginalState()
        } else {
            originalLayout.visibility = View.GONE
        }
    }

    protected fun updateSize(item: Item) {
        if (item.isGif()) {
            size.visibility = View.VISIBLE
            size.text = getSizeInMB(item.size).toString() + "M"
        } else {
            size.visibility = View.GONE
        }

        if (item.isVideo()) {
            originalLayout.visibility = View.GONE
        } else if (mSpec.originalable) {
            originalLayout.visibility = View.VISIBLE
        }
    }

    protected fun sendBackResult(apply: Boolean) {
        val intent = Intent()
        intent.putExtra(EXTRA_RESULT_BUNDLE, mSelectedCollection.getDataWithBundle())
        intent.putExtra(EXTRA_RESULT_APPLY, apply)
        intent.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
        setResult(Activity.RESULT_OK, intent)
    }

    private fun assertAddSelection(item: Item): Boolean {
        val cause = mSelectedCollection.isAcceptable(item)
        handleCause(this, cause)
        return cause == null
    }
}