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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.util.List;

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
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.DialogUtil;
import li.klass.fhem.util.FhemResultReceiver;
import li.klass.fhem.widget.TimePickerWithSeconds;
import li.klass.fhem.widget.TimePickerWithSecondsDialog;

import static com.google.common.base.Preconditions.checkNotNull;
import static li.klass.fhem.constants.Actions.GET_DEVICE_FOR_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.CLICKED_DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_HOUR;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_IS_ACTIVE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_MINUTE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_REPETITION;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_SECOND;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TARGET_DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TARGET_STATE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TARGET_STATE_APPENDIX;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TYPE;

public class TimerDetailFragment extends BaseFragment {

    private static final DeviceNameSelectionFragment.DeviceFilter deviceFilter = new DeviceNameSelectionFragment.DeviceFilter() {
        @Override
        public boolean isSelectable(Device<?> device) {
            return device.getSetList().size() > 0;
        }
    };

    private static final String TAG = TimerDetailFragment.class.getName();
    public AtDevice timerDevice;
    private transient ArrayAdapter<String> targetStateAdapter;
    private String timerDeviceName;

    private transient Device targetDevice;
    private String targetState;
    private int hour = 0;
    private int minute = 0;
    private int second = 0;
    private AtDevice.AtRepetition repetition;
    private AtDevice.TimerType type;
    private boolean requiresStateAppendix;
    private String stateAppendix;
    private boolean isActive;
    private String savedTimerDeviceName;

