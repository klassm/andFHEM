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
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

@SupportsWidget(TemperatureWidgetView.class)
@SuppressWarnings("unused")
public class PIDDevice extends Device<PIDDevice> implements DesiredTempDevice {

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String temperature;

    @ShowField(description = ResourceIdMapper.desiredTemperature)
    private double desiredTemperature;

    @ShowField(description = ResourceIdMapper.delta, showInOverview = true)
    @WidgetTemperatureAdditionalField(description = ResourceIdMapper.delta)
    private String delta;

    public static final double MINIMUM_TEMPERATURE = 0;
    public static final double MAXIMUM_TEMPERATURE = 40;

    public void readSTATE(String value) {
        String content = value.trim();
        int firstBlank = content.indexOf(" ");
        if (firstBlank != -1 && !content.startsWith("desired")) {
            temperature = ValueDescriptionUtil.appendTemperature(content.substring(0, firstBlank));
        }
    }

    public void readDELTA(String value) {
        delta = value;
    }

    public void readDESIRED(String value) {
        desiredTemperature = ValueExtractUtil.extractLeadingDouble(value);
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
        return ValueDescriptionUtil.desiredTemperatureToString(desiredTemperature, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE);
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

    @Override
    public long getTimeRequiredForStateError() {
        return OUTDATED_DATA_MS_DEFAULT;
    }
}
