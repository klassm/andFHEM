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
import li.klass.fhem.appwidget.annotation.*;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.fht.FHTDayControl;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.domain.heating.WindowOpenTempDevice;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;
import li.klass.fhem.util.ValueUtil;
import org.w3c.dom.NamedNodeMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FloorplanViewSettings
@SupportsWidget({TemperatureWidgetView.class, MediumInformationWidgetView.class})
@SuppressWarnings("unused")
public class FHTDevice extends Device<FHTDevice> implements DesiredTempDevice,
        WindowOpenTempDevice {
    public static double MAXIMUM_TEMPERATURE = 30.5;
    public static double MINIMUM_TEMPERATURE = 5.5;

    @ShowField(description = ResourceIdMapper.actuator, showInOverview = true)
    @WidgetTemperatureAdditionalField(description = ResourceIdMapper.actuator)
    @WidgetMediumLine3(description = ResourceIdMapper.actuator)
    private String actuator;
    private FHTMode mode;
    @ShowField(description = ResourceIdMapper.desiredTemperature)
    @WidgetMediumLine2(description = ResourceIdMapper.desiredTemperature)
    private double desiredTemp = MINIMUM_TEMPERATURE;
    @ShowField(description = ResourceIdMapper.dayTemperature)
    private double dayTemperature = MINIMUM_TEMPERATURE;
    @ShowField(description = ResourceIdMapper.nightTemperature)
    private double nightTemperature = MINIMUM_TEMPERATURE;
    @ShowField(description = ResourceIdMapper.windowOpenTemp)
    private double windowOpenTemp = MINIMUM_TEMPERATURE;
    @ShowField(description = ResourceIdMapper.warnings)
    private String warnings;
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true, showInFloorplan = true)
    @WidgetTemperatureField
    @WidgetMediumLine1
    private String temperature;
    @ShowField(description = ResourceIdMapper.battery)
    private String battery;

    private Map<Integer, FHTDayControl> dayControlMap = new HashMap<Integer, FHTDayControl>();

    public FHTDevice() {
        for (Integer dayId : DayUtil.getSortedDayStringIdList()) {
            dayControlMap.put(dayId, new FHTDayControl(dayId));
        }
    }

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap nodeAttributes) {
        super.onChildItemRead(tagName, key, value, nodeAttributes);

        if (key.startsWith("ACTUATOR") && value != null && value.matches("[0-9]*[%]?")) {
            double percentage = ValueExtractUtil.extractLeadingDouble(value);
            actuator = ValueDescriptionUtil.appendPercent(percentage);
        } else if (key.endsWith("FROM1") || key.endsWith("FROM2") || key.endsWith("TO1") || key.endsWith("TO2")) {
            String shortName = key.substring(0, 3);
            FHTDayControl dayControl = dayControlMap.get(DayUtil.getDayStringIdForShortName(shortName));
            if (dayControl == null) return;

            if (key.endsWith("FROM1")) dayControl.setFrom1(value);
            if (key.endsWith("FROM2")) dayControl.setFrom2(value);
            if (key.endsWith("TO1")) dayControl.setTo1(value);
            if (key.endsWith("TO2")) dayControl.setTo2(value);
        }
    }

    public void readBATTERY(String value) {
        battery = value;
    }

    public void readMEASURED_TEMP(String value) {
        temperature = ValueUtil.formatTemperature(value);
    }

    public void readWARNINGS(String value) {
        warnings = value;
    }

    public void readMODE(String value) {
        try {
            this.mode = FHTMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.mode = FHTMode.UNKNOWN;
        }
    }

    public void readDESIRED_TEMP(String value) {
        if (value.equalsIgnoreCase("off")) value = "5.5";
        if (value.equalsIgnoreCase("on")) value = "30.5";

        desiredTemp = ValueExtractUtil.extractLeadingDouble(value);
    }

    public void readDAY_TEMP(String value) {
        dayTemperature = ValueExtractUtil.extractLeadingDouble(value);
    }

    public void readNIGHT_TEMP(String value) {
        nightTemperature = ValueExtractUtil.extractLeadingDouble(value);
    }

    public void readWINDOWOPEN_TEMP(String value) {
        windowOpenTemp = ValueExtractUtil.extractLeadingDouble(value);
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

    public String getTemperature() {
        return temperature;
    }

    public void setDesiredTemp(double desiredTemp) {
        this.desiredTemp = desiredTemp;
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

    public FHTMode getMode() {
        return mode;
    }

    public void setMode(FHTMode mode) {
        this.mode = mode;
    }

    public String getBattery() {
        return battery;
    }

    public Map<Integer, FHTDayControl> getDayControlMap() {
        return Collections.unmodifiableMap(dayControlMap);
    }

    public boolean hasChangedDayControlMapValues() {
        for (FHTDayControl fhtDayControl : dayControlMap.values()) {
            if (fhtDayControl.hasChangedValues()) {
                return true;
            }
        }
        return false;
    }

    public void resetDayControlMapValues() {
        for (FHTDayControl fhtDayControl : dayControlMap.values()) {
            fhtDayControl.reset();
        }
    }

    public void setChangedDayControlMapValuesAsCurrent() {
        for (FHTDayControl fhtDayControl : dayControlMap.values()) {
            fhtDayControl.setChangedAsCurrent();
        }
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        addDeviceChartIfNotNull(temperature, new DeviceChart(R.string.temperatureGraph, R.string.yAxisTemperature,
                ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:measured"),
                ChartSeriesDescription.getDiscreteValuesInstance(R.string.desiredTemperature, "4:desired-temp")));
        addDeviceChartIfNotNull(actuator, new DeviceChart(R.string.actuatorGraph, R.string.yAxisActuator,
                new ChartSeriesDescription(R.string.actuator, "4:actuator.*[0-9]+%:0:int")));
    }
}
