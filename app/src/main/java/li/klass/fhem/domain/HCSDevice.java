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

import org.w3c.dom.NamedNodeMap;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;

@OverviewViewSettings(showState = true)
@SuppressWarnings("unused")
public class HCSDevice extends Device<HCSDevice> {

    @ShowField(description = ResourceIdMapper.ecoThresholdOn)
    private String thermostatThresholdOn;
    @ShowField(description = ResourceIdMapper.thermostatThresholdOff)
    private String thermostatThresholdOff;
    @ShowField(description = ResourceIdMapper.valveThresholdOff)
    private String valveThresholdOff;
    @ShowField(description = ResourceIdMapper.valveThresholdOn)
    private String valveThresholdOn;
    @ShowField(description = ResourceIdMapper.ecoThresholdOff)
    private String ecoTemperatureOff;
    @ShowField(description = ResourceIdMapper.ecoThresholdOn)
    private String ecoTemperatureOn;
    @ShowField(description = ResourceIdMapper.mode)
    private String mode;
    @ShowField(description = ResourceIdMapper.idleDevices)
    private int numberOfIdleDevices;
    @ShowField(description = ResourceIdMapper.excludedDevices)
    private int numberOfExcludedDevices;
    @ShowField(description = ResourceIdMapper.demandDevices)
    private int numberOfDemandDevices;
    @ShowField(description = ResourceIdMapper.blank, showAfter = "numberOfDemandDevices")
    private String commaSeparatedDemandDevices;


    public void readTHERMOSTATTHRESHOLDOFF(String value) {
        thermostatThresholdOff = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readTHERMOSTATTHRESHOLDON(String value) {
        thermostatThresholdOn = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readVALVETHRESHOLDOFF(String value) {
        valveThresholdOff = ValueDescriptionUtil.appendPercent(value);
    }

    public void readVALVETHRESHOLDON(String value) {
        valveThresholdOn = ValueDescriptionUtil.appendPercent(value);
    }

    public void readECOTEMPERATUREOFF(String value) {
        ecoTemperatureOff = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readECOTEMPERATUREON(String value) {
        ecoTemperatureOn = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readMODE(String value) {
        mode = value;
    }

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        super.onChildItemRead(tagName, key, value, attributes);

        if (!tagName.equalsIgnoreCase("STATE") || key.equalsIgnoreCase("STATE")) {
            return;
        }

        if (value.equalsIgnoreCase("DEMAND")) {
            numberOfDemandDevices++;
            addToDemandDevicesCommaSeparatedList(key);
        } else if (value.equalsIgnoreCase("IDLE")) {
            numberOfIdleDevices++;
        } else if (value.equalsIgnoreCase("EXCLUDED")) {
            numberOfExcludedDevices++;
        }
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.HEATING;
    }

    private void addToDemandDevicesCommaSeparatedList(String key) {
        if (commaSeparatedDemandDevices == null) {
            commaSeparatedDemandDevices = key;
        } else {
            commaSeparatedDemandDevices += ", " + key;
        }
    }

    public String getThermostatThresholdOn() {
        return thermostatThresholdOn;
    }

    public String getThermostatThresholdOff() {
        return thermostatThresholdOff;
    }

    public String getValveThresholdOff() {
        return valveThresholdOff;
    }

    public String getValveThresholdOn() {
        return valveThresholdOn;
    }

    public String getEcoTemperatureOff() {
        return ecoTemperatureOff;
    }

    public String getEcoTemperatureOn() {
        return ecoTemperatureOn;
    }

    public String getMode() {
        return mode;
    }

    public int getNumberOfIdleDevices() {
        return numberOfIdleDevices;
    }

    public int getNumberOfExcludedDevices() {
        return numberOfExcludedDevices;
    }

    public int getNumberOfDemandDevices() {
        return numberOfDemandDevices;
    }

    public String getCommaSeparatedDemandDevices() {
        return commaSeparatedDemandDevices;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }

    @Override
    public long getTimeRequiredForStateError() {
        return OUTDATED_DATA_MS_DEFAULT;
    }
}
