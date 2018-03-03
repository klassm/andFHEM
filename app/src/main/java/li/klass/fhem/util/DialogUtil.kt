/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.EditText
import android.widget.TextView

import li.klass.fhem.R

object DialogUtil {
    val DISMISSING_LISTENER: DialogInterface.OnClickListener = DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() }

    interface InputDialogListener {
        fun onClick(text: String)
    }

    fun showAlertDialog(context: Context, title: Int, text: Int, onClickListener: Runnable) {
        val titleText = if (title != -1) context.getString(title) else null
        val contentText = context.getString(text)

        showAlertDialog(context, titleText, contentText, onClickListener)
    }

    fun showAlertDialog(context: Context, title: Int, text: Int) =
            showAlertDialog(context, context.getString(title), context.getString(text))

    fun showAlertDialog(context: Context, title: Int, text: String) =
            showAlertDialog(context, context.getString(title), text)

    @JvmOverloads
    fun showAlertDialog(context: Context, title: String?, text: String, onClickListener: Runnable? = null) {
        val alert = AlertDialog.Builder(context, R.style.alertDialog)
                .setCancelable(false)
                .setMessage(text)
                .setTitle(title ?: "")
                .create()
                .apply {
                    setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.okButton)) { dialog, _ ->
                        dialog.dismiss()
                        onClickListener?.run()
                    }
                }
                .show()
    }

    fun showInputBox(context: Context, title: String, defaultText: String, positiveOnClickListener: InputDialogListener) {
        val input = EditText(context)
        input.setText(defaultText)

        showConfirmBox(context, title, input, Runnable {
            val text = input.text.toString()
            positiveOnClickListener.onClick(text)
        })
    }

    fun showConfirmBox(context: Context, title: Int, text: Int, positiveOnClickListener: Runnable) {
        showConfirmBox(
                context, context.getString(title), context.getString(text), positiveOnClickListener
        )
    }

    fun showConfirmBox(context: Context, title: String, text: String, positiveOnClickListener: Runnable) {
        val view = TextView(context)
        view.setPadding(5, 5, 5, 5)
        view.text = text

        showConfirmBox(context, title, view, positiveOnClickListener)
    }

    private fun showConfirmBox(context: Context, title: String, view: View, positiveOnClickListener: Runnable) {
        AlertDialog.Builder(context, R.style.alertDialog)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.okButton) { dialogInterface, _ ->
                    positiveOnClickListener.run()
                    dialogInterface.dismiss()
                }
                .setNegativeButton(R.string.cancelButton) { _, _ -> }.show()
    }
}
