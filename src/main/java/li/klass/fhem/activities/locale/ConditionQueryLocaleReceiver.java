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
import android.util.Log;

import com.google.common.base.Optional;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.update.backend.DeviceListService;

import static li.klass.fhem.activities.locale.LocaleIntentConstants.RESULT_CONDITION_SATISFIED;
import static li.klass.fhem.activities.locale.LocaleIntentConstants.RESULT_CONDITION_UNSATISFIED;

public class ConditionQueryLocaleReceiver extends BroadcastReceiver {

    public static final String TAG = ConditionQueryLocaleReceiver.class.getName();

    @Inject
    DeviceListService deviceListService;

    public ConditionQueryLocaleReceiver() {
        ApplicationComponent daggerComponent = AndFHEMApplication.Companion.getApplication().getDaggerComponent();
        daggerComponent.inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, intent.getAction());

        String deviceName = intent.getStringExtra(BundleExtraKeys.DEVICE_NAME);
        final String targetState = intent.getStringExtra(BundleExtraKeys.DEVICE_TARGET_STATE);

        Optional<FhemDevice> device = deviceListService.getDeviceForName(deviceName, null);
        if (!device.isPresent()) {
            setResultCode(RESULT_CONDITION_UNSATISFIED);
            return;
        }
        boolean satisfied = targetState != null && (targetState.equalsIgnoreCase(device.get().getInternalState())
                || device.get().getInternalState().matches(targetState));
        if (satisfied) {
            setResultCode(RESULT_CONDITION_SATISFIED);
        } else {
            setResultCode(RESULT_CONDITION_UNSATISFIED);
        }
    }
}
