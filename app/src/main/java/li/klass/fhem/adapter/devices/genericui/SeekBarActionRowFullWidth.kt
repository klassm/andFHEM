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

abstract class SeekBarActionRowFullWidth(protected var initialProgress: Double,
                                         private val minimumProgress: Double,
                                         private val step: Double,
                                         private val maximumProgress: Double,
                                         private val layoutId: Int,
                                         updateRow: TableRow) {
    var updateView: TextView = updateRow.findViewById(R.id.value)

    fun createRow(inflater: LayoutInflater, device: FhemDevice): TableRow =
            createRow(inflater, device.xmlListDevice)

    open fun createRow(inflater: LayoutInflater, device: XmlListDevice?): TableRow {
        val seekbarMax = DimConversionUtil.toSeekbarProgress(maximumProgress, minimumProgress, step)
        val seekbarProgress = DimConversionUtil.toSeekbarProgress(initialProgress, minimumProgress, step)

        val row = inflater.inflate(layoutId, null) as TableRow
        findSeekbar(row)
                .apply {
                    setOnSeekBarChangeListener(createListener(device))
                    max = seekbarMax
                    progress = seekbarProgress
                }
        updateView.text = toUpdateText(device, initialProgress)

        return row
    }

    protected fun setSeekBarProgressTo(row: TableRow, progress: Double) {
        findSeekbar(row).progress = DimConversionUtil.toSeekbarProgress(progress, minimumProgress, step)
    }

    private fun findSeekbar(row: TableRow) = row.findViewById<SeekBar>(R.id.seekBar)!!

    private fun createListener(device: XmlListDevice?): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {

            internal var progress = initialProgress

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                this.progress = DimConversionUtil.toDimState(progress, minimumProgress, step)
                LOGGER.info("onProgressChange - progress={}, converted={}", progress, this.progress)
                if (fromUser) {
                    this@SeekBarActionRowFullWidth.onNewValue(device, this.progress)
                    initialProgress = progress.toDouble()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                this@SeekBarActionRowFullWidth.onProgressChange(seekBar.context, device, progress)
                toUpdateText(device, progress)
            }
        }
    }

    protected fun onNewValue(device: XmlListDevice?, progress: Double) {
        updateView.text = toUpdateText(device, progress)
    }

    open fun toUpdateText(device: XmlListDevice?, progress: Double): String = progress.toString() + ""

    abstract fun onProgressChange(context: Context, device: XmlListDevice?, progress: Double)

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SeekBarActionRowFullWidth::class.java)
    }
}
