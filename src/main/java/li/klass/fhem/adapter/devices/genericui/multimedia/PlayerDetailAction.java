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

import com.google.common.base.Optional;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.DeviceDetailViewAction;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;

public class PlayerDetailAction extends DeviceDetailViewAction {

    private final StateUiService stateUiService;
    private final Optional<String> previousCommand;
    private final Optional<String> pauseCommand;
    private final Optional<String> stopCommand;
    private final Optional<String> playCommand;
    private final Optional<String> nextCommand;

    private PlayerDetailAction(Builder builder) {
        stateUiService = builder.stateUiService;
        previousCommand = builder.previousCommand;
        pauseCommand = builder.pauseCommand;
        stopCommand = builder.stopCommand;
        playCommand = builder.playCommand;
        nextCommand = builder.nextCommand;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, FhemDevice device, LinearLayout parent, String connectionId) {
        View view = inflater.inflate(R.layout.player_action, parent, false);

        fillImageButtonWithAction(context, view, device, R.id.rewind, previousCommand, connectionId);
        fillImageButtonWithAction(context, view, device, R.id.pause, pauseCommand, connectionId);
        fillImageButtonWithAction(context, view, device, R.id.stop, stopCommand, connectionId);
        fillImageButtonWithAction(context, view, device, R.id.play, playCommand, connectionId);
        fillImageButtonWithAction(context, view, device, R.id.forward, nextCommand, connectionId);

        return view;
    }

    private void fillImageButtonWithAction(final Context context, View view, final FhemDevice device,
                                           int id, final Optional<String> action, final String connectionId) {
        ImageButton button = view.findViewById(id);
        if (!action.isPresent()) {
            button.setVisibility(View.GONE);
            return;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stateUiService.setState(device, action.get(), context, connectionId);
            }
        });
    }

    public static Builder builderFor(StateUiService stateUiService) {
        return new Builder(stateUiService);
    }

    public static final class Builder {
        private StateUiService stateUiService;
        private Optional<String> previousCommand = Optional.absent();
        private Optional<String> pauseCommand = Optional.absent();
        private Optional<String> stopCommand = Optional.absent();
        private Optional<String> playCommand = Optional.absent();
        private Optional<String> nextCommand = Optional.absent();

        public Builder(StateUiService stateUiService) {
            checkNotNull(stateUiService);
            this.stateUiService = stateUiService;
        }

        public Builder withPreviousCommand(final String previousCommand) {
            this.previousCommand = fromNullable(previousCommand);
            return this;
        }

        public Builder withPauseCommand(final String pauseCommand) {
            this.pauseCommand = fromNullable(pauseCommand);
            return this;
        }

        public Builder withStopCommand(final String stopCommand) {
            this.stopCommand = fromNullable(stopCommand);
            return this;
        }

        public Builder withPlayCommand(final String playCommand) {
            this.playCommand = fromNullable(playCommand);
            return this;
        }

        public Builder withNextCommand(final String nextCommand) {
            this.nextCommand = fromNullable(nextCommand);
            return this;
        }

        public PlayerDetailAction build() {
            return new PlayerDetailAction(this);
        }
    }
}
