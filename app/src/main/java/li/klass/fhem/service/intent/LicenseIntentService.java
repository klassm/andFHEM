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

package li.klass.fhem.service.intent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.Serializable;

import javax.inject.Inject;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.billing.IsPremiumListener;
import li.klass.fhem.billing.LicenseService;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;

public class LicenseIntentService extends BroadcastReceiver {

    @Inject
    LicenseService licenseService;

    @Inject
    ApplicationProperties applicationProperties;

    public LicenseIntentService() {
        super();
        AndFHEMApplication.Companion.getApplication().getDaggerComponent().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);

        if (Actions.IS_PREMIUM.equals(action)) {
            handlePremiumRequest(resultReceiver);
        }
    }

    private void handlePremiumRequest(final ResultReceiver resultReceiver) {

        licenseService.isPremium(new IsPremiumListener() {
            @Override
            public void isPremium(boolean isPremium) {
                sendSingleExtraResult(resultReceiver, isPremium);
            }
        });
    }

    protected void sendSingleExtraResult(ResultReceiver receiver, Serializable value) {
        if (receiver != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(li.klass.fhem.constants.BundleExtraKeys.IS_PREMIUM, value);
            receiver.send(li.klass.fhem.constants.ResultCodes.SUCCESS, bundle);
        }
    }
}
