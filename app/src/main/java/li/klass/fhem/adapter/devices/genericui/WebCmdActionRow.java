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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ensequence.socialmediatestharness.ui.FlowLayout;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;

public class WebCmdActionRow<T extends Device<T>> {
    private String description;
    private int layout;

    public static final int LAYOUT_DETAIL = R.layout.device_detail_webcmd;
    public static final int LAYOUT_OVERVIEW = R.layout.device_overview_webcmd;

    public WebCmdActionRow(int layout) {
        this(null, layout);
    }

    public WebCmdActionRow(String description, int layout) {
        this.description = description;
        this.layout = layout;
    }

    public TableRow createRow(final Context context, LayoutInflater inflater, final T device) {
        TableRow row = (TableRow) inflater.inflate(layout, null);

        assert row != null;

        TextView descriptionView = (TextView) row.findViewById(R.id.description);
        if (descriptionView != null) {
            descriptionView.setText(description);
        }

        FlowLayout holder = (FlowLayout) row.findViewById(R.id.webcmdHolder);

        for (final String cmd : device.getWebCmd()) {
            ToggleButton button = (ToggleButton) inflater.inflate(R.layout.device_detail_togglebutton, null);
            assert button != null;

            button.setBackgroundDrawable(
                    context.getResources().getDrawable(R.drawable.theme_toggle_default_normal));

            button.setText(cmd);
            button.setTextOn(cmd);
            button.setTextOff(cmd);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Actions.DEVICE_SET_STATE);
                    intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, cmd);
                    intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
                    context.startService(intent);
                }
            });
            holder.addView(button);
        }

        return row;
    }
}
