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

package li.klass.fhem.activities.deviceDetail;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import li.klass.fhem.R;
import li.klass.fhem.activities.base.BaseActivity;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;

public abstract class DeviceDetailActivity<D extends Device> extends BaseActivity<DeviceAdapter<D>> {

    protected String deviceName;
    protected String room;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        this.deviceName = extras.getString("deviceName");
        this.room = extras.getString("room");

        super.onCreate(savedInstanceState);

        String deviceDetailPrefix = getResources().getString(R.string.deviceDetailPrefix);
        setTitle(deviceDetailPrefix + " " + deviceName);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected DeviceAdapter<D> initializeLayoutAndReturnAdapter() {
        update(false);
        return adapter;
    }

    @Override
    protected void setLayout() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(boolean doUpdate) {
        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doUpdate);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                if (resultCode == ResultCodes.SUCCESS) {
                    D device = (D) resultData.getSerializable(BundleExtraKeys.DEVICE);
                    if (device == null) {
                        finish();
                        return;
                    }

                    DeviceAdapter<D> adapter = DeviceType.getAdapterFor(device);
                    setContentView(adapter.getDetailView(DeviceDetailActivity.this, device));
                }
            }
        });
        startService(intent);
    }
}
