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

import android.util.Log;

import org.w3c.dom.NamedNodeMap;

import java.util.List;
import java.util.Locale;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.ToggleWidgetView;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.ComfortTempDevice;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.domain.heating.EcoTempDevice;
import li.klass.fhem.domain.heating.HeatingDevice;
import li.klass.fhem.domain.heating.WindowOpenTempDevice;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.configuration.MAXConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

import static li.klass.fhem.service.graph.description.SeriesType.ACTUATOR;
import static li.klass.fhem.service.graph.description.SeriesType.DESIRED_TEMPERATURE;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static li.klass.fhem.util.ValueDescriptionUtil.desiredTemperatureToString;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true)
@SupportsWidget(TemperatureWidgetView.class)
public class MaxDevice extends ToggleableDevice<MaxDevice> implements DesiredTempDevice,
        HeatingDevice<MaxDevice.HeatingMode, MAXConfiguration, FilledTemperatureInterval, MaxDevice>,
        WindowOpenTempDevice, EcoTempDevice, ComfortTempDevice {

    public static final MAXConfiguration heatingConfiguration = new MAXConfiguration();
    public static double MAXIMUM_TEMPERATURE = 30.5;
    public static double MINIMUM_TEMPERATURE = 4.5;

    private SubType subType;
    private WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice> weekProfile = new WeekProfile<>(heatingConfiguration);

    @ShowField(description = ResourceIdMapper.type)
    private String type;

    @ShowField(description = ResourceIdMapper.battery, showInOverview = true)
    private String battery;

    @ShowField(description = ResourceIdMapper.windowOpenTemp)
    private double windowOpenTemp;

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String temperature;

    @ShowField(description = ResourceIdMapper.actuator)
    private String actuator;

    @ShowField(description = ResourceIdMapper.ecoTemp)
    private double ecoTemp;

    @ShowField(description = ResourceIdMapper.comfortTemp)
    private double comfortTemp;

    private double desiredTemp;

    @ShowField(description = ResourceIdMapper.desiredTemperature, showInOverview = true, showAfter = "temperature")
    private String desiredTempDesc;

    private HeatingMode heatingMode;

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        super.onChildItemRead(tagName, key, value, attributes);
        weekProfile.readNode(key, value);
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();
        weekProfile.afterXMLRead();
    }

    public void readTYPE(String value) {
        if (value.equalsIgnoreCase("ShutterContact")) {
            subType = SubType.WINDOW;
        } else if (value.equalsIgnoreCase("Cube")) {
            subType = SubType.CUBE;
        } else if (value.equalsIgnoreCase("PushButton")) {
            subType = SubType.SWITCH;
        } else if (value.equalsIgnoreCase("HeatingThermostat")
                || value.equalsIgnoreCase("HeatingThermostatPlus")
                || value.equalsIgnoreCase("WallMountedThermostat")) {
            subType = SubType.TEMPERATURE;
        }

        type = value;
    }

    public void readBATTERY(String value) {
        this.battery = value;
    }

    public void readWINDOWOPENTEMPERATURE(String value) {
        this.windowOpenTemp = parseTemperature(value);
    }

    public void readECOTEMPERATURE(String value) {
        this.ecoTemp = parseTemperature(value);
    }

    public void readCOMFORTTEMPERATURE(String value) {
        comfortTemp = parseTemperature(value);
    }

    public void readDESIREDTEMPERATURE(String value) {
        for (HeatingMode heatingMode : HeatingMode.values()) {
            if (value.equalsIgnoreCase(heatingMode.name())) {
                this.heatingMode = heatingMode;
                return;
            }
        }

        double temperature = parseTemperature(value);
        setDesiredTemp(temperature);
    }

    public void readTEMPERATURE(String value) {
        this.temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readVALVEPOSITION(String value) {
        this.actuator = ValueDescriptionUtil.appendPercent(value);
    }

    public void readMODE(String value) {
        try {
            heatingMode = HeatingMode.valueOf(value.toUpperCase(Locale.getDefault()));
        } catch (Exception e) {
            Log.e(MaxDevice.class.getName(), "cannot set heating mode from value " + value, e);
        }
    }

    private double parseTemperature(String temperature) {
        if (temperature.equalsIgnoreCase("on")) {
            return MAXIMUM_TEMPERATURE;
        } else if (temperature.equals("off")) {
            return MINIMUM_TEMPERATURE;
        }
        return ValueExtractUtil.extractLeadingDouble(temperature);
    }

    public SubType getSubType() {
        return subType;
    }

    public String getType() {
        return type;
    }

    public String getBattery() {
        return battery;
    }

    public Double getWindowOpenTemp() {
        return windowOpenTemp;
    }

    @Override
    public void setWindowOpenTemp(double windowOpenTemp) {
        this.windowOpenTemp = windowOpenTemp;
    }

    @Override
    public String getWindowOpenTempDesc() {
        return ValueDescriptionUtil.appendTemperature(windowOpenTemp);
    }

    @Override
    public String getWindowOpenTempCommandFieldName() {
        return "windowOpenTemperature";
    }

    @Override
    public Double getEcoTemp() {
        return ecoTemp;
    }

    @Override
    public void setEcoTemp(double ecoTemp) {
        this.ecoTemp = ecoTemp;
    }

    @Override
    public String getEcoTempDesc() {
        return ValueDescriptionUtil.appendTemperature(ecoTemp);
    }

    @Override
    public String getEcoTempCommandFieldName() {
        return "ecoTemperature";
    }

    @Override
    public Double getComfortTemp() {
        return comfortTemp;
    }

    @Override
    public void setComfortTemp(double comfortTemp) {
        this.comfortTemp = comfortTemp;
    }

    @Override
    public String getComfortTempDesc() {
        return ValueDescriptionUtil.appendTemperature(comfortTemp);
    }

    @Override
    public String getComfortTempCommandFieldName() {
        return "comfortTemperature";
    }

    public String getTemperature() {
        return temperature;
    }

    public String getActuator() {
        return actuator;
    }

    @Override
    public boolean supportsToggle() {
        return subType == SubType.SWITCH;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        if (actuator != null) {
            addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureActuatorGraph,
                    new ChartSeriesDescription.Builder()
                            .withColumnName(R.string.temperature)
                            .withFileLogSpec("4:temperature")
                            .withDbLogSpec("temperature::int1")
                            .withSeriesType(TEMPERATURE)
                            .withShowRegression(true)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                            .build(),
                    new ChartSeriesDescription.Builder().withColumnName(R.string.desiredTemperature)
                            .withFileLogSpec("4:desiredTemperature")
                            .withDbLogSpec("desiredTemperature::int")
                            .withSeriesType(DESIRED_TEMPERATURE)
                            .withShowDiscreteValues(true)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("desiredTemperature", 0, 30))
                            .build(),
                    new ChartSeriesDescription.Builder()
                            .withColumnName(R.string.actuator).withFileLogSpec("4:valveposition")
                            .withDbLogSpec("valveposition::int")
                            .withSeriesType(ACTUATOR)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("valveposition", 0, 100))
                            .build()
            ), temperature, actuator);
        } else {
            addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                    new ChartSeriesDescription.Builder()
                            .withColumnName(R.string.temperature)
                            .withFileLogSpec("4:temperature")
                            .withDbLogSpec("temperature::int1")
                            .withSeriesType(TEMPERATURE)
                            .withShowRegression(true)
                            .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 30))
                            .build()
            ), temperature);
        }
    }

    @Override
    public boolean supportsWidget(Class<? extends DeviceAppWidgetView> appWidgetClass) {
        if (appWidgetClass.isAssignableFrom(TemperatureWidgetView.class) && subType == SubType.TEMPERATURE) {
            return true;
        } else if (appWidgetClass.isAssignableFrom(ToggleWidgetView.class) && subType == SubType.SWITCH) {
            return true;
        }
        return super.supportsWidget(appWidgetClass);
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.HEATING;
    }

    @Override
    public boolean isSupported() {
        return super.isSupported() && subType != null;
    }

    @Override
    public double getDesiredTemp() {
        return desiredTemp;
    }

    @Override
    public void setDesiredTemp(double desiredTemp) {
        this.desiredTemp = desiredTemp;
        this.desiredTempDesc = desiredTemperatureToString(desiredTemp, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE);
    }

    @Override
    public String getDesiredTempDesc() {
        return desiredTempDesc;
    }

    public HeatingMode getHeatingMode() {
        return heatingMode;
    }

    @Override
    public void setHeatingMode(HeatingMode heatingMode) {
        this.heatingMode = heatingMode;
        this.desiredTemp = MINIMUM_TEMPERATURE;
    }

    @Override
    public HeatingMode[] getIgnoredHeatingModes() {
        return new HeatingMode[]{HeatingMode.MANUAL};
    }

    @Override
    public HeatingMode[] getHeatingModes() {
        return HeatingMode.values();
    }

    @Override
    public String getHeatingModeCommandField() {
        return "desiredTemperature";
    }

    @Override
    public WeekProfile<FilledTemperatureInterval, MAXConfiguration, MaxDevice> getWeekProfile() {
        return weekProfile;
    }

    @Override
    public String getDesiredTempCommandFieldName() {
        return "desiredTemperature";
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }

    @Override
    public long getTimeRequiredForStateError() {
        return OUTDATED_DATA_MS_DEFAULT;
    }

    public enum HeatingMode {
        ECO, COMFORT, BOOST, AUTO, MANUAL
    }

    public enum SubType {
        CUBE, SWITCH, WINDOW, TEMPERATURE
    }
}
