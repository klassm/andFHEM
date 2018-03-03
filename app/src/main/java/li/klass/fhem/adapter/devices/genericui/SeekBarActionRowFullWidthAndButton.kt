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

package li.klass.fhem.adapter.devices.genericui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TableRow
import li.klass.fhem.R
import li.klass.fhem.settings.SettingsKeys.SHOW_SET_VALUE_BUTTONS
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ApplicationProperties
import li.klass.fhem.util.DialogUtil
import li.klass.fhem.util.NumberUtil.isDecimalNumber

abstract class SeekBarActionRowFullWidthAndButton(protected var context: Context,
                                                  initialProgress: Double, step: Double,
                                                  minimumProgress: Double, maximumProgress: Double,
                                                  updateRow: TableRow,
                                                  val applicationProperties: ApplicationProperties)
    : SeekBarActionRowFullWidth(initialProgress, minimumProgress, step,
        maximumProgress, LAYOUT_DETAIL, updateRow) {

    override fun createRow(inflater: LayoutInflater, device: XmlListDevice?) =
            super.createRow(inflater, device).apply {
                applySetButtonIfRequired(this, device)
            }

    private fun applySetButtonIfRequired(row: TableRow, device: XmlListDevice?) {
        val button = row.findViewById<View>(R.id.button) as Button
        button.setOnClickListener {
            val title = context.getString(R.string.set_value)

            DialogUtil.showInputBox(context, title, initialProgress.toString() + "") { text ->
                if (isDecimalNumber(text)) {
                    val progress = text.toDouble()
                    setSeekBarProgressTo(row, progress)
                    onNewValue(device, progress)
                    onProgressChange(context, device, progress)
                } else {
                    DialogUtil.showAlertDialog(context, R.string.error, R.string.invalidInput)
                }
            }
        }
        if (!showButton()) {
            button.visibility = View.GONE
        }
    }

    protected open fun showButton(): Boolean =
            applicationProperties.getBooleanSharedPreference(SHOW_SET_VALUE_BUTTONS, false)

    companion object {
        const val LAYOUT_DETAIL = R.layout.device_detail_seekbarrow_with_button
    }
}
