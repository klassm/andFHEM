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

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ToggleButton;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;

import static com.google.common.collect.Lists.newArrayList;

public class WebCmdActionRow<D extends Device<D>> extends HolderActionRow<D, String> {
    public WebCmdActionRow(int layout) {
        super(layout);
    }

    public WebCmdActionRow(String description, int layout) {
        super(description, layout);
    }

    @Override
    public List<String> getItems(D device) {
        return newArrayList(device.getWebCmd());
    }

    @Override
    public View viewFor(final String command, final D device, LayoutInflater inflater,
                        final Context context) {

        ToggleButton button = (ToggleButton) inflater.inflate(R.layout.device_detail_togglebutton, null);
        assert button != null;

        button.setBackgroundDrawable(
                context.getResources().getDrawable(R.drawable.theme_toggle_default_normal));

        button.setText(command);
        button.setTextOn(command);
        button.setTextOff(command);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.DEVICE_SET_STATE);
                intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, command);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                context.startService(intent);
            }
        });

        return button;
    }
}
