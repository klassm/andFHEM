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

package li.klass.fhem.activities.locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.FhemResultReceiver;

import static li.klass.fhem.activities.locale.LocaleIntentConstants.RESULT_CONDITION_SATISFIED;
import static li.klass.fhem.activities.locale.LocaleIntentConstants.RESULT_CONDITION_UNKNOWN;
import static li.klass.fhem.activities.locale.LocaleIntentConstants.RESULT_CONDITION_UNSATISFIED;

public class ConditionQueryLocaleReceiver extends BroadcastReceiver {

    public static final String TAG = ConditionQueryLocaleReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, intent.getAction());

        String deviceName = intent.getStringExtra(BundleExtraKeys.DEVICE_NAME);
        final String targetState = intent.getStringExtra(BundleExtraKeys.DEVICE_TARGET_STATE);

        context.startService(new Intent(Actions.GET_DEVICE_FOR_NAME)
                .setClass(context, RoomListIntentService.class)
                .putExtra(BundleExtraKeys.DEVICE_NAME, deviceName)
                .putExtra(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS || !resultData.containsKey(BundleExtraKeys.DEVICE)) {
                    finishConditionIntent(RESULT_CONDITION_UNSATISFIED);
                    return;
                }

                Device device = (Device) resultData.getSerializable(BundleExtraKeys.DEVICE);
                if (device == null) {
                    finishConditionIntent(RESULT_CONDITION_UNKNOWN);
                } else if (device.getInternalState().equalsIgnoreCase(targetState)) {
                    finishConditionIntent(RESULT_CONDITION_SATISFIED);
                } else if (device.getInternalState().matches(targetState)) {
                    finishConditionIntent(RESULT_CONDITION_SATISFIED);
                } else {
                    finishConditionIntent(RESULT_CONDITION_UNSATISFIED);
                }
            }
                }));
    }

    private void finishConditionIntent(int resultCode) {
        setResult(resultCode, "", new Bundle());
    }
}
