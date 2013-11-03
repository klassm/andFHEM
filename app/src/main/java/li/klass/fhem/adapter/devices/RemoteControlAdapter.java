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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.RemoteControlDevice;
import li.klass.fhem.util.ImageUtil;

public class RemoteControlAdapter extends GenericDeviceAdapter<RemoteControlDevice> {
    public RemoteControlAdapter() {
        super(RemoteControlDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        detailActions.add(new DeviceDetailViewAction<RemoteControlDevice>() {
            @Override
            public View createView(Context context, LayoutInflater inflater,
                                   RemoteControlDevice device, LinearLayout parent) {
                return createRemoteControlTable(context, device, inflater);
            }
        });
    }

    private TableLayout createRemoteControlTable(Context context, RemoteControlDevice device, LayoutInflater layoutInflater) {
        TableLayout tableLayout = new TableLayout(context);
        for (List<RemoteControlDevice.Entry> row : device.getRows()) {
            tableLayout.addView(createTableRowForRemoteControlRow(row, context, device, layoutInflater));
        }

        return tableLayout;
    }

    private TableRow createTableRowForRemoteControlRow(List<RemoteControlDevice.Entry> row,
                                                       Context context, RemoteControlDevice device,
                                                       LayoutInflater layoutInflater) {

        TableRow tableRow = new TableRow(context);

        for (RemoteControlDevice.Entry entry : row) {
            tableRow.addView(createImageViewFor(entry, context, device, layoutInflater));
        }

        return tableRow;
    }

    private View createImageViewFor(final RemoteControlDevice.Entry entry, final Context context,
                                    final RemoteControlDevice device, LayoutInflater layoutInflater) {
        ImageButton imageButton = (ImageButton) layoutInflater.inflate(R.layout.remote_control_view, null);

        ImageUtil.loadImageFromFHEMAndSetIn(imageButton, entry.getIconPath(), 120, 120);

        assert imageButton != null;
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.DEVICE_SET_STATE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, entry.command);
                context.startService(intent);
            }
        });

        return imageButton;
    }
}
