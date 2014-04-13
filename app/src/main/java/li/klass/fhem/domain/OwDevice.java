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
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

@SuppressWarnings("unused")
public class OwDevice extends ToggleableDevice<OwDevice> {

    enum SubType {
        TEMPERATURE(DeviceFunctionality.TEMPERATURE),
        SWITCH(DeviceFunctionality.SWITCH),
        RELAIS(DeviceFunctionality.UNKNOWN);
        private final DeviceFunctionality functionality;

        SubType(DeviceFunctionality functionality) {
            this.functionality = functionality;
        }
    }

    private SubType subType = null;

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    private String temperature;

    @ShowField(description = ResourceIdMapper.counterA, showInOverview = true)
    private String counterA;

    @ShowField(description = ResourceIdMapper.counterB, showInOverview = true)
    private String counterB;

    public void readMODEL(String value) {
        if (value.equalsIgnoreCase("DS18S20") || value.equalsIgnoreCase("DS18B20")) {
            subType = SubType.TEMPERATURE;
        } else if (value.equalsIgnoreCase("DS2413")) {
            subType = SubType.RELAIS;
        } else if (value.equalsIgnoreCase("DS2405")) {
            subType = SubType.SWITCH;
        }
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();
        if (subType != SubType.TEMPERATURE) return;

        if (temperature == null && getInternalState().matches("[0-9]+\\.[0-9]+.*")) {
            readTEMPERATURE(getInternalState());
        }
    }

    public void readTEMPERATURE(String value) {
        double leading = ValueExtractUtil.extractLeadingDouble(value);
        this.temperature = ValueDescriptionUtil.appendTemperature(leading);
    }

    public void readPIO_A(String value) {
        this.counterA = value;
    }

    public void readPIO_B(String value) {
        this.counterB = value;
    }

    @Override
    public boolean isSupported() {
        return super.isSupported() && subType != null;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        if (subType == null) return null;
        return subType.functionality;
    }

    @Override
    public boolean supportsToggle() {
        return subType == SubType.SWITCH;
    }

    @Override
    public String getOffStateName() {
        return "PIO 0";
    }

    @Override
    public String getOnStateName() {
        return "PIO 1";
    }

    public SubType getSubType() {
        return subType;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getCounterA() {
        return counterA;
    }

    public String getCounterB() {
        return counterB;
    }

    @Override
    protected void putEventToEventMap(String key, String value) {
        if (key.equals("1")) key = "PIO 1";
        if (key.equals("0")) key = "PIO 0";
        if (key.equals("PIO")) return;

        super.putEventToEventMap(key, value);
    }

    @Override
    public boolean isSensorDevice() {
        return ! supportsToggle();
    }

    @Override
    public long getTimeRequiredForStateError() {
        return OUTDATED_DATA_MS_DEFAULT;
    }
}
