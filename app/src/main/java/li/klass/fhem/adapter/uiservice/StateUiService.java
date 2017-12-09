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

package li.klass.fhem.adapter.uiservice;

import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;

import static li.klass.fhem.constants.Actions.DEVICE_DIM;
import static li.klass.fhem.constants.Actions.DEVICE_SET_STATE;
import static li.klass.fhem.constants.Actions.DEVICE_SET_SUB_STATE;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_DIM_PROGRESS;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_TARGET_STATE;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.BundleExtraKeys.STATE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.STATE_VALUE;

@Singleton
public class StateUiService {
    @Inject
    public StateUiService() {
    }


    public void setSubState(FhemDevice device,
                            String connectionId, String stateName, String value, Context context) {
        setSubState(device.getXmlListDevice(), stateName, value, connectionId, context);
    }

    public void setSubState(XmlListDevice device,
                            String stateName, String value, String connectionId, Context context) {
        if ("state".equalsIgnoreCase(stateName)) {
            setState(device, value, context, connectionId);
        } else {
            context.startService(new Intent(DEVICE_SET_SUB_STATE)
                    .setClass(context, DeviceIntentService.class)
                    .putExtra(CONNECTION_ID, connectionId)
                    .putExtra(DEVICE_NAME, device.getName())
                    .putExtra(STATE_NAME, stateName)
                    .putExtra(STATE_VALUE, value)
                    .putExtra(RESULT_RECEIVER, new UpdatingResultReceiver(context)));
        }
    }

    public void setState(FhemDevice device, String value, Context context, String connectionId) {
        setState(device.getXmlListDevice(), value, context, connectionId);
    }

    public void setState(XmlListDevice device, String value, Context context, String connectionId) {
        context.startService(new Intent(DEVICE_SET_STATE)
                .setClass(context, DeviceIntentService.class)
                .putExtra(CONNECTION_ID, connectionId)
                .putExtra(DEVICE_TARGET_STATE, value)
                .putExtra(DEVICE_NAME, device.getName())
                .putExtra(RESULT_RECEIVER, new UpdatingResultReceiver(context)));
    }

    public void setDim(DimmableDevice<?> device, float progress, Context context) {
        context.startService(new Intent(DEVICE_DIM)
                .setClass(context, DeviceIntentService.class)
                .putExtra(DEVICE_DIM_PROGRESS, progress)
                .putExtra(DEVICE_NAME, device.getName())
                .putExtra(RESULT_RECEIVER, new UpdatingResultReceiver(context)));
    }
}
