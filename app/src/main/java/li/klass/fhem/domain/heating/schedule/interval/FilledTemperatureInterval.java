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

public class FilledTemperatureInterval extends BaseHeatingInterval implements Comparable<BaseHeatingInterval> {
    private boolean timeFixed;

    private double temperature = 6;
    private double changedTemperature = 6;

    private String switchTime;
    private String changedSwitchTime;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
        this.changedTemperature = temperature;
    }

    public double getChangedTemperature() {
        return changedTemperature;
    }

    public void setChangedTemperature(double changedTemperature) {
        this.changedTemperature = changedTemperature;
    }

    public String getSwitchTime() {
        return switchTime;
    }

    public void setSwitchTime(String switchTime) {
        this.switchTime = switchTime;
        this.changedSwitchTime = switchTime;
    }

    public String getChangedSwitchTime() {
        return changedSwitchTime;
    }

    public void setChangedSwitchTime(String changedSwitchTime) {
        if (isTimeFixed()) throw new IllegalStateException("time is fixed!");

        this.changedSwitchTime = changedSwitchTime;
    }

    public boolean isTimeFixed() {
        return timeFixed;
    }

    public void setTimeFixed(boolean timeFixed) {
        this.timeFixed = timeFixed;
    }

    @Override
    public boolean isModified() {
        if (super.isModified()) return true;

        double temperatureDiff = Math.abs(temperature - changedTemperature);
        return temperatureDiff > 0.1 || (switchTime != null && !switchTime.equals(changedSwitchTime));
    }

    @Override
    public int compareTo(BaseHeatingInterval interval) {
        if (!(interval instanceof FilledTemperatureInterval)) return 1;

        FilledTemperatureInterval filledTemperatureInterval = (FilledTemperatureInterval) interval;
        return changedSwitchTime.compareTo(filledTemperatureInterval.changedSwitchTime);
    }

    @Override
    public void acceptChanges() {
        super.acceptChanges();
        temperature = changedTemperature;
        switchTime = changedSwitchTime;
    }

    @Override
    public void reset() {
        changedTemperature = temperature;
        changedSwitchTime = switchTime;
    }
}
