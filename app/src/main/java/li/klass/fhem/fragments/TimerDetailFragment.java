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

package li.klass.fhem.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.AtDevice;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceStateAdditionalInformationType;
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;
import li.klass.fhem.util.FhemResultReceiver;
import li.klass.fhem.widget.TimePickerWithSecondsDialog;

import static li.klass.fhem.constants.BundleExtraKeys.CLICKED_DEVICE;

public class TimerDetailFragment extends BaseFragment {

    private static final DeviceNameSelectionFragment.DeviceFilter deviceFilter = new DeviceNameSelectionFragment.DeviceFilter() {
        @Override
        public boolean isSelectable(Device<?> device) {
            return device.getAvailableTargetStates() != null &&
                    device.getAvailableTargetStates().length > 0;
        }
    };

    private static final String TAG = TimerDetailFragment.class.getName();
    private transient ArrayAdapter<String> targetStateAdapter;

    public AtDevice timerDevice;

    private String timerDeviceName;
    private boolean isModify = false;

    private transient Device selectedTargetDevice;
    private String selectedTargetState;
    private int hour = 0;
    private int minute = 0;
    private int second = 0;
    private AtDevice.AtRepetition repetition;
    private AtDevice.TimerType type;
    private boolean requiresStateAppendix;

    @SuppressWarnings("unused")
    public TimerDetailFragment(Bundle bundle) {
        super(bundle);
        if (bundle.containsKey(BundleExtraKeys.DEVICE_NAME)) {
            timerDeviceName = bundle.getString(BundleExtraKeys.DEVICE_NAME);
            isModify = true;
        }
    }

