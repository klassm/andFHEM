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

package li.klass.fhem.adapter.devices.genericui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.SetListGroupValue;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.domain.setlist.SetListValue;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.util.DialogUtil;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.base.Preconditions.checkNotNull;
import static li.klass.fhem.domain.core.DeviceStateRequiringAdditionalInformation.isValidAdditionalInformationValue;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class AvailableTargetStatesDialogUtil {

    public static final TargetStateSelectedCallback STATE_SENDING_CALLBACK = new TargetStateSelectedCallback() {
        @Override
        public <D extends FhemDevice<?>> void onTargetStateSelected(String state, String subState, D device, Context context) {
            if (isBlank(state)) return;

            if (!isBlank(subState)) {
                switchDeviceSubState(state, subState, device, context);
            } else {
                switchDeviceState(state, device, context);
            }
        }

        private <D extends FhemDevice<?>> void switchDeviceState(String newState, D device, final Context context) {
            context.startService(new Intent(Actions.DEVICE_SET_STATE)
                    .setClass(context, DeviceIntentService.class)
                    .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                    .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, newState)
                    .putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context)));
        }

        private <D extends FhemDevice<?>> void switchDeviceSubState(String newState, String newSubState, D device, final Context context) {
            if (newState.equalsIgnoreCase("state") || isEmpty(newSubState)) {
                switchDeviceState(newSubState, device, context);
                return;
            }
            context.startService(new Intent(Actions.DEVICE_SET_SUB_STATE)
                    .setClass(context, DeviceIntentService.class)
                    .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                    .putExtra(BundleExtraKeys.STATE_NAME, newState)
                    .putExtra(BundleExtraKeys.STATE_VALUE, newSubState)
                    .putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context)));
        }
    };

    public static <D extends FhemDevice<D>> void showSwitchOptionsMenu(final Context context, final D device, final TargetStateSelectedCallback callback) {
        AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
        contextMenu.setTitle(context.getResources().getString(R.string.switchDevice));
        final List<String> setOptions = device.getSetList().getSortedKeys();
        final String[] eventMapOptions = device.getAvailableTargetStatesEventMapTexts();

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                final String option = setOptions.get(position);

                if (handleSelectedOption(context, device, option, callback)) return;

                dialog.dismiss();
            }
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.list_item_with_arrow, eventMapOptions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(context, R.layout.list_item_with_arrow, null);
                }
                assert convertView != null;
                TextView textView = (TextView) convertView.findViewById(R.id.text);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.image);

                textView.setText(getItem(position));

                String setOption = setOptions.get(position);
                SetList setList = device.getSetList();
                final SetListValue setListValue = setList.get(setOption);

                imageView.setVisibility(setListValue instanceof SetListGroupValue ? VISIBLE : GONE);

                return convertView;
            }
        };
        contextMenu.setAdapter(adapter, clickListener);

        contextMenu.show();
    }

    public static <D extends FhemDevice<?>> TypeHandler<D>
    handlerForSelectedOption(D device, Context context,
                             final String option,
                             final TargetStateSelectedCallback callback) {

        SetList setList = device.getSetList();
        final SetListValue setListValue = setList.get(option);

        final DeviceStateRequiringAdditionalInformation specialDeviceState =
                DeviceStateRequiringAdditionalInformation.deviceStateForFHEM(option);

        if (setListValue instanceof SetListSliderValue) {
            final SetListSliderValue sliderValue = (SetListSliderValue) setListValue;
            return new TypeHandler<D>() {
                private float dimProgress = 0;

                @Override
                public View getContentViewFor(Context context, D device) {
                    float initialProgress = 0;
                    if (device instanceof DimmableDevice) {
                        initialProgress = ((DimmableDevice) device).getDimPosition();
                    }

                    LayoutInflater inflater = getInflater(context);
                    @SuppressLint("InflateParams") TableLayout tableLayout = (TableLayout) inflater.inflate(R.layout.availabletargetstates_action_with_seekbar, null, false);

                    TableRow updateRow = (TableRow) tableLayout.findViewById(R.id.updateRow);
                    assert updateRow != null;
                    ((TextView) updateRow.findViewById(R.id.description)).setText("");
                    ((TextView) updateRow.findViewById(R.id.value)).setText("");

                    tableLayout.addView(new DeviceDimActionRowFullWidth(initialProgress,
                            sliderValue.getStart(), sliderValue.getStep(), sliderValue.getStop(),
                            updateRow, R.layout.device_detail_seekbarrow_full_width) {

                        @Override
                        public void onStopDim(Context context, XmlListDevice device, float progress) {
                            dimProgress = progress;
                        }

                        @Override
                        public String toDimUpdateText(XmlListDevice device, float progress) {
                            return progress + "";
                        }
                    }.createRow(inflater, device));
                    return tableLayout;
                }

                @Override
                public boolean onPositiveButtonClick(View view, Context context, D device) {
                    callback.onTargetStateSelected(option, "" + dimProgress, device, context);
                    return true;
                }
            };
        } else if (setListValue instanceof SetListGroupValue) {
            final SetListGroupValue groupValue = (SetListGroupValue) setListValue;
            final List<String> groupStates = groupValue.getGroupStates();
            return new TypeHandler<D>() {

                @Override
                public View getContentViewFor(final Context context, final D device) {
                    ListView listView = new ListView(context);
                    listView.setAdapter(new ArrayAdapter<>(context,
                            android.R.layout.simple_list_item_1, groupStates));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                            String selection = groupStates.get(position);
                            callback.onTargetStateSelected(option, selection, device, context);
                            dialog.dismiss();
                        }
                    });

                    return listView;
                }

                @Override
                boolean requiresPositiveButton() {
                    return false;
                }
            };
        } else if (specialDeviceState != null) {
            return new TypeHandler<D>() {

                private EditText editText;

                @Override
                public View getContentViewFor(Context context, D device) {
                    editText = new EditText(context);
                    return editText;
                }

                @Override
                public boolean onPositiveButtonClick(View view, Context context, D device) {
                    Editable value = editText.getText();
                    String text = value == null ? "" : value.toString();

                    if (isValidAdditionalInformationValue(text, specialDeviceState)) {
                        callback.onTargetStateSelected(option, text, device, context);
                        return true;
                    } else {
                        DialogUtil.showAlertDialog(context, R.string.error, R.string.invalidInput);
                        return false;
                    }
                }
            };
        } else {
            callback.onTargetStateSelected(option, null, device, context);
            return null;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static
    @NotNull
    LayoutInflater getInflater(Context context) {
        return checkNotNull(LayoutInflater.from(context));
    }


    public static <D extends FhemDevice<?>> boolean handleSelectedOption(final Context context, final D device, String option, TargetStateSelectedCallback callback) {
        final TypeHandler<D> typeHandler = handlerForSelectedOption(device, context, option, callback);
        if (typeHandler == null) {
            return true;
        }
        final View view = typeHandler.getContentViewFor(context, device);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.stateAppendix)
                .setView(view)
                .setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        if (typeHandler.requiresPositiveButton()) {
            builder.setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (typeHandler.onPositiveButtonClick(view, context, device)) {
                        dialog.dismiss();
                    }
                }
            });
        }

        AlertDialog subDialog = builder.show();
        typeHandler.setDialog(subDialog);
        return false;
    }

    public interface TargetStateSelectedCallback {
        <D extends FhemDevice<?>> void onTargetStateSelected(String state, String subState, D device, Context context);
    }

    public static abstract class TypeHandler<D> {
        Dialog dialog;

        abstract View getContentViewFor(Context context, D device);

        boolean onPositiveButtonClick(View view, Context context, D device) {
            return true;
        }

        boolean requiresPositiveButton() {
            return true;
        }

        public void setDialog(Dialog dialog) {
            this.dialog = dialog;
        }
    }
}
