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

package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.content.Intent;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.common.base.Optional;

import java.util.Map;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow;
import li.klass.fhem.adapter.devices.genericui.ToggleActionRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.PioneerAvrDevice;
import li.klass.fhem.domain.setlist.SetListGroupValue;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.service.intent.DeviceIntentService;
import li.klass.fhem.util.ApplicationProperties;

public class PioneerAvrDeviceAdapter extends ToggleableAdapterWithSwitchActionRow<PioneerAvrDevice> {
    @Inject
    ApplicationProperties applicationProperties;

    public PioneerAvrDeviceAdapter() {
        super(PioneerAvrDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener<PioneerAvrDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, PioneerAvrDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new ToggleActionRow<PioneerAvrDevice>(context, R.string.musicMute, ToggleActionRow.LAYOUT_DETAIL) {
                    @Override
                    protected Optional<String> getOnStateText(Map<String, String> eventMap) {
                        return Optional.of(getContext().getString(R.string.yes));
                    }

                    @Override
                    protected Optional<String> getOffStateText(Map<String, String> eventMap) {
                        return Optional.of(getContext().getString(R.string.no));
                    }

                    @Override
                    protected boolean isOn(PioneerAvrDevice device) {
                        return device.isMuted();
                    }

                    @Override
                    protected void onButtonClick(Context context, PioneerAvrDevice device, boolean isChecked) {
                        Intent intent = new Intent(Actions.DEVICE_SET_SUB_STATE)
                                .setClass(context, DeviceIntentService.class)
                                .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                                .putExtra(BundleExtraKeys.STATE_NAME, "mute")
                                .putExtra(BundleExtraKeys.STATE_VALUE, isChecked ? "on" : "off")
                                .putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context));

                        context.startService(intent);
                    }
                }
                        .createRow(context, getInflater(), device));

                SetListGroupValue inputSetList = (SetListGroupValue) device.getSetList().get("input");
                tableLayout.addView(new StateChangingSpinnerActionRow<PioneerAvrDevice>(context,
                        R.string.input, R.string.input, inputSetList.getGroupStates(), device.getInput(), "input")
                        .createRow(device, tableLayout));
            }
        });

        registerFieldListener("volume", new FieldNameAddedToDetailListener<PioneerAvrDevice>() {
            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field, PioneerAvrDevice device, TableRow fieldTableRow) {
                SetListSliderValue volumeSetList = (SetListSliderValue) device.getSetList().get("volume");
                tableLayout.addView(new StateChangingSeekBarFullWidth<PioneerAvrDevice>(context,
                        device.getVolumeProgress(), volumeSetList, "volume", fieldTableRow, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });
    }
}
