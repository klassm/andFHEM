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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import li.klass.fhem.R
import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.util.DimConversionUtil

class DimActionRow(inflater: LayoutInflater, private val stateUiService: StateUiService, private val context: Context) {
    private var updateView: TextView? = null
    private val description: TextView
    private val seekBar: SeekBar
    val view: TableRow

    init {
        view = inflater.inflate(LAYOUT_OVERVIEW, null) as TableRow
        description = view.findViewById(R.id.description)
        seekBar = view.findViewById(R.id.seekBar)
    }

    fun fillWith(device: FhemDevice, updateRow: TableRow?, connectionId: String?) {
        val behavior = DimmableBehavior.behaviorFor(device, connectionId) ?: return

        seekBar.setOnSeekBarChangeListener(createListener(behavior))
        seekBar.max = DimConversionUtil.toSeekbarProgress(behavior.dimUpperBound, behavior.dimLowerBound, behavior.dimStep)
        seekBar.progress = DimConversionUtil.toSeekbarProgress(behavior.currentDimPosition, behavior.dimLowerBound, behavior.dimStep)
        description.text = device.aliasOrName
        if (updateRow != null) {
            updateView = updateRow.findViewById(R.id.value)
        }
    }

    private fun createListener(behavior: DimmableBehavior): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {

            var progress = behavior.currentDimPosition

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                this.progress = DimConversionUtil.toDimState(progress, behavior.dimLowerBound, behavior.dimStep)

                if (updateView != null) {
                    updateView!!.text = behavior.getDimStateForPosition(this.progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                GlobalScope.launch(Dispatchers.Main) {
                    behavior.switchTo(stateUiService, context, progress)
                }
            }
        }
    }

    companion object {

        private val LAYOUT_OVERVIEW = R.layout.device_overview_seekbarrow
        val HOLDER_KEY = "DimActionRow"
    }
}
