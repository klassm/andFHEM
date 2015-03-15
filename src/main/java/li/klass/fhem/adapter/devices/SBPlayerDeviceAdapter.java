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

import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.core.ToggleableAdapter;
import li.klass.fhem.adapter.devices.genericui.multimedia.MuteActionRow;
import li.klass.fhem.adapter.devices.genericui.multimedia.PlayerDetailAction;
import li.klass.fhem.adapter.devices.genericui.multimedia.VolumeActionRow;
import li.klass.fhem.domain.SBPlayerDevice;
import li.klass.fhem.util.ApplicationProperties;

public class SBPlayerDeviceAdapter extends ToggleableAdapter<SBPlayerDevice> {
    @Inject
    ApplicationProperties applicationProperties;

    public SBPlayerDeviceAdapter() {
        super(SBPlayerDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {

        registerFieldListener("state", new FieldNameAddedToDetailListener<SBPlayerDevice>() {
            @Override
            public void onFieldNameAdded(final Context context, TableLayout tableLayout, String field,
                                         final SBPlayerDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new MuteActionRow<SBPlayerDevice>(stateUiService)
                        .createRow(getInflater(), device, context));
            }
        });

        registerFieldListener("volume", new FieldNameAddedToDetailListener<SBPlayerDevice>() {

            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, SBPlayerDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new VolumeActionRow<>(context, device, applicationProperties)
                        .createRow(getInflater(), device));
            }
        });

        detailActions.add(new PlayerDetailAction<SBPlayerDevice>(stateUiService, "previous", "pause", "stop", "play", "next"));
    }
}
