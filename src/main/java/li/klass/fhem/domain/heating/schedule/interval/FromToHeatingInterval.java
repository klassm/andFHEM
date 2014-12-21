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

package li.klass.fhem.domain.heating.schedule.interval;

import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration;

public class FromToHeatingInterval extends BaseHeatingInterval {
    private String fromTime;
    private String toTime;

    private String changedFromTime;
    private String changedToTime;

    public FromToHeatingInterval(HeatingConfiguration<?, ?, ?> configuration) {
        fromTime = configuration.offTime;
        toTime = configuration.offTime;

        changedFromTime = configuration.offTime;
        changedToTime = configuration.offTime;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.changedFromTime = fromTime;
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.changedToTime = toTime;
        this.toTime = toTime;
    }

    public String getChangedFromTime() {
        return changedFromTime;
    }

    public void setChangedFromTime(String changedFromTime) {
        this.changedFromTime = changedFromTime;
    }

    public String getChangedToTime() {
        return changedToTime;
    }

    public void setChangedToTime(String changedToTime) {
        this.changedToTime = changedToTime;
    }

    @Override
    public boolean isModified() {
        return super.isModified() || !fromTime.equals(changedFromTime) || !toTime.equals(changedToTime);
    }

    @Override
    public void acceptChanges() {
        super.acceptChanges();
        fromTime = changedFromTime;
        toTime = changedToTime;
    }

    @Override
    public void reset() {
        changedFromTime = fromTime;
        changedToTime = toTime;
    }

    @Override
    public int compareTo(BaseHeatingInterval interval) {
        if (!(interval instanceof FromToHeatingInterval)) return 1;

        FromToHeatingInterval filledTemperatureInterval = (FromToHeatingInterval) interval;
        int compare = changedFromTime.compareTo(filledTemperatureInterval.changedFromTime);
        if (compare != 0) return compare;

        return changedToTime.compareTo(filledTemperatureInterval.changedToTime);
    }
}
