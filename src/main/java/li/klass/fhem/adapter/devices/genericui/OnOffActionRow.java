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
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.common.base.Optional;

import java.util.Map;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.UpdatingResultReceiver;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.service.intent.DeviceIntentService;

public class OnOffActionRow<T extends FhemDevice> {

    public static final String HOLDER_KEY = "OnOffActionRow";
    public static final int LAYOUT_DETAIL = R.layout.device_detail_onoffbuttonrow;
    public static final int LAYOUT_OVERVIEW = R.layout.device_overview_onoffbuttonrow;
    private final int layoutId;
    private final Optional<Integer> description;


    public OnOffActionRow(int layoutId) {
        this(layoutId, Optional.<Integer>absent());
    }

    public OnOffActionRow(int layoutId, int description) {
        this(layoutId, Optional.of(description));
    }

    public OnOffActionRow(int layoutId, Optional<Integer> description) {
        this.layoutId = layoutId;
        this.description = description;
    }

    @SuppressWarnings("unchecked")
    public TableRow createRow(LayoutInflater inflater, final T device, Context context) {
        TableRow tableRow = (TableRow) inflater.inflate(layoutId, null);
        TextView descriptionView = ((TextView) tableRow.findViewById(R.id.description));
        Button onButton = findOnButton(tableRow);
        Button offButton = findOffButton(tableRow);

        String text = description.isPresent() ? context.getString(description.get()) : device.getAliasOrName();
        descriptionView.setText(text);

        String onStateName = getOnStateName(device, context);
        onButton.setOnClickListener(createListener(context, device, onStateName));
        onButton.setText(getOnStateText(device, context));

        String offStateName = getOffStateName(device, context);
        offButton.setOnClickListener(createListener(context, device, offStateName));
        offButton.setText(getOffStateText(device, context));

        if (isOn(device)) {
            onButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.theme_toggle_on_normal));
            offButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.theme_toggle_default_normal));
        } else {
            onButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.theme_toggle_default_normal));
            offButton.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.theme_toggle_off_normal));
        }

        return tableRow;
    }

    protected Button findOffButton(TableRow tableRow) {
        return (Button) tableRow.findViewById(R.id.offButton);
    }

    protected Button findOnButton(TableRow tableRow) {
        return (Button) tableRow.findViewById(R.id.onButton);
    }

    protected String getOnStateName(T device, Context context) {
        return "on";
    }

    protected String getOffStateName(T device, Context context) {
        return "off";
    }

    protected String getOnStateText(T device, Context context) {
        @SuppressWarnings("unchecked")
        Map<String, String> eventMap = device.getEventMap();

        String onStateName = getOnStateName(device, context);
        return eventMap.containsKey(onStateName) ? eventMap.get(onStateName) : context.getString(R.string.on);
    }

    protected String getOffStateText(T device, Context context) {
        @SuppressWarnings("unchecked")
        Map<String, String> eventMap = device.getEventMap();

        String offStateName = getOffStateName(device, context);
        return eventMap.containsKey(offStateName) ? eventMap.get(offStateName) : context.getString(R.string.off);
    }

    protected boolean isOn(T device) {
        return false;
    }

    private ToggleButton.OnClickListener createListener(final Context context, final T device, final String targetState) {
        return new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(context, device, targetState);
            }
        };
    }

    public void onButtonClick(final Context context, T device, String targetState) {
        Intent intent = new Intent(Actions.DEVICE_SET_STATE);
        intent.setClass(context, DeviceIntentService.class);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, targetState);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new UpdatingResultReceiver(context));

        context.startService(intent);
    }
}
