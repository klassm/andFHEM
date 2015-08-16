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
import android.content.Intent;
import android.widget.TableRow;

import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.behavior.dim.DimmableBehavior;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.util.ApplicationProperties;

public class StateChangingSeekBarFullWidth extends SeekBarActionRowFullWidthAndButton {

    private final StateUiService stateUiService;
    private ApplicationProperties applicationProperties;
    private DimmableBehavior dimmableBehavior;

    public StateChangingSeekBarFullWidth(Context context, StateUiService stateUiService, ApplicationProperties applicationProperties, DimmableBehavior dimmableBehavior, TableRow updateRow) {
        super(context, dimmableBehavior.getCurrentDimPosition(), dimmableBehavior.getDimLowerBound(), dimmableBehavior.getDimUpperBound(), updateRow);
        this.dimmableBehavior = dimmableBehavior;
        this.stateUiService = stateUiService;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void onButtonSetValue(FhemDevice device, int value) {
        onStopTrackingTouch(context, device, value);
    }

    @Override
    public void onStopTrackingTouch(Context context, FhemDevice device, int progress) {
        dimmableBehavior.switchTo(stateUiService, context, progress);
    }

    @Override
    public String toUpdateText(FhemDevice device, int progress) {
        return dimmableBehavior.getDimStateForPosition(progress);
    }

    @Override
    protected ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }
}
