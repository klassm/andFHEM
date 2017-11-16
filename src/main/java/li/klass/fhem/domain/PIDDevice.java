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

import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.ui.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.resources.ResourceIdMapper;

import static li.klass.fhem.util.ValueDescriptionUtil.appendPercent;
import static li.klass.fhem.util.ValueDescriptionUtil.appendTemperature;
import static li.klass.fhem.util.ValueDescriptionUtil.desiredTemperatureToString;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;

@SupportsWidget(TemperatureWidgetView.class)
public class PIDDevice extends FhemDevice implements DesiredTempDevice {

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String temperature;

    @ShowField(description = ResourceIdMapper.desiredTemperature, showInOverview = true)
    private double desiredTemperature;

    @ShowField(description = ResourceIdMapper.delta, showInOverview = false)
    @WidgetTemperatureAdditionalField(description = ResourceIdMapper.delta)
    private String delta;

    @ShowField(description = ResourceIdMapper.actuator, showInOverview = true)
    private String actuator;

    public static final double MINIMUM_TEMPERATURE = 0;
    public static final double MAXIMUM_TEMPERATURE = 40;

    @XmllistAttribute("STATE")
    public void readSTATE(String value) {
        String content = value.trim();
        int firstBlank = content.indexOf(" ");
        if (firstBlank != -1 && !content.startsWith("desired")) {
            setTemperature(content.substring(0, firstBlank));
        }
    }

    @XmllistAttribute("DELTA")
    public void readDELTA(String value) {
        delta = value;
    }

    @XmllistAttribute("MEASURED")
    public void setTemperature(String value) {
        temperature = appendTemperature(value);
    }

    @XmllistAttribute("DESIRED")
    public void setDesired(String value) {
        desiredTemperature = extractLeadingDouble(value);
    }

    @XmllistAttribute("ACTUATION")
    public void setActuator(String actuator) {
        this.actuator = appendPercent(actuator);
    }

    public String getActuator() {
        return actuator;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setDesiredTemp(double desiredTemperature) {
        this.desiredTemperature = desiredTemperature;
    }

    public double getDesiredTemp() {
        return desiredTemperature;
    }

    @Override
    public String getDesiredTempDesc() {
        return desiredTemperatureToString(desiredTemperature, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE);
    }

    @Override
    public String getDesiredTempCommandFieldName() {
        return "desired";
    }

    public String getDelta() {
        return delta;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.HEATING;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }
}
