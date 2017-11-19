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

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.devices.genericui.StateChangingSpinnerActionRow;
import li.klass.fhem.adapter.devices.genericui.multimedia.PlayerDetailAction;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.HarmonyDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.typeEntry.GroupSetListEntry;

public class HarmonyDeviceAdapter extends ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow {
    public HarmonyDeviceAdapter() {
        super();
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return HarmonyDevice.class;
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, String connectionId, TableRow fieldTableRow) {
                GroupSetListEntry inputSetList = (GroupSetListEntry) device.getXmlListDevice().getSetList().get("activity", true);
                tableLayout.addView(new StateChangingSpinnerActionRow(context,
                        R.string.activity, R.string.activity, inputSetList.getGroupStates(), ((HarmonyDevice) device).getActivity(), "activity")
                        .createRow(device.getXmlListDevice(), connectionId, tableLayout));
            }
        });
    }

    @Override
    protected List<DeviceDetailViewAction> provideDetailActions() {
        List<DeviceDetailViewAction> detailActions = super.provideDetailActions();

        //noinspection unchecked
        detailActions.add(PlayerDetailAction.builderFor(stateUiService)
                .withPreviousCommand("special previousTrack")
                .withStopCommand("special stop")
                .withPlayCommand("special playPause")
                .withNextCommand("special nextTrack")
                .build());

        return detailActions;
    }
}
