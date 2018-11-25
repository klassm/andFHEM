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
import android.widget.TableRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import li.klass.fhem.adapter.uiservice.StateUiService
import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.ApplicationProperties

open class StateChangingSeekBarFullWidth(context: Context,
                                         private val stateUiService: StateUiService,
                                         applicationProperties: ApplicationProperties,
                                         private val dimmableBehavior: DimmableBehavior,
                                         updateRow: TableRow)
    : SeekBarActionRowFullWidthAndButton(context, dimmableBehavior.currentDimPosition,
        dimmableBehavior.dimStep, dimmableBehavior.dimLowerBound,
        dimmableBehavior.dimUpperBound, updateRow, applicationProperties) {

    override fun onProgressChange(context: Context, device: XmlListDevice?, progress: Double) {
        GlobalScope.launch(Dispatchers.Main) {
            dimmableBehavior.switchTo(stateUiService, context, progress)
        }
    }

    override fun toUpdateText(device: XmlListDevice?, progress: Double): String =
            dimmableBehavior.getDimStateForPosition(progress)
}