    @SuppressWarnings("unused")
    public TimerDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            return view;
        }

        view = inflater.inflate(R.layout.timer_detail, null);

        TextView timerName = (TextView) view.findViewById(R.id.timerName);
        timerName.setText("");

        TextView targetDeviceName = (TextView) view.findViewById(R.id.targetDeviceName);
        targetDeviceName.setText("");

        createRepetitionSpinner(view);
        createSelectDeviceButton(view);
        createTargetStateSpinner(view);
        createTimerTypeSpinner(view);
        createSwitchTimeButton(view);
        createSendAndResetButtons(view);

        if (isModify) {
            view.findViewById(R.id.timerName).setEnabled(false);
        }

        return view;
    }

    private void createSelectDeviceButton(View view) {
        Button selectDeviceButton = (Button) view.findViewById(R.id.targetDeviceSet);
        selectDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.SHOW_FRAGMENT);
                intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.DEVICE_SELECTION);
                intent.putExtra(BundleExtraKeys.DEVICE_FILTER, deviceFilter);
                intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (!resultData.containsKey(CLICKED_DEVICE)) return;

                        selectedTargetDevice = (Device) resultData.get(CLICKED_DEVICE);
                    }
                });
                getActivity().sendBroadcast(intent);
            }
        });
    }

    private void createSendAndResetButtons(View view) {
        Button resetButton = (Button) view.findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update(false);
            }
        });

        Button saveButton = (Button) view.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    private void createSwitchTimeButton(View view) {
        Button switchTimeChangeButton = (Button) view.findViewById(R.id.switchTimeSet);
        switchTimeChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerWithSecondsDialog(getActivity(), hour, minute, second, new TimePickerWithSecondsDialog.TimePickerWithSecondsListener() {
                    @Override
                    public void onTimeChanged(boolean okClicked, int newHour, int newMinute, int newSecond, String formattedText) {
                        TextView dateText = (TextView) getView().findViewById(R.id.switchTimeContent);
                        dateText.setText(formattedText);

                        TimerDetailFragment.this.hour = newHour;
                        TimerDetailFragment.this.minute = newMinute;
                        TimerDetailFragment.this.second = newSecond;
                    }
                }).show();
            }
        });
        update(false);
    }

    private void createTimerTypeSpinner(View view) {
        view.findViewById(R.id.stateAppendix).setVisibility(View.GONE);
        TextView commandAppendix = (TextView) view.findViewById(R.id.stateAppendix);
        commandAppendix.setText("");

        Spinner timerTypeSpinner = (Spinner) view.findViewById(R.id.timerType);
        ArrayAdapter<String> timerTypeAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinnercontent);
        for (AtDevice.TimerType type : AtDevice.TimerType.values()) {
            timerTypeAdapter.add(type.getText());
        }
        timerTypeSpinner.setAdapter(timerTypeAdapter);
        timerTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TimerDetailFragment.this.type = AtDevice.TimerType.values()[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void createTargetStateSpinner(View view) {
        Spinner targetState = (Spinner) view.findViewById(R.id.targetState);
        targetStateAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinnercontent);
        targetState.setAdapter(targetStateAdapter);
        view.findViewById(R.id.targetStateRow).setVisibility(View.GONE);
        targetState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (selectedTargetDevice == null) {
                    Log.e(TAG, "cannot select new target state, as new target device is set!");
                    return;
                } else if (selectedTargetDevice.getAvailableTargetStates() == null) {
                    Log.e(TAG, "cannot select new target state, as the new device does not contain any target states!");
                    return;
                }

                String newTargetState = selectedTargetDevice.getAvailableTargetStates()[i];

                if (selectedTargetState == null || !selectedTargetState.equals(newTargetState)) {
                    selectTargetState(newTargetState);
                    setTargetStateAppendix(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void createRepetitionSpinner(View view) {
        Spinner repetitionSpinner = (Spinner) view.findViewById(R.id.timerRepetition);
        ArrayAdapter<String> repetitionAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinnercontent);
        for (AtDevice.AtRepetition atRepetition : AtDevice.AtRepetition.values()) {
            repetitionAdapter.add(atRepetition.getText());
        }
        repetitionSpinner.setAdapter(repetitionAdapter);
        repetitionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TimerDetailFragment.this.repetition = AtDevice.AtRepetition.values()[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void save() {
        if (selectedTargetDevice == null || selectedTargetState == null) {
            Intent intent = new Intent(Actions.SHOW_TOAST);
            intent.putExtra(BundleExtraKeys.TOAST_STRING_ID, R.string.incompleteConfigurationError);
            getActivity().sendBroadcast(intent);
            return;
        }

        CheckBox timerIsActiveCheckbox = (CheckBox) getView().findViewById(R.id.isActive);
        boolean isActive = timerIsActiveCheckbox.isChecked();

        Bundle bundle = new Bundle();
        bundle.putString(BundleExtraKeys.TIMER_TARGET_DEVICE_NAME, selectedTargetDevice.getName());
        bundle.putString(BundleExtraKeys.TIMER_TARGET_STATE, selectedTargetState);
        bundle.putInt(BundleExtraKeys.TIMER_HOUR, hour);
        bundle.putInt(BundleExtraKeys.TIMER_MINUTE, minute);
        bundle.putInt(BundleExtraKeys.TIMER_SECOND, second);
        bundle.putString(BundleExtraKeys.TIMER_REPETITION, repetition.name());
        bundle.putString(BundleExtraKeys.TIMER_TYPE, type.name());
        bundle.putBoolean(BundleExtraKeys.TIMER_IS_ACTIVE, isActive);

        if (requiresStateAppendix) {
            EditText targetStateAppendixView = (EditText) getView().findViewById(R.id.stateAppendix);
            bundle.putString(BundleExtraKeys.TIMER_TARGET_STATE_APPENDIX, targetStateAppendixView.getText().toString());
        }
        if (!isModify) {
            EditText timerNameView = (EditText) getView().findViewById(R.id.timerName);
            timerDeviceName = timerNameView.getText().toString();
        }
        bundle.putString(BundleExtraKeys.TIMER_NAME, timerDeviceName);

        String action = isModify ? Actions.DEVICE_TIMER_MODIFY : Actions.DEVICE_TIMER_NEW;
        Intent intent = new Intent(action);
        intent.putExtras(bundle);

        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == ResultCodes.SUCCESS) {
                    Intent intent = new Intent(Actions.BACK);
                    getActivity().sendBroadcast(intent);

                    isModify = false;
                }
            }
        });
        getActivity().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTargetDevice(selectedTargetDevice);
    }

    private void selectTargetDeviceInSpinner(String targetDeviceName, final String targetState) {
        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, targetDeviceName);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS || !resultData.containsKey(BundleExtraKeys.DEVICE)) {
                    return;
                }

                Device device = (Device) resultData.getSerializable(BundleExtraKeys.DEVICE);
                updateTargetDevice(device);
                selectTargetState(targetState);
            }
        });
        getActivity().startService(intent);
    }


    private void updateTargetDevice(Device targetDevice) {
        this.selectedTargetDevice = targetDevice;
        TextView targetDeviceView = (TextView) getView().findViewById(R.id.targetDeviceName);
        if (!updateTargetStateRowVisibility()) {
            targetDeviceView.setText(R.string.unknown);
            return;
        }
        setNewTargetStatesInSpinner();
        targetDeviceView.setText(targetDevice.getName());

    }

    private void selectTargetState(String targetState) {
        if (selectedTargetDevice == null) {
            return;
        }
        this.selectedTargetState = targetState;

        Spinner targetStateSpinner = (Spinner) getView().findViewById(R.id.targetState);
        if (targetState == null) {
            targetStateSpinner.setSelection(0);
            return;
        }

        for (int i = 0; i < selectedTargetDevice.getAvailableTargetStates().length; i++) {
            String availableTargetState = selectedTargetDevice.getAvailableTargetStates()[i];
            if (availableTargetState.equals(targetState)) {
                targetStateSpinner.setSelection(i);
                break;
            }
        }
    }

    private void setNewTargetStatesInSpinner() {
        String[] availableTargetStates = selectedTargetDevice.getAvailableTargetStates();
        targetStateAdapter.clear();

        if (availableTargetStates == null || availableTargetStates.length == 0) return;

        for (String availableTargetState : availableTargetStates) {
            targetStateAdapter.add(availableTargetState);
        }
    }

    @Override
    public void update(boolean doUpdate) {
        if (!isModify) {
            Log.e(TAG, "I can only update if a device is being modified!");
            return;
        }

        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, timerDeviceName);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS || !resultData.containsKey(BundleExtraKeys.DEVICE)) {
                    return;
                }
                Device device = (Device) resultData.getSerializable(BundleExtraKeys.DEVICE);
                if (!(device instanceof AtDevice)) {
                    Log.e(TAG, "expected an AtDevice, but got " + device);
                    return;
                }

                TimerDetailFragment.this.timerDevice = (AtDevice) device;

                setValuesForCurrentTimerDevice();

                Intent intent = new Intent(Actions.DISMISS_UPDATING_DIALOG);

                FragmentActivity activity = getActivity();
                if (activity != null) activity.sendBroadcast(intent);
            }
        });
        getActivity().startService(intent);
    }

    private void setValuesForCurrentTimerDevice() {
        final View view = getView();
        if (view == null) return;

        TextView timerName = (TextView) view.findViewById(R.id.timerName);
        timerName.setText(timerDevice.getAliasOrName());

        TextView targetDeviceName = (TextView) view.findViewById(R.id.targetDeviceName);
        targetDeviceName.setText(timerDevice.getTargetDevice());

        Spinner repetitionSpinner = (Spinner) view.findViewById(R.id.timerRepetition);
        repetitionSpinner.setSelection(timerDevice.getRepetition().ordinal());
        this.repetition = timerDevice.getRepetition();

        Spinner typeSpinner = (Spinner) view.findViewById(R.id.timerType);
        typeSpinner.setSelection(timerDevice.getTimerType().ordinal());
        this.type = timerDevice.getTimerType();

        TextView switchTimeContent = (TextView) view.findViewById(R.id.switchTimeContent);
        switchTimeContent.setText(timerDevice.getFormattedSwitchTime());

        TextView stateAppendix = (TextView) view.findViewById(R.id.stateAppendix);
        stateAppendix.setText(timerDevice.getTargetStateAddtionalInformation());

        selectTargetDeviceInSpinner(timerDevice.getTargetDevice(), timerDevice.getTargetState());
        selectTargetState(timerDevice.getTargetState());
        selectedTargetState = timerDevice.getTargetState();
        setTargetStateAppendix(timerDevice.getTargetStateAddtionalInformation());

        CheckBox isActiveCheckbox = (CheckBox) view.findViewById(R.id.isActive);
        isActiveCheckbox.setChecked(timerDevice.isActive());

        hour = timerDevice.getHours();
        minute = timerDevice.getMinutes();
        second = timerDevice.getSeconds();
    }

    private void setTargetStateAppendix(String stateAppendix) {
        DeviceStateRequiringAdditionalInformation specialDeviceState =
                DeviceStateRequiringAdditionalInformation.deviceStateForFHEM(selectedTargetState);
        updateStateAppendixVisibility(specialDeviceState);
        if (specialDeviceState == null) {
            requiresStateAppendix = false;
            return;
        }
        requiresStateAppendix = true;

        EditText appendixView = (EditText) getView().findViewById(R.id.stateAppendix);
        if (stateAppendix != null) {
            appendixView.setText(stateAppendix);
        } else {
            DeviceStateAdditionalInformationType[] requiredAppendixType = specialDeviceState.getAdditionalInformationTypes();
            if (requiredAppendixType != null && requiredAppendixType.length > 0) {
                appendixView.setText(requiredAppendixType[0].getExample());
            }
        }
    }

    private void updateStateAppendixVisibility(DeviceStateRequiringAdditionalInformation specialDeviceState) {
        EditText stateAppendix = (EditText) getView().findViewById(R.id.stateAppendix);
        if (specialDeviceState == null) {
            stateAppendix.setVisibility(View.GONE);
            stateAppendix.setText("");
        } else {
            stateAppendix.setVisibility(View.VISIBLE);
        }
    }

    private boolean updateTargetStateRowVisibility() {
        View targetDeviceRow = getView().findViewById(R.id.targetStateRow);
        if (selectedTargetDevice == null) {
            targetDeviceRow.setVisibility(View.GONE);
            return false;
        } else {
            targetDeviceRow.setVisibility(View.VISIBLE);
            return true;
        }
    }
}
