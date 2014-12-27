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

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;

import static li.klass.fhem.domain.core.DeviceFunctionality.USAGE;
import static li.klass.fhem.util.ValueDescriptionUtil.*;
import static li.klass.fhem.util.ValueDescriptionUtil.append;

public class SolarViewDevice extends Device<SolarViewDevice> {
    @ShowField(description = ResourceIdMapper.currentPower, showInOverview = true)
    private String currentPower;

    @ShowField(description = ResourceIdMapper.current)
    private String gridCurrent;

    @ShowField(description = ResourceIdMapper.voltage)
    private String gridVoltage;

    @ShowField(description = ResourceIdMapper.temperature)
    private String temperature;

    @ShowField(description = ResourceIdMapper.totalEnergy, showInOverview = true)
    private String totalEnergy;

    @ShowField(description = ResourceIdMapper.totalEnergyDay)
    private String totalEnergyDay;

    @ShowField(description = ResourceIdMapper.totalEnergyMonth)
    private String totalEnergyMonth;

    @ShowField(description = ResourceIdMapper.totalEnergyYear)
    private String totalEnergyYear;

    @XmllistAttribute("currentPower")
    public void setCurrentPower(String currentPower) {
        this.currentPower = appendW(currentPower);
    }

    @XmllistAttribute("gridCurrent")
    public void setGridCurrent(String gridCurrent) {
        this.gridCurrent = append(gridCurrent, "A");
    }

    @XmllistAttribute("gridVoltage")
    public void setGridVoltage(String gridVoltage) {
        this.gridVoltage = append(gridVoltage, "V");
    }

    @XmllistAttribute("temperature")
    public void setTemperature(String temperature) {
        this.temperature = appendTemperature(temperature);
    }

    @XmllistAttribute("totalEnergy")
    public void setTotalEnergy(String totalEnergy) {
        this.totalEnergy = appendKWh(totalEnergy);
    }

    @XmllistAttribute("totalEnergyDay")
    public void setTotalEnergyDay(String totalEnergyDay) {
        this.totalEnergyDay = appendKWh(totalEnergyDay);
    }

    @XmllistAttribute("totalEnergyMonth")
    public void setTotalEnergyMonth(String totalEnergyMonth) {
        this.totalEnergyMonth = appendKWh(totalEnergyMonth);
    }

    @XmllistAttribute("totalEnergyYear")
    public void setTotalEnergyYear(String totalEnergyYear) {
        this.totalEnergyYear = appendKWh(totalEnergyYear);
    }

    public String getCurrentPower() {
        return currentPower;
    }

    public String getGridCurrent() {
        return gridCurrent;
    }

    public String getGridVoltage() {
        return gridVoltage;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getTotalEnergy() {
        return totalEnergy;
    }

    public String getTotalEnergyDay() {
        return totalEnergyDay;
    }

    public String getTotalEnergyMonth() {
        return totalEnergyMonth;
    }

    public String getTotalEnergyYear() {
        return totalEnergyYear;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return USAGE;
    }
}
