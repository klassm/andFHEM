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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.state

import android.content.Context
import android.view.ViewGroup
import android.widget.TableRow
import li.klass.fhem.R
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow
import li.klass.fhem.update.backend.xmllist.XmlListDevice
import li.klass.fhem.util.EnumUtils.toStringList

abstract class HeatingModeDetailAction<M : Enum<M>> : StateAttributeAction {

    protected abstract val availableModes: Array<M>

    override fun createRow(device: XmlListDevice, connectionId: String?, key: String, stateValue: String, context: Context, parent: ViewGroup): TableRow {
        val mode = getCurrentModeFor(device)
        val available = availableModes

        return StateChangingSpinnerActionRow(context, null, context.getString(R.string.setMode), toStringList(available), mode.name, key).createRow(device, connectionId, parent)
    }

    override fun supports(xmlListDevice: XmlListDevice): Boolean {
        return true
    }

    protected abstract fun getCurrentModeFor(device: XmlListDevice): M
}
