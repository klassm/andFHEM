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

package li.klass.fhem.adapter.devices.genericui.multimedia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;

public class PlayerDetailAction<T extends FhemDevice<T>> extends DeviceDetailViewAction<T> {

    private final StateUiService stateUiService;
    private final String previousCommand;
    private final String pauseCommand;
    private final String stopCommand;
    private final String playCommand;
    private final String nextCommand;

    public PlayerDetailAction(StateUiService stateUiService, String previousCommand,
                              String pauseCommand, String stopCommand,
                              String playCommand, String nextCommand) {
        this.previousCommand = previousCommand;
        this.pauseCommand = pauseCommand;
        this.stopCommand = stopCommand;
        this.playCommand = playCommand;
        this.nextCommand = nextCommand;

        this.stateUiService = stateUiService;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, T device, LinearLayout parent) {
        View view = inflater.inflate(R.layout.player_action, parent, false);

        fillImageButtonWithAction(context, view, device, R.id.rewind, previousCommand);
        fillImageButtonWithAction(context, view, device, R.id.pause, pauseCommand);
        fillImageButtonWithAction(context, view, device, R.id.stop, stopCommand);
        fillImageButtonWithAction(context, view, device, R.id.play, playCommand);
        fillImageButtonWithAction(context, view, device, R.id.forward, nextCommand);

        return view;
    }

    private void fillImageButtonWithAction(final Context context, View view, final T device,
                                           int id, final String action) {
        ImageButton button = (ImageButton) view.findViewById(id);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateUiService.setState(device, action, context);
            }
        });
    }
}
