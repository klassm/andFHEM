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

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.ToggleWidgetView;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.*;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

import java.util.List;

import static li.klass.fhem.util.ValueDescriptionUtil.desiredTemperatureToString;

@SuppressWarnings("unused")
@FloorplanViewSettings(showState = true)
@DetailOverviewViewSettings(showState = true)
@SupportsWidget(TemperatureWidgetView.class)
public class MaxDevice extends ToggleableDevice<MaxDevice> implements DesiredTempDevice, HeatingModeDevice<MaxDevice.HeatingMode>,
        WindowOpenTempDevice, EcoTempDevice, ComfortTempDevice {

    public enum HeatingMode {
        ECO, COMFORT, BOOST, AUTO, MANUAL
    }

    public enum SubType {
        CUBE, SWITCH, WINDOW, TEMPERATURE
    }

    public static double MAXIMUM_TEMPERATURE = 30.5;
    public static double MINIMUM_TEMPERATURE = 4.5;

    private SubType subType;

    @ShowField(description = ResourceIdMapper.type)
    private String type;

    @ShowField(description = ResourceIdMapper.battery, showInOverview = true)
    private String battery;

    @ShowField(description = ResourceIdMapper.windowOpenTemp)
    private double windowOpenTemp;


    private double desiredTemp;

    @ShowField(description = ResourceIdMapper.desiredTemperature, showInOverview = true)
    private String desiredTempDesc;

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String temperature;

    @ShowField(description = ResourceIdMapper.actuator)
    private String actuator;

    @ShowField(description = ResourceIdMapper.ecoTemp)
    private double ecoTemp;

    @ShowField(description = ResourceIdMapper.comfortTemp)
    private double comfortTemp;

    private HeatingMode heatingMode;

    public void readTYPE(String value) {
        if (value.equalsIgnoreCase("ShutterContact")) {
            subType = SubType.WINDOW;
        } else if (value.equalsIgnoreCase("Cube")) {
            subType = SubType.CUBE;
        } else if (value.equalsIgnoreCase("PushButton")) {
            subType = SubType.SWITCH;
        } else if (value.equalsIgnoreCase("HeatingThermostat")) {
            subType = SubType.TEMPERATURE;
        }

        type = value;
    }

    public void readBATTERY(String value) {
        this.battery = value;
    }

    public void readWINDOWOPENTEMPERATURE(String value) {
        this.windowOpenTemp = ValueExtractUtil.extractLeadingDouble(value);
    }

    public void readECOTEMPERATURE(String value) {
        this.ecoTemp = ValueExtractUtil.extractLeadingDouble(value);
    }

    public void readCOMFORTTEMPERATURE(String value) {
        comfortTemp = ValueExtractUtil.extractLeadingDouble(value);
    }

    public void readDESIREDTEMPERATURE(String value) {
        for (HeatingMode heatingMode : HeatingMode.values()) {
            if (value.equalsIgnoreCase(heatingMode.name())) {
                this.heatingMode = heatingMode;
                return;
            }
        }

        setDesiredTemp(ValueExtractUtil.extractLeadingDouble(value));
    }

    public void readTEMPERATURE(String value) {
        this.temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readVALVEPOSITION(String value) {
        this.actuator = ValueDescriptionUtil.appendPercent(value);
    }

    @Override
    public boolean isOnByState() {
        return getInternalState().equalsIgnoreCase(getOnStateName());
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

    @Override
    public void setWindowOpenTemp(double windowOpenTemp) {
        this.windowOpenTemp = windowOpenTemp;
    }

    public Double getWindowOpenTemp() {
        return windowOpenTemp;
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
    public void setEcoTemp(double ecoTemp) {
        this.ecoTemp = ecoTemp;
    }

    @Override
    public Double getEcoTemp() {
        return ecoTemp;
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
    public void setComfortTemp(double comfortTemp) {
        this.comfortTemp = comfortTemp;
    }

    @Override
    public Double getComfortTemp() {
        return comfortTemp;
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
        addDeviceChartIfNotNull(temperature, new DeviceChart(R.string.temperatureGraph, R.string.yAxisTemperature,
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:temperature"),
                ChartSeriesDescription.getDiscreteValuesInstance(R.string.desiredTemperature, "4:desiredTemperature")));
        addDeviceChartIfNotNull(actuator, new DeviceChart(R.string.actuatorGraph, R.string.yAxisActuator,
                new ChartSeriesDescription(R.string.actuator, "4:valveposition")));
    }

    @Override
    public boolean supportsWidget(Class<? extends AppWidgetView> appWidgetClass) {
        if (appWidgetClass.isAssignableFrom(TemperatureWidgetView.class) && subType == SubType.TEMPERATURE) {
            return true;
        } else if (appWidgetClass.isAssignableFrom(ToggleWidgetView.class) && subType == SubType.SWITCH) {
            return true;
        }
        return super.supportsWidget(appWidgetClass);
    }

    @Override
    public boolean isSupported() {
        return subType != null;
    }

    @Override
    public void setDesiredTemp(double desiredTemp) {
        this.desiredTemp = desiredTemp;
        this.desiredTempDesc = desiredTemperatureToString(desiredTemp, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE);
        this.heatingMode = HeatingMode.MANUAL;
    }

    @Override
    public double getDesiredTemp() {
        return desiredTemp;
    }

    @Override
    public String getDesiredTempDesc() {
        return desiredTempDesc;
    }

    @Override
    public void setHeatingMode(HeatingMode heatingMode) {
        this.heatingMode = heatingMode;
        this.desiredTemp = MINIMUM_TEMPERATURE;
    }

    public HeatingMode getHeatingMode() {
        return heatingMode;
    }

    @Override
    public HeatingMode[] getIgnoredHeatingModes() {
        return new HeatingMode[] { HeatingMode.MANUAL };
    }

    @Override
    public String getHeatingModeCommandField() {
        return "desired-temp";
    }

    @Override
    public String getDesiredTempCommandFieldName() {
        return "desired-temp";
    }



}
