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
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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

package li.klass.fhem.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import li.klass.fhem.activities.base.Updateable;
import li.klass.fhem.activities.core.FragmentBaseActivity;
import li.klass.fhem.constants.BundleExtraKeys;

import static li.klass.fhem.constants.Actions.*;

public class UIBroadcastReceiver extends BroadcastReceiver {

    private final IntentFilter intentFilter;
    private FragmentBaseActivity activity;
    private Updateable updateable;

    public UIBroadcastReceiver(FragmentBaseActivity activity, Updateable updateable) {
        this.activity = activity;
        this.updateable = updateable;

        intentFilter = new IntentFilter();
        intentFilter.addAction(DO_UPDATE);
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        final String action = intent.getAction();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.e(UIBroadcastReceiver.class.getName(), "received action " + action);

                try {
                    if (action.equals(DO_UPDATE)) {
                        boolean doUpdate = intent.getBooleanExtra(BundleExtraKeys.DO_REFRESH, false);
                        updateable.update(doUpdate);
                    }
                } catch (Exception e) {
                    Log.e(UIBroadcastReceiver.class.getName(), "error occurred", e);
                }
            }
        });
    }

    public void attach() {
        activity.registerReceiver(this, intentFilter);
    }

    public void detach() {
        try {
            activity.unregisterReceiver(this);
        } catch (IllegalArgumentException e) {
            Log.e(UIBroadcastReceiver.class.getName(), "error while detaching", e);
        }
    }
}
