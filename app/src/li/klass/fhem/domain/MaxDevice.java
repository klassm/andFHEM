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
import li.klass.fhem.appwidget.view.widget.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.ToggleWidgetView;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

import java.util.List;

@SuppressWarnings("unused")
@FloorplanViewSettings(showState = true)
@DetailOverviewViewSettings(showState = true)
public class MaxDevice extends ToggleableDevice<MaxDevice> {

    private SubType subType;

    @ShowField(description = ResourceIdMapper.type)
    private String type;

    @ShowField(description = ResourceIdMapper.battery, showInOverview = true)
    private String battery;

    @ShowField(description = ResourceIdMapper.windowOpenTemp)
    private String windowOpenTemperature;

    @ShowField(description = ResourceIdMapper.desiredTemperature, showInOverview = true)
    private String desiredTemperature;

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    private String temperature;

    @ShowField(description = ResourceIdMapper.actuator)
    private String actuator;


    public enum SubType {
        CUBE, SWITCH, WINDOW, TEMPERATURE
    }

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
        this.windowOpenTemperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readDESIREDTEMPERATURE(String value) {
        this.desiredTemperature = ValueDescriptionUtil.appendTemperature(value);
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

    public String getWindowOpenTemperature() {
        return windowOpenTemperature;
    }

    public String getDesiredTemperature() {
        return desiredTemperature;
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
        if (appWidgetClass.equals(TemperatureWidgetView.class) && subType == SubType.TEMPERATURE) {
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
}
