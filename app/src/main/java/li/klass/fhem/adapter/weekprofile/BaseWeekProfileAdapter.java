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
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.heating.schedule.DayProfile;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval;
import li.klass.fhem.widget.NestedListViewAdapter;

import static com.google.common.collect.Lists.newArrayList;

public abstract class BaseWeekProfileAdapter<H extends BaseHeatingInterval>
        extends NestedListViewAdapter<DayProfile<H, ?, ?>, H> {

    protected WeekProfile<H, ?, ? extends Device> weekProfile;


    protected final Resources resources;
    protected final Context context;
    private WeekProfileChangedListener listener;

    public BaseWeekProfileAdapter(Context context) {
        super(context);
        this.context = context;
        resources = context.getResources();
    }

    @Override
    protected int getChildrenCountForParent(DayProfile<H, ?, ?> parent) {
        return parent.getNumberOfHeatingIntervals() + getNumberOfAdditionalChildrenForParent();
    }

    protected abstract int getNumberOfAdditionalChildrenForParent();

    @Override
    protected H getChildForParentAndChildPosition(DayProfile<H, ?, ?> parent, int childPosition) {
        return parent.getHeatingIntervalAt(childPosition);
    }

    @Override
    protected View getParentView(DayProfile<H, ?, ?> parent, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.weekprofile_parent, viewGroup, false);

        TextView parentTextView = (TextView) view.findViewById(R.id.parent);
        parentTextView.setText(resources.getText(parent.getDay().getStringId()));

        return view;
    }

    @Override
    protected List<DayProfile<H, ?, ?>> getParents() {
        List<DayProfile<H, ?, ?>> parents = newArrayList();
        if (weekProfile == null) return parents;

        List<? extends DayProfile> sortedDayProfiles = weekProfile.getSortedDayProfiles();

        for (DayProfile<H, ?, ? extends HeatingConfiguration> sortedDayProfile : sortedDayProfiles) {
            parents.add(sortedDayProfile);
        }
        return parents;
    }

    protected String timeToTimeString(int hourOfDay, int minuteOfDay) {
        int minutes = (minuteOfDay + 9) / 10 * 10;
        if (minutes == 60) minutes = 0;

        if (minutes == 0 && minuteOfDay != 0) hourOfDay += 1;

        return String.format("%02d", hourOfDay) + ":" + String.format("%02d", minutes);
    }

    public void updateData(WeekProfile<H, ?, ? extends Device> weekProfile) {
        if (weekProfile == null) return;
        this.weekProfile = weekProfile;
        super.updateData();
    }

    protected void setDetailTextView(View view, int layoutItemId, String currentText,
                                     String originalText, boolean isNew) {
        TextView layoutItem = (TextView) view.findViewById(layoutItemId);
        layoutItem.setText(weekProfile.formatTime(currentText));

        if (isNew || originalText == null || currentText == null || !originalText.equals(currentText)) {
            layoutItem.setTextColor(Color.BLUE);
        }
    }

    public void registerWeekProfileChangedListener(WeekProfileChangedListener listener) {
        this.listener = listener;
    }

    protected void notifyWeekProfileChangedListener() {
        notifyDataSetChanged();
        if (listener != null) {
            listener.onWeekProfileChanged(weekProfile);
        }
    }

    public interface WeekProfileChangedListener {
        void onWeekProfileChanged(WeekProfile weekProfile);
    }
}
