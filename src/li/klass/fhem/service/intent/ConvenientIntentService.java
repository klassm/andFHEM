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

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;

import java.io.Serializable;

import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;

/**
 * Abstract class extending {@link IntentService} to provide some more convenience methods.
 */
public abstract class ConvenientIntentService extends IntentService {
    public ConvenientIntentService(String name) {
        super(name);
    }

    protected enum STATE {
        SUCCESS, ERROR, DONE
    }

    protected void sendNoResult(ResultReceiver receiver, int resultCode) {
        if (receiver != null) {
            receiver.send(resultCode, null);
        }
    }

    protected void sendSingleExtraResult(ResultReceiver receiver, int resultCode, String bundleExtrasKey, Serializable value) {
        if (receiver != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(bundleExtrasKey, value);
            receiver.send(resultCode, bundle);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);
        boolean doRefresh = intent.getBooleanExtra(BundleExtraKeys.DO_REFRESH, false);

        try {
            STATE state = handleIntent(intent, doRefresh, resultReceiver);
            if (state == STATE.SUCCESS) {
                sendNoResult(resultReceiver, ResultCodes.SUCCESS);
            } else if (state == STATE.ERROR) {
                sendNoResult(resultReceiver, ResultCodes.ERROR);
            }
        } catch (Exception e) {
            Log.e(ConvenientIntentService.class.getName(), "An error occurred while processing an intent", e);
            sendNoResult(resultReceiver, ResultCodes.ERROR);
        }
    }
    
    protected abstract STATE handleIntent(Intent intent, boolean doRefresh, ResultReceiver resultReceiver);
}
