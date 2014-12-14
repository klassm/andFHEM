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

package li.klass.fhem.adapter.weekprofile;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.TemperatureChangeTableRow;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DialogUtil;

import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;

public class IntervalWeekProfileAdapter
        extends BaseWeekProfileAdapter<FilledTemperatureInterval> {

    private final ApplicationProperties applicationProperties;

    public IntervalWeekProfileAdapter(Context context, ApplicationProperties applicationProperties) {
        super(context);
        this.applicationProperties = applicationProperties;
    }

    @Override
    protected int getNumberOfAdditionalChildrenForParent() {
        return 1;
    }

    @Override
    protected View getChildView(final DayProfile<FilledTemperatureInterval, ?, ?> parent, int parentPosition,
                                final FilledTemperatureInterval child, View v, ViewGroup viewGroup, final int relativeChildPosition) {

        if (child == null) {
            return addView(parent, viewGroup);
        }

        final View view = layoutInflater.inflate(R.layout.weekprofile_interval_item, viewGroup, false);
        assert view != null;

        boolean isNew = child.isNew();

        int intervalStringId = weekProfile.getIntervalType().stringId;
        TextView intervalTypeTextView = (TextView) view.findViewById(R.id.intervalType);
        intervalTypeTextView.setText(intervalStringId);

        setDetailTextView(view, R.id.time, child.getChangedSwitchTime(), child.getSwitchTime(), isNew);
        setDetailTextView(view, R.id.temperature, appendTemperature(child.getChangedTemperature()),
                appendTemperature(child.getTemperature()), isNew);

        setTemperatureAndInterval(view, R.id.set, child, viewGroup, new OnIntervalTemperatureChangedListener() {
            @Override
            public void onIntervalTemperatureChanged(String time, double temperature) {
                child.setChangedTemperature(temperature);

                if (!child.isTimeFixed()) {
                    child.setChangedSwitchTime(time);
                }
                notifyWeekProfileChangedListener();
            }
        });

        Button deleteButton = (Button) view.findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtil.showConfirmBox(context, R.string.areYouSure, R.string.deleteConfirmIntervalText, new DialogUtil.AlertOnClickListener() {
                    @Override
                    public void onClick() {
                        parent.deleteHeatingIntervalAt(relativeChildPosition);
                        notifyWeekProfileChangedListener();
                    }
                });
            }
        });

        if (child.isTimeFixed()) {
            deleteButton.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    private View addView(final DayProfile<FilledTemperatureInterval, ?, ?> parent, ViewGroup viewGroup) {
        View view = layoutInflater.inflate(R.layout.weekprofile_interval_add, viewGroup, false);

        final FilledTemperatureInterval interval = new FilledTemperatureInterval();

        setTemperatureAndInterval(view, R.id.addInterval, interval, viewGroup, new OnIntervalTemperatureChangedListener() {
            @Override
            public void onIntervalTemperatureChanged(String time, double temperature) {
                interval.setChangedSwitchTime(time);
                interval.setChangedTemperature(temperature);
                interval.setNew(true);

                parent.addHeatingInterval(interval);
                notifyWeekProfileChangedListener();
            }
        });

        return view;
    }

    private void setTemperatureAndInterval(View view, int buttonId, final FilledTemperatureInterval interval,
                                           final ViewGroup viewGroup, final OnIntervalTemperatureChangedListener listener) {

        Button button = (Button) view.findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            @SuppressWarnings("unchecked")
            public void onClick(View view) {

                final View contentView = layoutInflater.inflate(R.layout.weekprofile_temperature_time_selector, viewGroup, false);

                final TimePicker timePicker = (TimePicker) contentView.findViewById(R.id.timePicker);
                timePicker.setIs24HourView(true);
                String time = interval.getChangedSwitchTime();

                String minutePart = time == null ? "0" : time.substring(0, 2);
                String hourPart = time == null ? "0" : time.substring(3, 5);

                int hours = Integer.valueOf(minutePart);
                if (hours == 24) hours = 0;

                final int minutes = Integer.valueOf(hourPart);

                timePicker.setCurrentHour(hours);
                timePicker.setCurrentMinute(minutes);

                if (interval.isTimeFixed()) {
                    timePicker.setEnabled(false);
                }

                LinearLayout layout = (LinearLayout) contentView.findViewById(R.id.tableLayout);

                TableRow updateRow = (TableRow) contentView.findViewById(R.id.updateRow);
                final TemperatureChangeTableRow temperatureChangeTableRow = new
                        TemperatureChangeTableRow(context, interval.getChangedTemperature(),
                                updateRow, 5.5, 30.0, applicationProperties) {
                            @Override
                            protected ApplicationProperties getApplicationProperties() {
                                return applicationProperties;
                            }

                            @Override
                            protected boolean showButton() {
                                return false;
                            }
                        };

                layout.addView(temperatureChangeTableRow.createRow(layoutInflater, null, 8));

                DialogUtil.showContentDialog(context, null, contentView, new DialogUtil.AlertOnClickListener() {
                    @Override
                    public void onClick() {
                        double temperature = temperatureChangeTableRow.getTemperature();

                        Integer currentHour = timePicker.getCurrentHour();
                        Integer currentMinute = timePicker.getCurrentMinute();

                        String time = timeToTimeString(currentHour, currentMinute);

                        if (listener != null) {
                            listener.onIntervalTemperatureChanged(time, temperature);
                        }
                        notifyWeekProfileChangedListener();
                    }
                });
            }
        });
    }

    private interface OnIntervalTemperatureChangedListener {
        void onIntervalTemperatureChanged(String time, double temperature);
    }
}
