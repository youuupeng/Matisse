package com.zhihu.matisse.internal.ui.widget

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.zhihu.matisse.R

/**
 * Description
 * <p>
 *
 * @author peyo
 * @date 9/5/2019
 */
private const val EXTRA_TITLE = "extra_title"
private const val EXTRA_MESSAGE = "extra_message"

fun newDialogInstance(title: String, message: String): IncapableDialog {
    val dialog = IncapableDialog()
    val args = Bundle()
    args.putString(EXTRA_TITLE, title)
    args.putString(EXTRA_MESSAGE, message)
    dialog.arguments = args
    return dialog
}

class IncapableDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments!!.getString(EXTRA_TITLE)
        val message = arguments!!.getString(EXTRA_MESSAGE)

        val builder = AlertDialog.Builder(activity!!)
        if (!title.isNullOrEmpty()) {
            builder.setTitle(title)
        }
        if (!message.isNullOrEmpty()) {
            builder.setMessage(message)
        }
        builder.setPositiveButton(R.string.button_ok) { it, _ -> it.dismiss() }
        return builder.create()
    }
}