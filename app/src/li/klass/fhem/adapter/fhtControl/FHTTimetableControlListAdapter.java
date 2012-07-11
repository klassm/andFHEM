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

package li.klass.fhem.adapter.fhtControl;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.fht.FHTDayControl;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.widget.NestedListViewAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FHTTimetableControlListAdapter extends NestedListViewAdapter<Integer, FHTDayControl> {

    private interface OnTimeChangedListener {
        void onTimeChanged(String newTime);
    }
    
    private Map<Integer, FHTDayControl> controlMap = new HashMap<Integer, FHTDayControl>();
    private Resources resources;
    private Context context;

    public FHTTimetableControlListAdapter(Context context) {
        super(context);
        this.context = context;
        resources = context.getResources();
    }

    @Override
    protected FHTDayControl getChildForParentAndChildPosition(Integer parent, int childPosition) {
        return controlMap.get(parent);
    }

    @Override
    protected int getChildrenCountForParent(Integer parent) {
        if (controlMap.get(parent) != null) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    protected View getParentView(Integer parent, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.control_fht_list_parent, null);

        TextView parentTextView = (TextView) view.findViewById(R.id.parent);
        parentTextView.setText(resources.getText(parent));

        return view;
    }

    @Override
    protected View getChildView(Integer parent, int parentPosition, final FHTDayControl child, View v, ViewGroup viewGroup) {
        final View view = layoutInflater.inflate(R.layout.control_fht_list_item, null);

        setDetailTextView(view, R.id.from1, child.getFrom1Current(), child.getFrom1(), child.getFrom1Changed());
        setDetailTextView(view, R.id.from2, child.getFrom2Current(), child.getFrom2(), child.getFrom2Changed());
        setDetailTextView(view, R.id.to1, child.getTo1Current(), child.getTo1(), child.getTo1Changed());
        setDetailTextView(view, R.id.to2, child.getTo2Current(), child.getTo2(), child.getTo2Changed());

        setChangeTimeButton(view, R.id.from1Set, child.getFrom1Current(), new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(String newTime) {
                child.setFrom1Changed(newTime);
            }
        });

        setChangeTimeButton(view, R.id.from2Set, child.getFrom2Current(), new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(String newTime) {
                child.setFrom2Changed(newTime);
            }
        });

        setChangeTimeButton(view, R.id.to1Set, child.getTo1Current(), new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(String newTime) {
                child.setTo1Changed(newTime);
            }
        });

        setChangeTimeButton(view, R.id.to2Set, child.getTo2Current(), new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(String newTime) {
                child.setTo2Changed(newTime);
            }
        });

        return view;
    }

    private void setDetailTextView(View view, int layoutItemId, String currentText, String originalText, String changedText) {
        TextView layoutItem = (TextView) view.findViewById(layoutItemId);
        layoutItem.setText(FHTDayControl.formatTime(currentText));

        if (! originalText.equals(changedText)) {
            layoutItem.setTextColor(Color.BLUE);
        }
    }

    private void setChangeTimeButton(View view, int buttonId, final String currentTime, final OnTimeChangedListener listener) {

        Button to2Button = (Button) view.findViewById(buttonId);
        to2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                int hours = Integer.valueOf(currentTime.substring(0, 2));
                if (hours == 24) hours = 0;
                int minutes = Integer.valueOf(currentTime.substring(3, 5));

                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfDay) {
                        int minutes = (minuteOfDay + 9) / 10 * 10;
                        if (minutes == 60) minutes = 0;

                        if (minutes == 0 && minuteOfDay != 0) hourOfDay += 1;

                        String newTime = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minutes);
                        listener.onTimeChanged(newTime);
                        context.sendBroadcast(new Intent(Actions.DO_UPDATE));
                    }
                }, hours, minutes, true);

                timePickerDialog.show();
            }
        });
    }

    @Override
    protected List<Integer> getParents() {
        return DayUtil.getSortedDayStringIdList();
    }

    public void updateData(Map<Integer, FHTDayControl> fhtDayControlMap) {
        if (fhtDayControlMap == null) return;
        this.controlMap = fhtDayControlMap;
        super.updateData();
    }
}
