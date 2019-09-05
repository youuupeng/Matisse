package com.zhihu.matisse.internal.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
class MediaGrid : SquareFrameLayout, View.OnClickListener {
    private lateinit var mThumbnail: ImageView
    private lateinit var mCheckView: CheckView
    private lateinit var mGifTag: ImageView
    private lateinit var mVideoDuration: TextView
    private lateinit var mPreBindInfo: PreBindInfo
    private lateinit var mMedia: Item

    var mListener: OnMediaGridClickListener? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.media_grid_content, this, true)
        mThumbnail = findViewById(R.id.media_thumbnail)
        mCheckView = findViewById(R.id.check_view)
        mGifTag = findViewById(R.id.gif)
        mVideoDuration = findViewById(R.id.video_duration)

        mThumbnail.setOnClickListener(this)
        mCheckView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        mListener?.let {
            if (v == mThumbnail) {
                it.onThumbnailClicked(mThumbnail, mMedia, mPreBindInfo.mViewHolder)
            } else if (v == mCheckView) {
                it.onCheckViewClicked(mCheckView, mMedia, mPreBindInfo.mViewHolder)
            }
        }
    }

    fun preBindMedia(info: PreBindInfo) {
        mPreBindInfo = info
    }

    fun bindMedia(item: Item) {
        mMedia = item
        setGifTag()
        initCheckView()
        setImage()
        setVideoDuration()
    }

    fun setCheckEnabled(enabled: Boolean) {
        mCheckView.isEnabled = enabled
    }

    fun setCheckedNum(checkedNum: Int) {
        mCheckView.mCheckedNum = checkedNum
    }

    fun setChecked(checked: Boolean) {
        mCheckView.mChecked = checked
    }

    fun removeOnMediaGridClickListener() {
        mListener = null
    }

    private fun setImage() {
        if (mMedia.isGif()) {
            SelectionSpec.getInstance().imageEngine.loadGifThumbnail(context, mPreBindInfo.mResize,
                    mPreBindInfo.mPlaceholder, mThumbnail, mMedia.uri!!)
        } else {
            SelectionSpec.getInstance().imageEngine.loadThumbnail(context, mPreBindInfo.mResize,
                    mPreBindInfo.mPlaceholder, mThumbnail, mMedia.uri!!)
        }
    }

    private fun setVideoDuration() {
        if (mMedia.isVideo()) {
            mVideoDuration.visibility = VISIBLE
            mVideoDuration.text = DateUtils.formatElapsedTime(mMedia.duration / 1000)
        } else {
            mVideoDuration.visibility = GONE
        }
    }

    private fun setGifTag() {
        mGifTag.visibility = if (mMedia.isGif()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun initCheckView() {
        mCheckView.mCountable = mPreBindInfo.mCheckViewCountable
    }

    interface OnMediaGridClickListener {
        fun onThumbnailClicked(thumbnail: ImageView, item: Item, holder: RecyclerView.ViewHolder)

        fun onCheckViewClicked(checkView: CheckView, item: Item, holder: RecyclerView.ViewHolder)
    }

    class PreBindInfo(internal val mResize: Int, internal val mPlaceholder: Drawable?,
                      internal val mCheckViewCountable: Boolean, internal val mViewHolder: RecyclerView.ViewHolder)

}