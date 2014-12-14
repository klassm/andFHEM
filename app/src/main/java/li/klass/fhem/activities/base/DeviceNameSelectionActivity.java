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

package li.klass.fhem.activities.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import li.klass.fhem.R;
import li.klass.fhem.activities.core.FragmentBaseActivity;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;
import li.klass.fhem.util.DialogUtil;
import li.klass.fhem.util.FhemResultReceiver;

public class DeviceNameSelectionActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addDeviceSelectionFragment();
    }

    private void addDeviceSelectionFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleExtraKeys.DEVICE_FILTER, new DeviceNameSelectionFragment.DeviceFilter() {
            @Override
            public boolean isSelectable(Device<?> device) {
                return DeviceNameSelectionActivity.this.isSelectable(device);
            }
        });

        bundle.putParcelable(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS ||
                        !resultData.containsKey(BundleExtraKeys.CLICKED_DEVICE)) return;

                Device<?> clickedDevice = (Device<?>) resultData.getSerializable(BundleExtraKeys.CLICKED_DEVICE);
                deviceClicked(clickedDevice);
            }
        });

        DeviceNameSelectionFragment deviceSelectionFragment = new DeviceNameSelectionFragment();
        deviceSelectionFragment.setArguments(bundle);

        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, deviceSelectionFragment)
                    .commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.e(FragmentBaseActivity.class.getName(), "error while switching to fragment " +
                    DeviceNameSelectionFragment.class.getName(), e);
        }
    }

    protected boolean isSelectable(Device<?> device) {
        return true;
    }

    protected void deviceClicked(final Device<?> device) {
        Intent result = new Intent();
        result.putExtra(BundleExtraKeys.DEVICE, device);
        setResult(RESULT_OK, result);
        finish();
    }
}
