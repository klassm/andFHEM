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

import static li.klass.fhem.util.ValueDescriptionUtil.appendKWh;
import static li.klass.fhem.util.ValueDescriptionUtil.appendW;

public class SMLUSBDevice extends FhemDevice<SMLUSBDevice> {

    @ShowField(showInOverview = true, description = ResourceIdMapper.currentUsage)
    private String power;
    @ShowField(showInOverview = true, description = ResourceIdMapper.counterReading)
    private String counterReading;
    private String counterReadingTariff1;

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.USAGE;
    }

    public String getPower() {
        return power;
    }

    @XmllistAttribute("MOMENTANLEISTUNG")
    public void setPower(String power) {
        this.power = appendW(power);
    }

    public String getCounterReading() {
        return counterReading;
    }

    @XmllistAttribute("ZÄHLERSTAND_BEZUG_TOTAL")
    public void setCounterReading(String counterReading) {
        this.counterReading = appendKWh(counterReading);
    }

    public String getCounterReadingTariff1() {
        return counterReadingTariff1;
    }

    @XmllistAttribute("ZÄHLERSTAND_TARIF_1_BEZUG")
    public void setCounterReadingTariff1(String counterReadingTariff1) {
        this.counterReadingTariff1 = appendKWh(counterReadingTariff1);
    }

}
