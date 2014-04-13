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
import li.klass.fhem.domain.genericview.ShowField;

import static li.klass.fhem.util.ValueDescriptionUtil.append;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;

@SuppressWarnings("unused")
public class CM160Device extends Device<CM160Device> {

    @ShowField(description = ResourceIdMapper.energyCurrent, showInOverview = true)
    private String current;
    @ShowField(description = ResourceIdMapper.energyPower, showInOverview = true)
    private String power;
    @ShowField(description = ResourceIdMapper.cost, showInOverview = true)
    private String cost;
    @ShowField(description = ResourceIdMapper.co2, showInOverview = true)
    private String co2;
    @ShowField(description = ResourceIdMapper.dayUsage)
    private String cumDay;
    @ShowField(description = ResourceIdMapper.hourUsage)
    private String cumHour;
    @ShowField(description = ResourceIdMapper.monthUsage)
    private String cumMonth;
    @ShowField(description = ResourceIdMapper.yearUsage)
    private String cumYear;

    public void readA(String value) {
        this.current = append(extractLeadingDouble(value), "A");
    }

    public void readW(String value) {
        this.power = append(extractLeadingDouble(value), "W");
    }

    public void readC(String value) {
        this.cost = append(extractLeadingDouble(value), "€/h");
    }

    public void readCO2(String value) {
        this.co2 = append(extractLeadingDouble(value), "kg/h");
    }

    public void readCUMHOUR(String value) {
        this.cumHour = value;
    }

    public void readCUMDAY(String value) {
        this.cumDay = value;
    }

    public void readCUMMONTH(String value) {
        this.cumMonth = value;
    }

    public void readCUMYEAR(String value) {
        this.cumYear = value;
    }

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
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        super.onChildItemRead(tagName, key, value, attributes);

        if ("STATE".equalsIgnoreCase(tagName) && "STATE".equalsIgnoreCase(key)) {
            setMeasured(attributes.getNamedItem("measured").getNodeValue());
        }
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.USAGE;
    }
}
