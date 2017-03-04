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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.OnTargetStateSelectedCallback;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.AtDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.fragments.device.DeviceNameSelectionFragment;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.DialogUtil;
import li.klass.fhem.util.FhemResultReceiver;
import li.klass.fhem.widget.TimePickerWithSecondsDialog;

import static com.google.common.base.Preconditions.checkNotNull;
import static li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesDialogUtil.showSwitchOptionsMenu;
import static li.klass.fhem.constants.Actions.DEVICE_TIMER_MODIFY;
import static li.klass.fhem.constants.Actions.DEVICE_TIMER_NEW;
import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.GET_DEVICE_FOR_NAME;
import static li.klass.fhem.constants.Actions.SHOW_FRAGMENT;
import static li.klass.fhem.constants.Actions.SHOW_TOAST;
import static li.klass.fhem.constants.BundleExtraKeys.CLICKED_DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE;
import static li.klass.fhem.constants.BundleExtraKeys.DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.FRAGMENT;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.BundleExtraKeys.STRING_ID;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_HOUR;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_IS_ACTIVE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_MINUTE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_REPETITION;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_SECOND;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TARGET_DEVICE_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TARGET_STATE;
import static li.klass.fhem.constants.BundleExtraKeys.TIMER_TYPE;
import static li.klass.fhem.fragments.FragmentType.DEVICE_SELECTION;
import static li.klass.fhem.widget.TimePickerWithSeconds.getFormattedValue;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class TimerDetailFragment extends BaseFragment {

    private static final DeviceNameSelectionFragment.DeviceFilter DEVICE_FILTER = new DeviceNameSelectionFragment.DeviceFilter() {
        @Override
        public boolean isSelectable(FhemDevice<?> device) {
            return device.getSetList().size() > 0;
        }
    };

    private static final String TAG = TimerDetailFragment.class.getName();
    public AtDevice timerDevice;

    private transient FhemDevice targetDevice;
    private String savedTimerDeviceName;
    private String connectionId;

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }

    @Override
    public void setArguments(Bundle args) {
        if (args.containsKey(DEVICE_NAME)) {
            savedTimerDeviceName = args.getString(DEVICE_NAME);
        }
        connectionId = args.getString(CONNECTION_ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.timer_detail, container, false);

        bindRepetitionSpinner(view);
        bindSelectDeviceButton(view);
        bindTimerTypeSpinner(view);
        bindSwitchTimeButton(view);
        bindIsActiveCheckbox(view);
        bindTargetStateButton(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText timerNameInput = getTimerNameInput(view);
        setTimerName("", view);
        if (isModify()) {
            timerNameInput.setEnabled(false);
        }
        setTargetDeviceName("", view);

        if (isModify()) {
            setTimerDeviceValuesForName(savedTimerDeviceName);
        }

        updateTargetDevice(targetDevice, view);
        updateTimerInformation(timerDevice);
        updateTargetStateRowVisibility(view);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.timer_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                setValuesForCurrentTimerDevice(timerDevice);
                return true;
            case R.id.save:
                save();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void bindTargetStateButton(final View view) {
        getTargetStateChangeButton(view).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                showSwitchOptionsMenu(getActivity(), targetDevice, new OnTargetStateSelectedCallback() {
                    @Override
                    public void onStateSelected(FhemDevice device, String targetState) {
                        setTargetState(targetState, view);
                    }

                    @Override
                    public void onSubStateSelected(FhemDevice device, String state, String subState) {
                        onStateSelected(device, state + " " + subState);
                    }

                    @Override
                    public void onNothingSelected(FhemDevice device) {
                    }
                });
            }
        });
    }

    private void bindSelectDeviceButton(final View view) {
        getTargetDeviceChangeButton(view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View button) {
                getActivity().sendBroadcast(new Intent(SHOW_FRAGMENT)
                        .putExtra(FRAGMENT, DEVICE_SELECTION)
                        .putExtra(BundleExtraKeys.DEVICE_FILTER, DEVICE_FILTER)
                        .putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                if (resultCode != ResultCodes.SUCCESS) return;

                                if (!resultData.containsKey(CLICKED_DEVICE)) return;

                                updateTargetDevice((FhemDevice) resultData.get(CLICKED_DEVICE), view);
                            }
                        }));
            }
        });
    }

    private void bindIsActiveCheckbox(View view) {
        CheckBox isActiveCheckbox = getIsActiveCheckbox(view);
        if (!isModify()) {
            isActiveCheckbox.setChecked(true);
            isActiveCheckbox.setEnabled(false);
        }
    }

    private void bindSwitchTimeButton(final View view) {
        Button switchTimeChangeButton = (Button) view.findViewById(R.id.switchTimeSet);
        switchTimeChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View button) {
                SwitchTime switchTime = getSwitchTime(view).or(new SwitchTime(0, 0, 0));
                new TimePickerWithSecondsDialog(getActivity(), switchTime.hour, switchTime.minute, switchTime.second, new TimePickerWithSecondsDialog.TimePickerWithSecondsListener() {
                    @Override
                    public void onTimeChanged(boolean okClicked, int newHour, int newMinute, int newSecond, String formattedText) {
                        setSwitchTime(newHour, newMinute, newSecond, view);
                    }
                }).show();
            }
        });
    }

    private void bindTimerTypeSpinner(View view) {
        Spinner typeSpinner = getTypeSpinner(view);

        ArrayAdapter<String> timerTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinnercontent);

        for (AtDevice.TimerType type : AtDevice.TimerType.values()) {
            timerTypeAdapter.add(view.getContext().getString(type.getText()));
        }
        typeSpinner.setAdapter(timerTypeAdapter);
    }

    private void bindRepetitionSpinner(View view) {
        Spinner repetitionSpinner = getRepetitionSpinner(view);
        ArrayAdapter<String> repetitionAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinnercontent);
        for (AtDevice.AtRepetition atRepetition : AtDevice.AtRepetition.values()) {
            repetitionAdapter.add(view.getContext().getString(atRepetition.getText()));
        }
        repetitionSpinner.setAdapter(repetitionAdapter);
    }

    private void save() {
        View view = getView();
        if (view == null) return;

        Optional<SwitchTime> switchTimeOptional = getSwitchTime(view);
        if (targetDevice == null || isBlank(getTargetState(view)) || !switchTimeOptional.isPresent()) {
            getActivity().sendBroadcast(new Intent(SHOW_TOAST)
                    .putExtra(STRING_ID, R.string.incompleteConfigurationError));
            return;
        }

        final String timerDeviceName = getTimerName(view);
        if (!isModify()) {
            if (timerDeviceName.contains(" ")) {
                DialogUtil.showAlertDialog(getActivity(), R.string.error, R.string.error_timer_name_spaces);
                return;
            }
        }

        String action = isModify() ? DEVICE_TIMER_MODIFY : DEVICE_TIMER_NEW;
        SwitchTime switchTime = switchTimeOptional.get();

        getActivity().startService(new Intent(action)
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
                .putExtra(TIMER_TARGET_STATE, getTargetState(view))
                .putExtra(TIMER_HOUR, switchTime.hour)
                .putExtra(TIMER_MINUTE, switchTime.minute)
                .putExtra(TIMER_SECOND, switchTime.second)
                .putExtra(TIMER_REPETITION, getRepetition(view).name())
                .putExtra(TIMER_TYPE, getType(view).name())
                .putExtra(TIMER_IS_ACTIVE, getIsActive(view))
                .putExtra(DEVICE_NAME, timerDeviceName));
    }

    private void updateTargetDevice(FhemDevice targetDevice, View view) {
        if (view == null || targetDevice == null) {
            return;
        }
        setTargetDeviceName(targetDevice.getName(), view);
        TimerDetailFragment.this.targetDevice = targetDevice;

        if (!updateTargetStateRowVisibility(view)) {
            setTargetState(getString(R.string.unknown), view);
        }
    }

    private boolean updateTargetStateRowVisibility(View view) {
        if (view == null) return false;

        View targetDeviceRow = view.findViewById(R.id.targetStateRow);
        if (targetDevice == null) {
            targetDeviceRow.setVisibility(View.GONE);
            return false;
        } else {
            targetDeviceRow.setVisibility(View.VISIBLE);
            return true;
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
                        FhemDevice device = (FhemDevice) resultData.getSerializable(DEVICE);
                        if (!(device instanceof AtDevice)) {
                            Log.e(TAG, "expected an AtDevice, but got " + device);
                            return;
                        }

                        setValuesForCurrentTimerDevice((AtDevice) device);

                        FragmentActivity activity = getActivity();
                        if (activity != null)
                            activity.sendBroadcast(new Intent(DISMISS_EXECUTING_DIALOG));
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
                            updateTargetDevice((FhemDevice) resultData.get(DEVICE), TimerDetailFragment.this.getView());
                        }
                    }
                }));

        updateTimerInformation(timerDevice);
    }

    private void updateTimerInformation(AtDevice timerDevice) {
        View view = getView();
        if (view != null && timerDevice != null) {
            setType(timerDevice.getTimerType(), view);
            setRepetition(timerDevice.getRepetition(), view);
            setIsActive(timerDevice.isActive(), view);
            setSwitchTime(timerDevice.getHours(), timerDevice.getMinutes(), timerDevice.getSeconds(), view);
            setTimerName(timerDevice.getName(), view);
            setTargetState(Joiner.on(" ").skipNulls().join(timerDevice.getTargetState(), timerDevice.getTargetStateAddtionalInformation()), view);
        }
    }

    private void setTimerName(String timerDeviceName, View view) {
        getTimerNameInput(view).setText(timerDeviceName);
    }

    private String getTimerName(View view) {
        return getTimerNameInput(view).getText().toString();
    }

    private void setTargetState(String targetState, View view) {
        getTargetStateTextView(view).setText(targetState);
    }

    private String getTargetState(View view) {
        return getTargetStateTextView(view).getText().toString();
    }

    private void setSwitchTime(int hour, int minute, int second, View view) {
        getSwitchTimeTextView(view).setText(getFormattedValue(hour, minute, second));
    }

    private Optional<SwitchTime> getSwitchTime(View view) {
        String text = getSwitchTimeTextView(view).getText().toString();
        ImmutableList<String> parts = ImmutableList.copyOf(Splitter.on(":").split(text));
        if (parts.size() != 3) {
            return Optional.absent();
        }
        return Optional.of(new SwitchTime(
                Integer.parseInt(parts.get(0)),
                Integer.parseInt(parts.get(1)),
                Integer.parseInt(parts.get(2))
        ));
    }

    private void setIsActive(boolean isActive, View view) {
        getIsActiveCheckbox(view).setChecked(isActive);
    }

    private boolean getIsActive(View view) {
        return getIsActiveCheckbox(view).isChecked();
    }

    private void setRepetition(AtDevice.AtRepetition repetition, View view) {
        getRepetitionSpinner(view).setSelection(repetition.ordinal());
    }

    private AtDevice.AtRepetition getRepetition(View view) {
        return AtDevice.AtRepetition.values()[getRepetitionSpinner(view).getSelectedItemPosition()];
    }

    private void setType(AtDevice.TimerType type, View view) {
        getTypeSpinner(view).setSelection(type.ordinal());
    }

    private AtDevice.TimerType getType(View view) {
        return AtDevice.TimerType.values()[getTypeSpinner(view).getSelectedItemPosition()];
    }

    @Override
    public void update(boolean doUpdate) {
    }

    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.timer);
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

    private TextView getTargetDeviceTextView(View view) {
        return (TextView) view.findViewById(R.id.targetDeviceName);
    }

    private Button getTargetDeviceChangeButton(View view) {
        return (Button) view.findViewById(R.id.targetDeviceSet);
    }

    private EditText getTargetStateTextView(View view) {
        return (EditText) view.findViewById(R.id.targetState);
    }

    private Button getTargetStateChangeButton(View view) {
        return (Button) view.findViewById(R.id.targetStateSet);
    }

    private void setTargetDeviceName(String string, View view) {
        getTargetDeviceTextView(view).setText(string);
    }

    private boolean isModify() {
        return !Strings.isNullOrEmpty(savedTimerDeviceName);
    }

    private static class SwitchTime {
        final int hour;
        final int minute;
        final int second;

        SwitchTime(int hour, int minute, int second) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }
    }
}
