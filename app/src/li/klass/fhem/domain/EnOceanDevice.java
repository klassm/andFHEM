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

package li.klass.fhem.domain;

import android.util.Log;
import li.klass.fhem.appwidget.view.widget.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.ToggleWidgetView;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;

@SuppressWarnings("unused")
@DetailOverviewViewSettings(showState = true)
@FloorplanViewSettings(showState = true)
public class EnOceanDevice extends ToggleableDevice<EnOceanDevice> {

    public enum SubType {
        SWITCH, SENSOR
    }

    private SubType subType;

    private static final String TAG = EnOceanDevice.class.getName();

    public void readSUBTYPE(String value) {
        if (value.equalsIgnoreCase("switch")) {
            subType = SubType.SWITCH;
        } else if (value.equalsIgnoreCase("sensor")) {
            subType = SubType.SENSOR;
        } else {
            Log.e(TAG, "unknown subtype " + value);
            subType = null;
        }
    }

    @Override
    public boolean isOnByState() {
        return getInternalState().equalsIgnoreCase(getOnStateName());
    }

    @Override
    public boolean supportsToggle() {
        return subType == SubType.SWITCH;
    }

    public SubType getSubType() {
        return subType;
    }

    @Override
    public boolean supportsWidget(Class<? extends AppWidgetView> appWidgetClass) {
        if (appWidgetClass.equals(ToggleWidgetView.class) && subType != SubType.SWITCH) {
            return false;
        }

        return super.supportsWidget(appWidgetClass);
    }

    @Override
    public int compareTo(EnOceanDevice other) {
        int result = 0;
        if (subType != null) {
            result = subType.compareTo(other.getSubType());
        }

        if (result != 0) return result;

        return name.compareTo(other.getName());
    }

    @Override
    public String getOffStateName() {
        return "BI";
    }

    @Override
    public String getOnStateName() {
        return "B0";
    }
}
