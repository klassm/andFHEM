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

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.room.xmllist.DeviceNode;

import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType.STATE;

public class CM160Device extends FhemDevice<CM160Device> {

    @ShowField(description = ResourceIdMapper.energy_current, showInOverview = true)
    @XmllistAttribute("A")
    private String current;

    @ShowField(description = ResourceIdMapper.energy_power, showInOverview = true)
    @XmllistAttribute("W")
    private String power;

    @ShowField(description = ResourceIdMapper.cost, showInOverview = true)
    @XmllistAttribute("C")
    private String cost;

    @ShowField(description = ResourceIdMapper.co2, showInOverview = true)
    @XmllistAttribute("CO2")
    private String co2;

    @ShowField(description = ResourceIdMapper.dayUsage)
    @XmllistAttribute("cumDay")
    private String cumDay;

    @ShowField(description = ResourceIdMapper.hourUsage)
    @XmllistAttribute("cumHour")
    private String cumHour;

    @ShowField(description = ResourceIdMapper.monthUsage)
    @XmllistAttribute("cumMonth")
    private String cumMonth;

    @ShowField(description = ResourceIdMapper.yearUsage)
    @XmllistAttribute("cumYear")
    private String cumYear;

    public String getCurrent() {
        return current;
    }

    public String getPower() {
        return power;
    }

    public String getCost() {
        return cost;
    }

    public String getCo2() {
        return co2;
    }

    public String getCumDay() {
        return cumDay;
    }

    public String getCumHour() {
        return cumHour;
    }

    public String getCumMonth() {
        return cumMonth;
    }

    public String getCumYear() {
        return cumYear;
    }

    @Override
    public void onChildItemRead(DeviceNode.DeviceNodeType type, String key, String value, DeviceNode node) {
        super.onChildItemRead(type, key, value, node);

        if (node.getType() == STATE && "STATE".equalsIgnoreCase(node.getKey())) {
            setMeasured(node.getMeasured());
        }
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.USAGE;
    }
}
