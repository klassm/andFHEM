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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Locale;

import li.klass.fhem.R;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.configuration.HeatingIntervalConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval;
import li.klass.fhem.widget.NestedListViewAdapter;

import static com.google.common.collect.Lists.newArrayList;

public abstract class BaseWeekProfileAdapter<INTERVAL extends BaseHeatingInterval<INTERVAL>>
        extends NestedListViewAdapter<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>, INTERVAL> {

    protected WeekProfile<INTERVAL, ?> weekProfile;


    protected final Resources resources;
    protected final Context context;
    private WeekProfileChangedListener listener;

    BaseWeekProfileAdapter(Context context) {
        super(context);
        this.context = context;
        resources = context.getResources();
    }

    @Override
    protected int getChildrenCountForParent(DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> parent) {
        return parent.getNumberOfHeatingIntervals() + getNumberOfAdditionalChildrenForParent();
    }

    protected abstract int getNumberOfAdditionalChildrenForParent();

    @Override
    protected INTERVAL getChildForParentAndChildPosition(DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> parent, int childPosition) {
        return parent.getHeatingIntervalAt(childPosition);
    }

    @Override
    protected View getParentView(final DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> parent, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.weekprofile_parent, viewGroup, false);

        TextView parentTextView = (TextView) view.findViewById(R.id.parent);
        parentTextView.setText(getParentTextFor(parent));

        Button button = (Button) view.findViewById(R.id.copy);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCopyContextMenuFor(parent);
            }
        });

        return view;
    }

    private String getParentTextFor(DayProfile<INTERVAL, ?> profile) {
        return resources.getText(profile.getDay().getStringId()).toString();
    }

    private void showCopyContextMenuFor(final DayProfile<INTERVAL, ?> target) {
        AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
        contextMenu.setTitle(context.getResources().getString(R.string.switchDevice));
        final List<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>> parents = getParents();
        ImmutableList<String> selectOptions = FluentIterable.from(parents)
                .transform(new Function<DayProfile<INTERVAL, ?>, String>() {
                    @Override
                    public String apply(DayProfile<INTERVAL, ?> input) {
                        return getParentTextFor(input);
                    }
                }).toList();

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int position) {
                final DayProfile<INTERVAL, ?> source = parents.get(position);
                if (source.getDay() == target.getDay()) {
                    return;
                }

                List<INTERVAL> heatingIntervals = source.getHeatingIntervals();
                if (heatingIntervals.isEmpty()) {
                    return;
                }
                target.replaceHeatingIntervalsWith(heatingIntervals);
                dialog.dismiss();
                notifyWeekProfileChangedListener();
            }
        };
        contextMenu.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, selectOptions), clickListener);
        contextMenu.show();
    }

    @Override
    protected List<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>> getParents() {
        List<DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>>> parents = newArrayList();
        if (weekProfile == null) return parents;

        List<? extends DayProfile> sortedDayProfiles = weekProfile.getSortedDayProfiles();

        for (DayProfile<INTERVAL, HeatingIntervalConfiguration<INTERVAL>> sortedDayProfile : sortedDayProfiles) {
            parents.add(sortedDayProfile);
        }
        return parents;
    }

    String timeToTimeString(int hourOfDay, int minuteOfDay) {
        int intervalMinutesMustBeDivisibleBy = weekProfile.getConfiguration().getIntervalMinutesMustBeDivisibleBy();
        int minutes = (minuteOfDay + intervalMinutesMustBeDivisibleBy - 1) / intervalMinutesMustBeDivisibleBy * intervalMinutesMustBeDivisibleBy;
        if (minutes == 60) minutes = 0;

        if (minutes == 0 && minuteOfDay != 0) hourOfDay += 1;

        return String.format(Locale.getDefault(), "%02d", hourOfDay) + ":" + String.format(Locale.getDefault(), "%02d", minutes);
    }

    public void updateData(WeekProfile<INTERVAL, ?> weekProfile) {
        if (weekProfile == null) return;
        this.weekProfile = weekProfile;
        super.updateData();
    }

    void setDetailTextView(View view, int layoutItemId, String currentText,
                           String originalText, boolean isNew) {
        TextView layoutItem = (TextView) view.findViewById(layoutItemId);
        layoutItem.setText(weekProfile.formatTimeForDisplay(currentText));

        if (isNew || originalText == null || currentText == null || !originalText.equals(currentText)) {
            layoutItem.setTextColor(Color.BLUE);
        }
    }

    public void registerWeekProfileChangedListener(WeekProfileChangedListener listener) {
        this.listener = listener;
    }

    void notifyWeekProfileChangedListener() {
        notifyDataSetChanged();
        if (listener != null) {
            listener.onWeekProfileChanged(weekProfile);
        }
    }

    interface WeekProfileChangedListener {
        void onWeekProfileChanged(WeekProfile weekProfile);
    }
}
