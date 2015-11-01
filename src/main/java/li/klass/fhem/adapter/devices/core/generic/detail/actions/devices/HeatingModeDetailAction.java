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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.devices;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TableRow;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.StateAttributeAction;
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

import static li.klass.fhem.util.EnumUtils.toStringList;

public abstract class HeatingModeDetailAction<M extends Enum<M>> implements StateAttributeAction {

    @Override
    public TableRow createRow(XmlListDevice device, String key, String stateValue, Context context, LayoutInflater inflater, ViewGroup parent) {
        M mode = getCurrentModeFor(device);
        final M[] available = getAvailableModes();

        return new StateChangingSpinnerActionRow(context, null, context.getString(R.string.setMode), toStringList(available), mode.name(), key).createRow(device, parent);
    }

    @Override
    public boolean supports(XmlListDevice xmlListDevice) {
        return true;
    }

    protected abstract M getCurrentModeFor(XmlListDevice device);

    protected abstract M[] getAvailableModes();
}
