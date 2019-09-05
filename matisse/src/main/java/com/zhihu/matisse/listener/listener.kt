package com.zhihu.matisse.listener

import android.net.Uri

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
interface OnCheckedListener {
    fun onCheck(isChecked: Boolean)
}

interface OnSelectedListener {
    /**
     * @param uriList  the selected item {@link Uri} list.
     * @param pathList the selected item file path list.
     */
    fun onSelected(uriList: List<Uri>, pathList: List<String>)
}

interface OnFragmentInteractionListener{
    /**
     *  ImageViewTouch 被点击了
     */
    fun onClick()
}