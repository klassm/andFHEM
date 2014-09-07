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

package li.klass.fhem.adapter.timer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.ListDataAdapter;
import li.klass.fhem.domain.AtDevice;

public class TimerListAdapter extends ListDataAdapter<AtDevice> {

    public TimerListAdapter(Context context, int resource, List<AtDevice> data) {
        super(context, resource, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AtDevice device = data.get(position);

        LinearLayout view;
        if (convertView != null && convertView instanceof LinearLayout) {
            view = (LinearLayout) convertView;
        } else {
            view = (LinearLayout) inflater.inflate(resource, null);
        }

        TextView timerNameView = (TextView) view.findViewById(R.id.timerName);
        String timerName = device.getAliasOrName();
        if (!device.isActive()) {
            timerName += " (" + context.getString(R.string.deactivated) + ")";
        }
        timerNameView.setText(timerName);

        String formatString = context.getString(R.string.timer_overview);
        String repetition = context.getString(device.getRepetition().getText());
        String interval = context.getString(device.getTimerType().getText());
        String date = device.getFormattedSwitchTime();
        String targetDevice = device.getTargetDevice();
        String targetState = device.getTargetState();

        if (device.getTargetStateAddtionalInformation() != null) {
            targetState += " " + device.getTargetStateAddtionalInformation();
        }

        String content = String.format(formatString, repetition, interval, date, targetDevice, targetState);
        TextView timerContent = (TextView) view.findViewById(R.id.timerContent);
        timerContent.setText(content);

        TextView timerNextTrigger = (TextView) view.findViewById(R.id.timerNextTrigger);
        timerNextTrigger.setText(context.getString(R.string.timer_next_trigger) + ": " + device.getNextTrigger());

        int color = device.isActive() ? R.color.activeGreen : android.R.color.white;
        int colorResource = context.getResources().getColor(color);
        view.setBackgroundColor(colorResource);

        view.setTag(device);

        return view;
    }
}
