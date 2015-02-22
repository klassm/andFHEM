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
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.common.base.Optional;

import java.util.Map;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow;
import li.klass.fhem.adapter.devices.genericui.ToggleActionRow;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.OnkyoAvrDevice;
import li.klass.fhem.domain.setlist.SetListGroupValue;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.util.ApplicationProperties;

public class OnkyoAvrDeviceAdapter extends ToggleableAdapterWithSwitchActionRow<OnkyoAvrDevice> {
    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    StateUiService stateUiService;

    public OnkyoAvrDeviceAdapter() {
        super(OnkyoAvrDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener<OnkyoAvrDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, OnkyoAvrDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new ToggleActionRow<OnkyoAvrDevice>(getInflater(), ToggleActionRow.LAYOUT_DETAIL) {
                    @Override
                    protected Optional<String> getOnStateText(Map<String, String> eventMap) {
                        return Optional.of(getContext().getString(R.string.yes));
                    }

                    @Override
                    protected Optional<String> getOffStateText(Map<String, String> eventMap) {
                        return Optional.of(getContext().getString(R.string.no));
                    }

                    @Override
                    protected boolean isOn(OnkyoAvrDevice device) {
                        return device.isMuted();
                    }

                    @Override
                    protected void onButtonClick(Context context, OnkyoAvrDevice device, boolean isChecked) {
                        stateUiService.setSubState(device, "mute", isChecked ? "on" : "off", context);
                    }
                }
                        .createRow(context, device, context.getString(R.string.musicMute)));

                SetListGroupValue inputSetList = (SetListGroupValue) device.getSetList().get("input");
                tableLayout.addView(new StateChangingSpinnerActionRow<OnkyoAvrDevice>(context,
                        R.string.input, R.string.input, inputSetList.getGroupStates(), device.getInput(), "input")
                        .createRow(device, tableLayout));

                SetListGroupValue sleepSetList = (SetListGroupValue) device.getSetList().get("sleep");
                tableLayout.addView(new StateChangingSpinnerActionRow<OnkyoAvrDevice>(context,
                        R.string.sleep, R.string.sleep, sleepSetList.getGroupStates(), device.getSleep(), "sleep")
                        .createRow(device, tableLayout));
            }
        });

        registerFieldListener("volume", new FieldNameAddedToDetailListener<OnkyoAvrDevice>() {
            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field, OnkyoAvrDevice device, TableRow fieldTableRow) {
                SetListSliderValue volumeSetList = (SetListSliderValue) device.getSetList().get("volume");
                tableLayout.addView(new StateChangingSeekBarFullWidth<OnkyoAvrDevice>(context,
                        device.getVolumeProgress(), volumeSetList, "volume", fieldTableRow, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });
    }
}
