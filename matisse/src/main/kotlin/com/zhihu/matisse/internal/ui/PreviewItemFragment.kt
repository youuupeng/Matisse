package com.zhihu.matisse.internal.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zhihu.matisse.R
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.entity.SelectionSpec
import com.zhihu.matisse.internal.utils.getBitmapSize
import com.zhihu.matisse.listener.OnFragmentInteractionListener
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase
import kotlinx.android.synthetic.main.fragment_preview_item.*
import org.jetbrains.anko.support.v4.toast

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
private const val ARGS_ITEM = "args_item"

fun newPreviewItemInstance(item: Item): PreviewItemFragment {
    val fragment = PreviewItemFragment()
    val bundle = Bundle()
    bundle.putParcelable(ARGS_ITEM, item)
    fragment.arguments = bundle
    return fragment
}

class PreviewItemFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_preview_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = arguments?.getParcelable<Item>(ARGS_ITEM) ?: return
        if (item.isVideo()) {
            video_play_button.visibility = View.VISIBLE
            video_play_button.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(item.uri, "video/*")
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.error_no_video_activity).show()
                }
            }
        } else {
            video_play_button.visibility = View.GONE
        }

        image_view.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        image_view.setSingleTapListener {
            mListener?.onClick()
        }

        val size = getBitmapSize(item.uri!!, activity!!)
        if (item.isGif()) {
            SelectionSpec.getInstance().imageEngine.loadGifImage(context!!, size.x, size.y, image_view, item.uri!!)
        } else {
            SelectionSpec.getInstance().imageEngine.loadImage(context!!, size.x, size.y, image_view,
                    item.uri!!)
        }
    }

    fun resetView() {
        view?.let {
            image_view.resetMatrix()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
}