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
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.util.ValueExtractUtil;

import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;

public class ThresholdDevice extends Device<ThresholdDevice> implements DesiredTempDevice {
    @ShowField(description = ResourceIdMapper.desiredTemperature)
    private double desiredTemp;

    public static double MAXIMUM_TEMPERATURE = 30.5;
    public static double MINIMUM_TEMPERATURE = 5.5;

    public void readDESIRED(String value) {
        desiredTemp = ValueExtractUtil.extractLeadingDouble(value);
    }

    public void readDESIRED_VALUE(String value) {
        desiredTemp = ValueExtractUtil.extractLeadingDouble(value);
    }

    @Override
    public void setDesiredTemp(double desiredTemp) {
        this.desiredTemp = desiredTemp;
    }

    public double getDesiredTemp() {
        return desiredTemp;
    }

    @Override
    public String getDesiredTempDesc() {
        return appendTemperature(desiredTemp);
    }

    @Override
    public String getDesiredTempCommandFieldName() {
        return "desired";
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.FHEM;
    }
}
