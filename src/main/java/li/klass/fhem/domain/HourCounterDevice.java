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

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;

import static li.klass.fhem.util.ValueDescriptionUtil.appendEuro;
import static li.klass.fhem.util.ValueDescriptionUtil.appendKWh;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;

public class HourCounterDevice extends FhemDevice<HourCounterDevice> {
    @ShowField(description = ResourceIdMapper.cumulativeUsage, showInOverview = true)
    private String cumulativeUsage;

    @ShowField(description = ResourceIdMapper.dayUsage, showInOverview = true)
    private String dayKwh;

    @ShowField(description = ResourceIdMapper.price)
    private String price;

    @ShowField(description = ResourceIdMapper.pricePerDay)
    private String pricePerDay;

    @XmllistAttribute("Zaehlerstand")
    public void setCumulativeUsage(String cumulativeUsage, NamedNodeMap namedNodeMap) {
        String[] parts = cumulativeUsage.split(" ");
        this.cumulativeUsage = parts[0] + " (" + parts[1] + ")";
        if (namedNodeMap != null) {
            setMeasured(namedNodeMap.getNamedItem("measured").getNodeValue());
        }
    }

    @XmllistAttribute("verbrauchTagkWh")
    public void setDayKwh(String dayKwh) {
        this.dayKwh = appendKWh(extractLeadingDouble(dayKwh, 2));
    }

    @XmllistAttribute("verbrauchGesamtEuro")
    public void setPrice(String price) {
        this.price = appendEuro(extractLeadingDouble(price, 2));
    }

    @XmllistAttribute("verbrauchTagEuro")
    public void setPricePerDay(String pricePerDay) {
        this.pricePerDay = appendEuro(pricePerDay);
    }

    public String getCumulativeUsage() {
        return cumulativeUsage;
    }

    public String getDayKwh() {
        return dayKwh;
    }

    public String getPrice() {
        return price;
    }

    public String getPricePerDay() {
        return pricePerDay;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.USAGE;
    }
}
