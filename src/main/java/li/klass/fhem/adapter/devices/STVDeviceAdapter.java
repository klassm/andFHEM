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
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.genericui.OnOffActionRow;
import li.klass.fhem.adapter.devices.genericui.StateChangingSeekBarFullWidth;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.STVDevice;
import li.klass.fhem.domain.setlist.SetListSliderValue;
import li.klass.fhem.util.ApplicationProperties;

public class STVDeviceAdapter extends GenericDeviceAdapter<STVDevice> {
    @Inject
    ApplicationProperties applicationProperties;

    @Inject
    StateUiService stateUiService;

    public STVDeviceAdapter() {
        super(STVDevice.class);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();

        registerFieldListener("state", new FieldNameAddedToDetailListener<STVDevice>() {
            @Override
            public void onFieldNameAdded(final Context context, TableLayout tableLayout, String field,
                                         final STVDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new OnOffActionRow<STVDevice>(OnOffActionRow.LAYOUT_DETAIL, R.string.musicMute) {
                    @Override
                    protected String getOnStateText(STVDevice device, Context context) {
                        return context.getString(R.string.yes);
                    }

                    @Override
                    protected String getOffStateText(STVDevice device, Context context) {
                        return context.getString(R.string.no);
                    }

                    @Override
                    public void onButtonClick(Context context, STVDevice device, String targetState) {
                        stateUiService.setSubState(device, "mute", targetState);
                    }

                    @Override
                    protected boolean isOn(STVDevice device) {
                        return device.isMuted();
                    }
                }.createRow(getInflater(), device, context));
            }
        });

        registerFieldListener("volume", new FieldNameAddedToDetailListener<STVDevice>() {
            @Override
            protected void onFieldNameAdded(Context context, TableLayout tableLayout, String field, STVDevice device, TableRow fieldTableRow) {
                SetListSliderValue volumeSetListEntry = (SetListSliderValue) device.getSetList().get("volume");
                tableLayout.addView(new StateChangingSeekBarFullWidth<STVDevice>(context, device.getVolumeAsInt(), volumeSetListEntry, "volume", fieldTableRow, applicationProperties) {

                }.createRow(getInflater(), device));
            }
        });
    }
}
