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

import java.util.Locale;

import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine3;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.domain.heating.HeatingDevice;
import li.klass.fhem.domain.heating.TemperatureDevice;
import li.klass.fhem.domain.heating.WindowOpenTempDevice;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.configuration.FHTConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.FromToHeatingInterval;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.util.ValueDescriptionUtil;

import static li.klass.fhem.util.ValueDescriptionUtil.appendPercent;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;

@SupportsWidget({TemperatureWidgetView.class, MediumInformationWidgetView.class})
public class FHTDevice extends FhemDevice<FHTDevice> implements DesiredTempDevice,
        WindowOpenTempDevice, HeatingDevice<FHTMode, FHTConfiguration, FromToHeatingInterval, FHTDevice>, TemperatureDevice {

    private static final FHTConfiguration heatingConfiguration = new FHTConfiguration();
    public static double MAXIMUM_TEMPERATURE = 30.5;
    public static double MINIMUM_TEMPERATURE = 5.5;

    private WeekProfile<FromToHeatingInterval, FHTConfiguration, FHTDevice> weekProfile = new WeekProfile<>(heatingConfiguration);
    private FHTMode heatingMode;

    @ShowField(description = ResourceIdMapper.desiredTemperature, showAfter = "temperature")
    @WidgetMediumLine2(description = ResourceIdMapper.desiredTemperature)
    private double desiredTemp = MINIMUM_TEMPERATURE;

    @ShowField(description = ResourceIdMapper.dayTemperature, showAfter = "desiredTemp")
    @XmllistAttribute("day_temp")
    private double dayTemperature = MINIMUM_TEMPERATURE;

    @ShowField(description = ResourceIdMapper.nightTemperature, showAfter = "dayTemperature")
    @XmllistAttribute("night_temp")
    private double nightTemperature = MINIMUM_TEMPERATURE;

    @ShowField(description = ResourceIdMapper.windowOpenTemp, showAfter = "nightTemperature")
    @XmllistAttribute("windowopen_temp")
    private double windowOpenTemp = MINIMUM_TEMPERATURE;

    @ShowField(description = ResourceIdMapper.actuator, showInOverview = true)
    @WidgetTemperatureAdditionalField(description = ResourceIdMapper.actuator)
    @WidgetMediumLine3(description = ResourceIdMapper.actuator)
    private String actuator;

    @ShowField(description = ResourceIdMapper.warnings)
    @XmllistAttribute("warnings")
    private String warnings;

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    @WidgetMediumLine1
    @XmllistAttribute("measured_temp")
    private String temperature;

    @ShowField(description = ResourceIdMapper.battery)
    @XmllistAttribute("battery")
    private String battery;

    @Override
    public void onChildItemRead(DeviceNode.DeviceNodeType type, String key, String value, DeviceNode node) {
        super.onChildItemRead(type, key, value, node);

        weekProfile.readNode(key, value);

        if (key.startsWith("actuator") && value != null && value.matches("[0-9]*[%]?")) {
            actuator = appendPercent(extractLeadingDouble(value));
        }
    }

    @Override
    public void afterDeviceXMLRead(Context context) {
        super.afterDeviceXMLRead(context);
        weekProfile.afterXMLRead();
    }

    @XmllistAttribute("mode")
    public void setMode(String value) {
        try {
            this.heatingMode = FHTMode.valueOf(value.toUpperCase(Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            this.heatingMode = FHTMode.UNKNOWN;
        }
    }

    @XmllistAttribute("desired_temp")
    public void setDesiredTemp(String value) {
        if (value.equalsIgnoreCase("off")) value = "5.5";
        if (value.equalsIgnoreCase("on")) value = "30.5";

        desiredTemp = extractLeadingDouble(value);
    }

    public String getActuator() {
        return actuator;
    }

    @Override
    public String toString() {
        return "FHTDevice{" +
                "actuator='" + actuator + '\'' +
                "} " + super.toString();
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.HEATING;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getDesiredTempDesc() {
        return ValueDescriptionUtil.desiredTemperatureToString(desiredTemp, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE);
    }

    @Override
    public String getDesiredTempCommandFieldName() {
        return "desired-temp";
    }

    public double getDesiredTemp() {
        return desiredTemp;
    }

    public void setDesiredTemp(double desiredTemp) {
        this.desiredTemp = desiredTemp;
    }

    public String getDayTemperatureDesc() {
        return ValueDescriptionUtil.desiredTemperatureToString(dayTemperature, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE);
    }

    public double getDayTemperature() {
        return dayTemperature;
    }

    public void setDayTemperature(double dayTemperature) {
        this.dayTemperature = dayTemperature;
    }

    public String getNightTemperatureDesc() {
        return ValueDescriptionUtil.desiredTemperatureToString(nightTemperature, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE);
    }

    public double getNightTemperature() {
        return nightTemperature;
    }

    public void setNightTemperature(double nightTemperature) {
        this.nightTemperature = nightTemperature;
    }

    public String getWindowOpenTempDesc() {
        return ValueDescriptionUtil.desiredTemperatureToString(windowOpenTemp, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE);
    }

    @Override
    public String getWindowOpenTempCommandFieldName() {
        return "windowopen-temp";
    }

    public Double getWindowOpenTemp() {
        return windowOpenTemp;
    }

    public void setWindowOpenTemp(double windowOpenTemp) {
        this.windowOpenTemp = windowOpenTemp;
    }

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    public FHTMode getHeatingMode() {
        if (heatingMode == null) return FHTMode.UNKNOWN;
        return heatingMode;
    }

    public void setHeatingMode(FHTMode heatingMode) {
        this.heatingMode = heatingMode;
    }

    @Override
    public FHTMode[] getIgnoredHeatingModes() {
        return new FHTMode[0];
    }

    @Override
    public FHTMode[] getHeatingModes() {
        return FHTMode.values();
    }

    @Override
    public String getHeatingModeCommandField() {
        return "mode";
    }

    @Override
    public WeekProfile<FromToHeatingInterval, FHTConfiguration, FHTDevice> getWeekProfile() {
        return weekProfile;
    }

    public String getBattery() {
        return battery;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }
}