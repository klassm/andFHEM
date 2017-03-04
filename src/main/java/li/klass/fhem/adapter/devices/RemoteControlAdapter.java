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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.ToggleableAdapter;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.RemoteControlDevice;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.util.ImageUtil;

import static li.klass.fhem.util.DisplayUtil.dpToPx;

public class RemoteControlAdapter extends ToggleableAdapter {

    @Inject
    StateUiService stateUiService;

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return RemoteControlDevice.class;
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    protected List<DeviceDetailViewAction> provideDetailActions() {
        List<DeviceDetailViewAction> detailActions = super.provideDetailActions();

        detailActions.add(new DeviceDetailViewAction() {
            @Override
            public View createView(Context context, LayoutInflater inflater,
                                   FhemDevice device, LinearLayout parent, String connectionId) {
                return createRemoteControlTable(context, device, inflater, parent, connectionId);
            }
        });

        return detailActions;
    }

    private TableLayout createRemoteControlTable(Context context, FhemDevice device,
                                                 LayoutInflater layoutInflater, LinearLayout parent, String connectionId) {
        TableLayout tableLayout = (TableLayout) getInflater().inflate(R.layout.remote_control_layout, parent, false);
        assert tableLayout != null;

        RemoteControlDevice remoteControlDevice = (RemoteControlDevice) device;
        for (RemoteControlDevice.Row row : remoteControlDevice.getRows()) {
            tableLayout.addView(createTableRowForRemoteControlRow(row, context, remoteControlDevice, layoutInflater, connectionId));
        }

        return tableLayout;
    }

    private TableRow createTableRowForRemoteControlRow(RemoteControlDevice.Row row,
                                                       Context context, RemoteControlDevice device,
                                                       LayoutInflater layoutInflater, String connectionId) {

        TableRow tableRow = new TableRow(context);

        for (RemoteControlDevice.Entry entry : row.entries) {
            tableRow.addView(createImageViewFor(entry, device, layoutInflater, tableRow, connectionId));
        }

        return tableRow;
    }

    private View createImageViewFor(final RemoteControlDevice.Entry entry,
                                    final RemoteControlDevice device, LayoutInflater layoutInflater, TableRow tableRow, final String connectionId) {
        ImageButton imageButton = (ImageButton) layoutInflater.inflate(R.layout.remote_control_view, tableRow, false);
        assert imageButton != null;

        int itemSizeInPx = (int) dpToPx(50);
        ImageUtil.loadImageFromFHEMAndSetIn(getContext(), imageButton, entry.getIconPath(), itemSizeInPx, itemSizeInPx);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stateUiService.setState(device, entry.command, getContext(), connectionId);
            }
        });

        return imageButton;
    }
}
