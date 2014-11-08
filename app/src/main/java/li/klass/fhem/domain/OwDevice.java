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

import java.util.Map;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;

import static li.klass.fhem.domain.OwDevice.SubType.SWITCH;
import static li.klass.fhem.domain.OwDevice.SubType.TEMPERATURE;
import static li.klass.fhem.domain.OwDevice.SubType.UNKNOWN;
import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;

public class OwDevice extends ToggleableDevice<OwDevice> {

    enum SubType {
        TEMPERATURE(DeviceFunctionality.TEMPERATURE),
        SWITCH(DeviceFunctionality.SWITCH),
        UNKNOWN(DeviceFunctionality.UNKNOWN);
        private final DeviceFunctionality functionality;

        SubType(DeviceFunctionality functionality) {
            this.functionality = functionality;
        }
    }

    private SubType subType = UNKNOWN;

    @ShowField(description = ResourceIdMapper.inputA, showInOverview = true)
    @XmllistAttribute({"PIO_A", "PIO", "PIO_0"})
    private String inputA;

    @ShowField(description = ResourceIdMapper.inputB, showInOverview = true)
    @XmllistAttribute({"PIO_B", "PIO_1"})
    private String inputB;

    @ShowField(description = ResourceIdMapper.inputC, showInOverview = true)
    @XmllistAttribute({"PIO_C", "PIO_2"})
    private String inputC;


    @ShowField(description = ResourceIdMapper.inputD, showInOverview = true)
    @XmllistAttribute({"PIO_D", "PIO_3"})
    private String inputD;

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();

        String internalState = getInternalState();
        Map<String, String> eventMap = getEventMap();
        if (internalState.contains("°C")) {
            setState(appendTemperature(extractLeadingDouble(internalState)));
            subType = TEMPERATURE;
        } else if (internalState.contains("temperature")) {
            subType = TEMPERATURE;
        } else if (eventMap.containsKey(getOnStateName()) && eventMap.containsKey(getOffStateName())) {
            subType = SWITCH;
        }
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

    public String getInputA() {
        return inputA;
    }

    public String getInputB() {
        return inputB;
    }

    public String getInputC() {
        return inputC;
    }

    public String getInputD() {
        return inputD;
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
        return !supportsToggle();
    }

    @Override
    public long getTimeRequiredForStateError() {
        return OUTDATED_DATA_MS_DEFAULT;
    }
}
