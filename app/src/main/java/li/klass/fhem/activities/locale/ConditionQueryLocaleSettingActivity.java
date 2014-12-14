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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import li.klass.fhem.R;
import li.klass.fhem.activities.base.DeviceNameSelectionActivity;
import li.klass.fhem.domain.core.Device;

import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_TARGET_STATE;

public class ConditionQueryLocaleSettingActivity extends Activity {

    public static final String TAG = ConditionQueryLocaleSettingActivity.class.getName();
    private String selectedDeviceName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.locale_getstate_plugin);

        Button setButton = (Button) findViewById(R.id.set);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(ConditionQueryLocaleSettingActivity.this, DeviceNameSelectionActivity.class), 0);
            }
        });

        final EditText targetStateView = (EditText) findViewById(R.id.targetStateText);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(DEVICE_NAME)) {
                setDeviceName(intent.getStringExtra(DEVICE_NAME));
            }

            if (intent.hasExtra(DEVICE_TARGET_STATE)) {
                targetStateView.setText(intent.getStringExtra(DEVICE_TARGET_STATE));
            }
        }

        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable targetState = targetStateView.getText();

                Intent result = new Intent();
                result.putExtra(DEVICE_TARGET_STATE, targetState);
                result.putExtra(DEVICE_NAME, selectedDeviceName);
                result.putExtra(LocaleIntentConstants.EXTRA_STRING_BLURB, selectedDeviceName + " (" + targetState + ")");
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || !data.hasExtra(DEVICE)) return;

        Device<?> device = (Device<?>) data.getSerializableExtra(DEVICE);
        setDeviceName(device.getName());
    }

    private void setDeviceName(String deviceName) {
        this.selectedDeviceName = deviceName;

        TextView deviceNameView = (TextView) findViewById(R.id.deviceName);
        deviceNameView.setText(deviceName);
    }
}
