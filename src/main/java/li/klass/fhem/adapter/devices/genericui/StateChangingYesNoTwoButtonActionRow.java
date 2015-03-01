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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;

import li.klass.fhem.R;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;

public abstract class StateChangingYesNoTwoButtonActionRow<T extends FhemDevice> extends OnOffActionRow<T> {

    private final StateUiService stateUiService;
    private final String subState;

    public StateChangingYesNoTwoButtonActionRow(StateUiService stateUiService, int description, String subState) {
        super(OnOffActionRow.LAYOUT_DETAIL, description);
        this.stateUiService = stateUiService;
        this.subState = subState;
    }

    @Override
    protected String getOnStateText(T device, Context context) {
        return context.getString(R.string.yes);
    }

    @Override
    protected String getOffStateText(T device, Context context) {
        return context.getString(R.string.no);
    }

    @Override
    public void onButtonClick(Context context, T device, String targetState) {
        stateUiService.setSubState(device, subState, targetState, context);
    }

    @Override
    protected boolean isOn(T device) {
        return isYes(device);
    }

    protected abstract boolean isYes(T device);
}
