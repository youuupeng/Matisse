package com.zhihu.matisse.internal.ui

import android.app.Activity
import android.os.Bundle
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.model.STATE_SELECTION
import kotlinx.android.synthetic.main.activity_media_preview.*

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
class SelectedPreviewActivity : BasePreviewActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!SelectionSpec.getInstance().hasInited){
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        val bundle = intent.getBundleExtra(EXTRA_DEFAULT_BUNDLE)
        val selected = bundle.getParcelableArrayList<Item>(STATE_SELECTION)
        mAdapter.addAll(selected)
        mAdapter.notifyDataSetChanged()
        if(mSpec.countable){
            check_view.mCheckedNum = 1
        } else {
            check_view.mChecked = true
        }
        mPreviousPos = 0
        updateSize(selected[0])
    }
}