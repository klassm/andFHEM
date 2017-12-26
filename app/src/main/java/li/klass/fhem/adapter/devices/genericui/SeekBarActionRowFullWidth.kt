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
import android.widget.SeekBar
import android.widget.TableRow
import android.widget.TextView
import li.klass.fhem.R
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.DimConversionUtil
import org.slf4j.LoggerFactory

abstract class SeekBarActionRowFullWidth(protected var initialProgress: Float, private val minimumProgress: Float, private val step: Float, private val maximumProgress: Float, private val layoutId: Int,
                                         updateRow: TableRow) {
    var updateView: TextView? = null

    init {
        setUpdateRow(updateRow)
    }

    protected fun setUpdateRow(updateRow: TableRow?) {
        if (updateRow != null) {
            updateView = updateRow.findViewById(R.id.value)
        }
    }

    fun createRow(inflater: LayoutInflater, device: FhemDevice): TableRow =
            createRow(inflater, device.xmlListDevice)

    open fun createRow(inflater: LayoutInflater, device: XmlListDevice?): TableRow {
        val seekbarMax = DimConversionUtil.toSeekbarProgress(maximumProgress, minimumProgress, step)
        val seekbarProgress = DimConversionUtil.toSeekbarProgress(initialProgress, minimumProgress, step)

        val row = inflater.inflate(layoutId, null) as TableRow
        val seekBar = row.findViewById<SeekBar>(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(createListener(device))
        seekBar.max = seekbarMax
        seekBar.progress = seekbarProgress
        return row
    }

    private fun createListener(device: XmlListDevice?): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {

            internal var progress = initialProgress

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                this.progress = DimConversionUtil.toDimState(progress, minimumProgress, step)
                LOGGER.info("onStopTrackingTouch - progress={}, converted={}", progress, this.progress)
                if (updateView != null && fromUser) {
                    this@SeekBarActionRowFullWidth.onProgressChanged(updateView, seekBar.context, device, this.progress)
                    initialProgress = progress.toFloat()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                this@SeekBarActionRowFullWidth.onStopTrackingTouch(seekBar.context, device, progress)
                toUpdateText(device, progress)
            }
        }
    }

    open fun onProgressChanged(updateView: TextView?, context: Context, device: XmlListDevice?, progress: Float) {
        updateView!!.text = toUpdateText(device, progress)
    }

    open fun toUpdateText(device: XmlListDevice?, progress: Float): String = progress.toString() + ""

    abstract fun onStopTrackingTouch(context: Context, device: XmlListDevice?, progress: Float)

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SeekBarActionRowFullWidth::class.java)
    }
}
