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

import android.content.Intent;
import android.os.ResultReceiver;

import javax.inject.Inject;

import li.klass.fhem.billing.LicenseService;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.util.ApplicationProperties;

import static li.klass.fhem.constants.BundleExtraKeys.IS_PREMIUM;
import static li.klass.fhem.constants.ResultCodes.SUCCESS;

public class LicenseIntentService extends ConvenientIntentService {

    @Inject
    LicenseService licenseService;

    @Inject
    ApplicationProperties applicationProperties;

    public LicenseIntentService() {
        super(LicenseIntentService.class.getName());
    }

    @Override
    protected State handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        String action = intent.getAction();

        if (Actions.IS_PREMIUM.equals(action)) {
            handlePremiumRequest(resultReceiver);
            return State.DONE;
        } else {
            return State.DONE;
        }
    }

    private void handlePremiumRequest(final ResultReceiver resultReceiver) {

        licenseService.isPremium(new LicenseService.IsPremiumListener() {
            @Override
            public void isPremium(boolean isPremium) {
                sendSingleExtraResult(resultReceiver, SUCCESS, IS_PREMIUM, isPremium);
            }
        });
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
