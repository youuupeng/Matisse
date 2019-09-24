package com.zhihu.matisse.internal.entity

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.zhihu.matisse.internal.ui.widget.newDialogInstance
import org.jetbrains.anko.toast

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/4/2019
 */

fun handleCause(context: Context, cause: IncapableCause?) {
    cause?.let {
        when (cause.mForm) {
            IncapableCause.Form.NONE -> {
            }
            IncapableCause.Form.DIALOG -> {
                val incapableDialog = newDialogInstance(cause.mTitle!!, cause.mMessage!!)
                incapableDialog.show((context as FragmentActivity).supportFragmentManager, IncapableCause::class.java.name)
            }
            else -> context.toast(cause.mMessage.toString())
        }
    }
}

class IncapableCause {
    enum class Form {
        TOAST, DIALOG, NONE
    }

    var mTitle: String? = null
    var mMessage: String? = null
    var mForm = Form.TOAST

    constructor(message: String) {
        mMessage = message
    }

    constructor(title: String?, message: String) {
        mTitle = title
        mMessage = message
    }

    constructor(form: Form, message: String) {
        mForm = form
        mMessage = message
    }

    constructor(form: Form, title: String? = null, message: String) {
        mForm = form
        mTitle = title
        mMessage = message
    }
}