    @Override
    public void setArguments(Bundle args) {
        if (args.containsKey(DEVICE_NAME)) {
            savedTimerDeviceName = args.getString(DEVICE_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            return view;
        }

        if (isModify()) {
            setTimerDeviceValuesForName(savedTimerDeviceName);
        }

        view = inflater.inflate(R.layout.timer_detail, container, false);

        view.findViewById(R.id.stateAppendix).setVisibility(View.GONE);
        TextView commandAppendix = (TextView) view.findViewById(R.id.stateAppendix);
        commandAppendix.setText("");

        EditText timerNameInput = getTimerNameInput(view);
        timerNameInput.setText("");
        if (isModify()) {
            timerNameInput.setEnabled(false);
        }

        TextView targetDeviceName = (TextView) view.findViewById(R.id.targetDeviceName);
        targetDeviceName.setText("");

        createRepetitionSpinner(view);
        createSelectDeviceButton(view);
        createTargetStateSpinner(view);
        createTimerTypeSpinner(view);
        createSwitchTimeButton(view);
        createSendAndResetButtons(view);
        createIsActiveCheckbox(view);


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateTargetDevice(targetDevice);
        updateTimerInformation();
    }

    private void createSelectDeviceButton(View view) {
        Button selectDeviceButton = (Button) view.findViewById(R.id.targetDeviceSet);
        selectDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.SHOW_FRAGMENT);
                intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.DEVICE_SELECTION);
                intent.putExtra(BundleExtraKeys.DEVICE_FILTER, deviceFilter);
                intent.putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode != ResultCodes.SUCCESS) return;

                        if (!resultData.containsKey(CLICKED_DEVICE)) return;

                        updateTargetDevice((Device) resultData.get(CLICKED_DEVICE));
                    }
                });
                getActivity().sendBroadcast(intent);
            }
        });
    }

    private void createIsActiveCheckbox(View view) {
        getIsActiveCheckbox(view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimerDetailFragment.this.isActive = getIsActiveCheckbox(view).isChecked();
            }
        });

    }

    private void createSendAndResetButtons(View view) {
        Button resetButton = (Button) view.findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setValuesForCurrentTimerDevice(timerDevice);
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

    private void createSwitchTimeButton(final View view) {
        Button switchTimeChangeButton = (Button) view.findViewById(R.id.switchTimeSet);
        switchTimeChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View button) {
                if (getView() == null) return;

                new TimePickerWithSecondsDialog(getActivity(), hour, minute, second, new TimePickerWithSecondsDialog.TimePickerWithSecondsListener() {
                    @Override
                    public void onTimeChanged(boolean okClicked, int newHour, int newMinute, int newSecond, String formattedText) {
                        TextView getSwitchTimeTextView = getSwitchTimeTextView(view);
                        getSwitchTimeTextView.setText(formattedText);

                        TimerDetailFragment.this.hour = newHour;
                        TimerDetailFragment.this.minute = newMinute;
                        TimerDetailFragment.this.second = newSecond;
                    }
                }).show();
            }
        });
    }

    private void createTimerTypeSpinner(View view) {
        Spinner typeSpinner = getTypeSpinner(view);

        ArrayAdapter<String> timerTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinnercontent);

        for (AtDevice.TimerType type : AtDevice.TimerType.values()) {
            timerTypeAdapter.add(view.getContext().getString(type.getText()));
        }
        typeSpinner.setAdapter(timerTypeAdapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        Spinner targetState = getTargetStateSpinner(view);
        targetStateAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinnercontent);
        targetState.setAdapter(targetStateAdapter);
        view.findViewById(R.id.targetStateRow).setVisibility(View.GONE);

        targetState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (targetDevice == null) {
                    Log.e(TAG, "cannot select new target state, as new target device is set!");
                    return;
                } else if (targetDevice.getSetList().size() == 0) {
                    Log.e(TAG, "cannot select new target state, as the new device does not contain any target states!");
                    return;
                }

                String newTargetState = targetDevice.getSetList().getSortedKeys().get(i);

                if (TimerDetailFragment.this.targetState == null || !TimerDetailFragment.this.targetState.equals(newTargetState)) {
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
        Spinner repetitionSpinner = getRepetitionSpinner(view);
        ArrayAdapter<String> repetitionAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinnercontent);
        for (AtDevice.AtRepetition atRepetition : AtDevice.AtRepetition.values()) {
            repetitionAdapter.add(view.getContext().getString(atRepetition.getText()));
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
        View view = getView();
        if (view == null) return;

        if (targetDevice == null || targetState == null) {
            Intent intent = new Intent(Actions.SHOW_TOAST);
            intent.putExtra(BundleExtraKeys.STRING_ID, R.string.incompleteConfigurationError);
            getActivity().sendBroadcast(intent);
            return;
        }

        if (!isModify()) {
            timerDeviceName = getTimerNameInput(view).getText().toString();

            if (timerDeviceName.contains(" ")) {
                DialogUtil.showAlertDialog(getActivity(), R.string.error, R.string.error_timer_name_spaces);
                return;
            }
        }

        String action = isModify() ? Actions.DEVICE_TIMER_MODIFY : Actions.DEVICE_TIMER_NEW;
        Intent intent = new Intent(action)
                .setClass(getActivity(), DeviceIntentService.class)
                .putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == ResultCodes.SUCCESS) {
                            back();
                            savedTimerDeviceName = timerDeviceName;
                        }
                    }
                })
                .putExtra(TIMER_TARGET_DEVICE_NAME, targetDevice.getName())
                .putExtra(TIMER_TARGET_STATE, targetState)
                .putExtra(TIMER_TARGET_STATE, targetState)
                .putExtra(TIMER_HOUR, hour)
                .putExtra(TIMER_MINUTE, minute)
                .putExtra(TIMER_SECOND, second)
                .putExtra(TIMER_REPETITION, repetition.name())
                .putExtra(TIMER_TYPE, type.name())
                .putExtra(TIMER_IS_ACTIVE, isActive)
                .putExtra(DEVICE_NAME, timerDeviceName);


        if (requiresStateAppendix) {
            EditText targetStateAppendixView = (EditText) view.findViewById(R.id.stateAppendix);
            intent.putExtra(TIMER_TARGET_STATE_APPENDIX, targetStateAppendixView.getText().toString());
        }


        getActivity().startService(intent);
    }

    private void updateTargetDevice(Device targetDevice) {
        if (targetDevice != null) {
            this.targetDevice = targetDevice;
            updateTargetDevice();
        }
    }

    private void updateTargetDevice() {
        if (getView() == null || targetDevice == null) {
            return;
        }

        TextView targetDeviceView = (TextView) getView().findViewById(R.id.targetDeviceName);
        if (!updateTargetStateRowVisibility()) {
            targetDeviceView.setText(R.string.unknown);
            return;
        }

        updateTargetStatesSpinner();
        selectTargetState(targetState);
        setTargetStateAppendix(stateAppendix);

        targetDeviceView.setText(targetDevice.getName());
    }

    private boolean updateTargetStateRowVisibility() {
        if (getView() == null) return false;

        View targetDeviceRow = getView().findViewById(R.id.targetStateRow);
        if (targetDevice == null) {
            targetDeviceRow.setVisibility(View.GONE);
            return false;
        } else {
            targetDeviceRow.setVisibility(View.VISIBLE);
            return true;
        }
    }

    private void updateTargetStatesSpinner() {
        List<String> availableTargetStates = targetDevice.getSetList().getSortedKeys();
        targetStateAdapter.clear();

        for (String availableTargetState : availableTargetStates) {
            targetStateAdapter.add(availableTargetState);
        }
    }

    private void selectTargetState(String targetState) {
        if (targetDevice == null || getView() == null) {
            return;
        }
        this.targetState = targetState;

        Spinner targetStateSpinner = getTargetStateSpinner(getView());
        if (targetState == null) {
            targetStateSpinner.setSelection(0);
            return;
        }

        List<String> targetStates = targetDevice.getSetList().getSortedKeys();
        for (int i = 0; i < targetStates.size(); i++) {
            String availableTargetState = targetStates.get(i);
            if (availableTargetState.equals(targetState)) {
                targetStateSpinner.setSelection(i);
                break;
            }
        }
    }

    private void setTimerDeviceValuesForName(String timerDeviceName) {
        checkNotNull(timerDeviceName);

        getActivity().startService(new Intent(GET_DEVICE_FOR_NAME)
                .setClass(getActivity(), RoomListIntentService.class)
                .putExtra(DEVICE_NAME, timerDeviceName)
                .putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode != ResultCodes.SUCCESS || !resultData.containsKey(DEVICE)) {
                            return;
                        }
                        Device device = (Device) resultData.getSerializable(DEVICE);
                        if (!(device instanceof AtDevice)) {
                            Log.e(TAG, "expected an AtDevice, but got " + device);
                            return;
                        }

                        setValuesForCurrentTimerDevice((AtDevice) device);

                        FragmentActivity activity = getActivity();
                        if (activity != null)
                            activity.sendBroadcast(new Intent(Actions.DISMISS_EXECUTING_DIALOG));
                    }
                }));
    }

    private void setValuesForCurrentTimerDevice(AtDevice atDevice) {
        this.timerDevice = atDevice;

        getActivity().startService(new Intent(GET_DEVICE_FOR_NAME)
                .setClass(getActivity(), RoomListIntentService.class)
                .putExtra(DEVICE_NAME, atDevice.getTargetDevice())
                .putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == ResultCodes.SUCCESS && resultData.containsKey(DEVICE)) {
                            updateTargetDevice((Device) resultData.get(DEVICE));
                        }
                    }
                }));


        this.stateAppendix = timerDevice.getTargetStateAddtionalInformation();
        this.targetState = timerDevice.getTargetState();


        this.repetition = timerDevice.getRepetition();
        this.type = timerDevice.getTimerType();

        this.isActive = timerDevice.isActive();

        this.hour = timerDevice.getHours();
        this.minute = timerDevice.getMinutes();
        this.second = timerDevice.getSeconds();
        this.timerDeviceName = timerDevice.getName();

        updateTimerInformation();
    }

    private void updateTimerInformation() {
        View view = getView();
        if (view != null && timerDevice != null) {
            getTypeSpinner(view).setSelection(timerDevice.getTimerType().ordinal());
            getRepetitionSpinner(view).setSelection(timerDevice.getRepetition().ordinal());
            getIsActiveCheckbox(view).setChecked(isActive);
            getSwitchTimeTextView(view).setText(TimePickerWithSeconds.getFormattedValue(hour, minute, second));
            getTimerNameInput(view).setText(timerDeviceName);
        }
    }

    private void setTargetStateAppendix(String stateAppendix) {
        if (getView() == null) return;

        DeviceStateRequiringAdditionalInformation specialDeviceState =
                DeviceStateRequiringAdditionalInformation.deviceStateForFHEM(targetState);
        updateStateAppendixVisibility(specialDeviceState);

        if (specialDeviceState == null) {
            this.requiresStateAppendix = false;
            this.stateAppendix = null;
        } else {
            this.stateAppendix = stateAppendix;
            this.requiresStateAppendix = true;

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
    }

    private void updateStateAppendixVisibility(DeviceStateRequiringAdditionalInformation specialDeviceState) {
        if (getView() == null) return;

        EditText stateAppendix = (EditText) getView().findViewById(R.id.stateAppendix);
        if (specialDeviceState == null) {
            stateAppendix.setVisibility(View.GONE);
            stateAppendix.setText("");
        } else {
            stateAppendix.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void update(boolean doUpdate) {
    }

    private EditText getTimerNameInput(View view) {
        return (EditText) view.findViewById(R.id.timerNameInput);
    }

    private Spinner getRepetitionSpinner(View view) {
        return (Spinner) view.findViewById(R.id.timerRepetition);
    }

    private CheckBox getIsActiveCheckbox(View view) {
        return (CheckBox) view.findViewById(R.id.isActive);
    }

    private Spinner getTypeSpinner(View view) {
        return (Spinner) view.findViewById(R.id.timerType);
    }

    private TextView getSwitchTimeTextView(View view) {
        return (TextView) view.findViewById(R.id.switchTimeContent);
    }

    private Spinner getTargetStateSpinner(View view) {
        return (Spinner) view.findViewById(R.id.targetStateSpinner);
    }

    private boolean isModify() {
        return !Strings.isNullOrEmpty(savedTimerDeviceName);
    }
}
