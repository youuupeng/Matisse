package com.zhihu.matisse.internal.model

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.IncapableCause
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.ui.widget.UNCHECKED
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils
import com.zhihu.matisse.internal.utils.getPath

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/4/2019
 */

const val STATE_SELECTION = "state_selection"
const val STATE_COLLECTION_TYPE = "state_collection_type"
/**
 * Empty collection
 */
const val COLLECTION_UNDEFINED = 0x00
/**
 * Collection only with images
 */
const val COLLECTION_IMAGE = 0x01
/**
 * Collection only with videos
 */
const val COLLECTION_VIDEO = 0x01 shl 1
/**
 * Collection with images and videos.
 */
const val COLLECTION_MIXED = COLLECTION_IMAGE or COLLECTION_VIDEO

class SelectedItemCollection(private val mContext: Context) {
    private var mItems: MutableSet<Item>? = null
    var mCollectionType = COLLECTION_UNDEFINED

    fun onCreate(bundle: Bundle?) {
        if (bundle == null) {
            mItems = linkedSetOf()
        } else {
            val saved = bundle.getParcelableArrayList<Item>(STATE_SELECTION)
            mItems = linkedSetOf(*saved!!.toTypedArray())
            mCollectionType = bundle.getInt(STATE_COLLECTION_TYPE, COLLECTION_UNDEFINED)
        }
    }

    fun setDefaultSelection(uris: List<Item>) = mItems?.addAll(uris)

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(STATE_SELECTION, arrayListOf(*mItems!!.toTypedArray()))
        outState.putInt(STATE_COLLECTION_TYPE, mCollectionType)
    }

    fun getDataWithBundle(): Bundle {
        val bundle = Bundle()
        bundle.putParcelableArrayList(STATE_SELECTION, arrayListOf(*mItems!!.toTypedArray()))
        bundle.putInt(STATE_COLLECTION_TYPE, mCollectionType)
        return bundle
    }

    fun add(item: Item): Boolean {
        require(!typeConflict(item)) { "Can't select images and videos at the same time." }
        val added = mItems?.add(item) ?: false
        if (added) {
            when (mCollectionType) {
                COLLECTION_UNDEFINED -> {
                    when {
                        item.isImage() -> mCollectionType = COLLECTION_IMAGE
                        item.isVideo() -> mCollectionType = COLLECTION_VIDEO
                    }
                }
                COLLECTION_IMAGE -> {
                    when {
                        item.isVideo() -> mCollectionType = COLLECTION_MIXED
                    }
                }
                COLLECTION_VIDEO -> {
                    when {
                        item.isImage() -> mCollectionType = COLLECTION_MIXED
                    }
                }
            }
        }
        return added
    }

    fun remove(item: Item): Boolean {
        val removed = mItems?.remove(item) ?: false
        if (removed) {
            if (mItems?.size == 0) {
                mCollectionType = COLLECTION_UNDEFINED
            } else {
                if (mCollectionType == COLLECTION_MIXED) {
                    refineCollectionType()
                }
            }
        }
        return removed
    }

    fun overwrite(items: ArrayList<Item>, collectionType: Int) {
        mCollectionType = when {
            items.isEmpty() -> COLLECTION_UNDEFINED
            else -> collectionType
        }
        mItems?.clear()
        mItems?.addAll(items)
    }

    fun asList(): ArrayList<Item> = arrayListOf(*mItems!!.toTypedArray())

    fun asListOfUri(): MutableList<Uri> {
        val uris = mutableListOf<Uri>()
        for (item in mItems!!) {
            uris.add(item.uri!!)
        }
        return uris
    }

    fun asListOfString(): MutableList<String> {
        val paths = mutableListOf<String>()
        for (item in mItems!!) {
            paths.add(getPath(mContext, item.uri!!) ?: "")
        }
        return paths
    }

    fun isEmpty() = mItems == null || (mItems?.isEmpty() ?: true)

    fun isSelected(item: Item) = mItems?.contains(item) ?: false

    fun isAcceptable(item: Item): IncapableCause? {
        return when {
            maxSelectableReached() -> {
                val maxSelectable = currentMaxSelectable()
                val cause = try {
                    mContext.resources.getQuantityString(
                            R.plurals.error_over_count,
                            maxSelectable,
                            maxSelectable
                    )
                } catch (e: Resources.NotFoundException) {
                    mContext.getString(
                            R.string.error_over_count,
                            maxSelectable
                    )
                } catch (e: NoClassDefFoundError) {
                    mContext.getString(
                            R.string.error_over_count,
                            maxSelectable
                    )
                }
                //互操作过程中默认参数也要传,但是可以用 JvmOverloads 注解
                IncapableCause(cause)
            }
            typeConflict(item) -> IncapableCause(mContext.getString(R.string.error_type_conflict))
            else -> PhotoMetadataUtils.isAcceptable(mContext, item)
        }
    }

    fun maxSelectableReached() = mItems?.size == currentMaxSelectable()

    // depends
    private fun currentMaxSelectable(): Int {
        val spec = SelectionSpec.getInstance()
        return when {
            spec.maxSelectable > 0 -> spec.maxSelectable
            mCollectionType == COLLECTION_IMAGE -> spec.maxImageSelectable
            mCollectionType == COLLECTION_VIDEO -> spec.maxVideoSelectable
            else -> spec.maxSelectable
        }
    }

    fun getCollectionType(): Int = mCollectionType

    private fun refineCollectionType() {
        var hasImage = false
        var hasVideo = false
        for (item in mItems!!) {
            when {
                (item.isImage() && !hasImage) -> hasImage = true
                (item.isVideo() && !hasVideo) -> hasVideo = true
            }
        }
        when {
            (hasImage && hasVideo) -> mCollectionType = COLLECTION_MIXED
            hasImage -> mCollectionType = COLLECTION_IMAGE
            hasVideo -> mCollectionType = COLLECTION_VIDEO
        }
    }

    /**
     * Determine whether there will be conflict media types. A user can only select images and videos at the same time
     * while {@link SelectionSpec#mediaTypeExclusive} is set to false.
     */
    private fun typeConflict(item: Item) =
            SelectionSpec.getInstance().mediaTypeExclusive
            && ((item.isImage() && (mCollectionType == COLLECTION_VIDEO || mCollectionType == COLLECTION_MIXED))
            || (item.isVideo() && (mCollectionType == COLLECTION_IMAGE || mCollectionType == COLLECTION_MIXED)))

    fun count(): Int = mItems?.size ?: 0

    fun checkedNumOf(item: Item): Int {
        val index = arrayListOf(*mItems!!.toTypedArray()).indexOf(item)
        return if (index == -1) {
            UNCHECKED
        } else {
            index + 1
        }
    }
}