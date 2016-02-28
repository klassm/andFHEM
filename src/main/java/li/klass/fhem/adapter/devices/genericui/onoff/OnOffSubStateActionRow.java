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

package li.klass.fhem.adapter.devices.genericui.onoff;

import android.content.Context;

import com.google.common.base.Optional;

import li.klass.fhem.R;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;

public class OnOffSubStateActionRow extends AbstractOnOffActionRow {

    private final String subState;

    public OnOffSubStateActionRow(int layoutId, String subState) {
        super(layoutId, Optional.of(R.string.blank));
        this.subState = subState;
    }

    @Override
    protected boolean isOn(FhemDevice device, Context context) {
        return !getOffStateName(device, context).equalsIgnoreCase(device.getXmlListDevice().getState(subState).or("off"));
    }

    @Override
    public void onButtonClick(Context context, FhemDevice device, String targetState) {
        new StateUiService().setSubState(device, subState, targetState, context);
    }
}
