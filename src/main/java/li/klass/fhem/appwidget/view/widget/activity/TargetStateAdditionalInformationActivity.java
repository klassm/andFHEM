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

package li.klass.fhem.appwidget.view.widget.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.util.DialogUtil;

import static li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation.deviceStateForFHEM;
import static li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation.isValidAdditionalInformationValue;

public class TargetStateAdditionalInformationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.target_state_additional_information_selection_view);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }

        final String deviceName = extras.getString(BundleExtraKeys.DEVICE_NAME);
        final String targetState = extras.getString(BundleExtraKeys.DEVICE_TARGET_STATE);
        final DeviceStateRequiringAdditionalInformation additionalInformationType = deviceStateForFHEM(targetState);

        TextView deviceNameView = (TextView) findViewById(R.id.deviceName);
        deviceNameView.setText(deviceName);

        TextView targetStateView = (TextView) findViewById(R.id.targetState);
        targetStateView.setText(targetState);

        final EditText additionalInfoView = (EditText) findViewById(R.id.additionalInformation);


        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = additionalInfoView.getText().toString();
                if (handleAdditionalInformationValue(content, additionalInformationType,
                        targetState, deviceName)) {
                    finish();
                }
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private boolean handleAdditionalInformationValue(String additionalInformation,
                                                     DeviceStateRequiringAdditionalInformation specialDeviceState,
                                                     String state, String deviceName) {

        if (isValidAdditionalInformationValue(additionalInformation, specialDeviceState)) {
            switchDeviceState(state + " " + additionalInformation, deviceName);
            return true;
        } else {
            DialogUtil.showAlertDialog(this, R.string.error, R.string.invalidInput);
            return false;
        }
    }

    private void switchDeviceState(String newState, String deviceName) {
        Intent intent = new Intent(Actions.DEVICE_SET_STATE);
        intent.setClass(this, DeviceIntentService.class);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, newState);
        startService(intent);
    }

}
