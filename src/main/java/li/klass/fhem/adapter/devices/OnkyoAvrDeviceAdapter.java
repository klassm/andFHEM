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

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.core.ToggleableAdapter;
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow;
import li.klass.fhem.adapter.devices.genericui.multimedia.MuteActionRow;
import li.klass.fhem.adapter.devices.genericui.multimedia.VolumeActionRow;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.OnkyoAvrDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry;
import li.klass.fhem.util.ApplicationProperties;

public class OnkyoAvrDeviceAdapter extends ToggleableAdapter {
    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    StateUiService stateUiService;

    public OnkyoAvrDeviceAdapter() {
        super();
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return OnkyoAvrDevice.class;
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                OnkyoAvrDevice avrDevice = (OnkyoAvrDevice) device;
                tableLayout.addView(new MuteActionRow(stateUiService)
                        .createRow(getInflater(), device, context));

                GroupSetListEntry inputSetList = (GroupSetListEntry) device.getSetList().get("input");
                tableLayout.addView(new StateChangingSpinnerActionRow(context,
                        R.string.input, R.string.input, inputSetList.getGroupStates(), avrDevice.getInput(), "input")
                        .createRow(device.getXmlListDevice(), tableLayout));

                GroupSetListEntry sleepSetList = (GroupSetListEntry) device.getSetList().get("sleep");
                tableLayout.addView(new StateChangingSpinnerActionRow(context,
                        R.string.sleep, R.string.sleep, sleepSetList.getGroupStates(), avrDevice.getSleep(), "sleep")
                        .createRow(device.getXmlListDevice(), tableLayout));
            }
        });

        registerFieldListener("volume", new FieldNameAddedToDetailListener() {
            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new VolumeActionRow(context, stateUiService, applicationProperties, device, fieldTableRow)
                        .createRow(getInflater(), device));
            }
        });
    }
}
