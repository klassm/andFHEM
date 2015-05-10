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

import android.content.Context;

import com.google.common.base.Joiner;

import java.util.List;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.room.xmllist.DeviceNode;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;

@OverviewViewSettings(showState = true)
@SuppressWarnings("unused")
public class HCSDevice extends FhemDevice<HCSDevice> {

    @ShowField(description = ResourceIdMapper.ecoThresholdOn)
    @XmllistAttribute("thermostatThresholdOn")
    private String thermostatThresholdOn;

    @ShowField(description = ResourceIdMapper.thermostatThresholdOff)
    @XmllistAttribute("thermostatThresholdOff")
    private String thermostatThresholdOff;

    @ShowField(description = ResourceIdMapper.valveThresholdOff)
    @XmllistAttribute("valveThresholdOff")
    private String valveThresholdOff;

    @ShowField(description = ResourceIdMapper.valveThresholdOn)
    @XmllistAttribute("valveThresholdOn")
    private String valveThresholdOn;

    @ShowField(description = ResourceIdMapper.ecoThresholdOff)
    @XmllistAttribute("ecoTemperatureOff")
    private String ecoTemperatureOff;

    @ShowField(description = ResourceIdMapper.ecoThresholdOn)
    @XmllistAttribute("ecoTemperatureOn")
    private String ecoTemperatureOn;

    @ShowField(description = ResourceIdMapper.mode)
    @XmllistAttribute("mode")
    private String mode;

    @ShowField(description = ResourceIdMapper.idleDevices)
    private int numberOfIdleDevices;
    @ShowField(description = ResourceIdMapper.excludedDevices)
    private int numberOfExcludedDevices;
    @ShowField(description = ResourceIdMapper.demandDevices)
    private int numberOfDemandDevices;

    private List<String> demandDevices = newArrayList();

    @Override
    public void onChildItemRead(DeviceNode.DeviceNodeType type, String key, String value, DeviceNode node) {
        super.onChildItemRead(type, key, value, node);

        // We only want STATE nodes whose value is DEMAND, IDLE or EXCLUDED.
        // However, we can also get a STATE node with key "state" and value "demand".
        // This example would result in a demand device number which is one two high.
        if (!node.isStateNode() || "state".equalsIgnoreCase(key)) {
            return;
        }

        if (value.equalsIgnoreCase("DEMAND")) {
            numberOfDemandDevices++;
            demandDevices.add(key);
        } else if (value.equalsIgnoreCase("IDLE")) {
            numberOfIdleDevices++;
        } else if (value.equalsIgnoreCase("EXCLUDED")) {
            numberOfExcludedDevices++;
        }
    }

    @Override
    public void afterDeviceXMLRead(Context context) {
        super.afterDeviceXMLRead(context);
        sort(demandDevices);
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.HEATING;
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

    @ShowField(description = ResourceIdMapper.blank, showAfter = "numberOfDemandDevices")
    public String getCommaSeparatedDemandDevices() {
        return Joiner.on(", ").join(demandDevices);
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
