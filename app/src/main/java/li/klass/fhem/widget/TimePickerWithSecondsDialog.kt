/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
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
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.widget

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import li.klass.fhem.R
import li.klass.fhem.databinding.TimepickerWithSecondsDialogBinding

class TimePickerWithSecondsDialog(context: Context, hours: Int, minutes: Int, seconds: Int,
                                  private val listener: TimePickerWithSecondsListener?)
    : AlertDialog(context, R.style.alertDialog), DialogInterface.OnClickListener {

    private val timePicker: TimePickerWithSeconds

    interface TimePickerWithSecondsListener {
        fun onTimeChanged(okClicked: Boolean, hours: Int, minutes: Int, seconds: Int, formattedText: String)
    }

    init {
        val inflater = LayoutInflater.from(context)
        val viewBinding = TimepickerWithSecondsDialogBinding.inflate(inflater)

        timePicker = viewBinding.timePickerWithSeconds.apply {
            this.hours = hours
            this.minutes = minutes
            this.seconds = seconds
        }

        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.okButton), this)
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancelButton), this)

        setView(viewBinding.root)
    }

    override fun onClick(dialogInterface: DialogInterface, which: Int) {
        if (listener == null) return

        listener.onTimeChanged(which == DialogInterface.BUTTON_POSITIVE, timePicker.hours, timePicker.minutes,
                timePicker.seconds, timePicker.formattedValue)
    }
}
