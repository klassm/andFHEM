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

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.domain.heating.HeatingDevice;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.configuration.CULHMConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.NumberUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

import static li.klass.fhem.domain.CULHMDevice.SubType.FILL_STATE;
import static li.klass.fhem.domain.CULHMDevice.SubType.HEATING;
import static li.klass.fhem.domain.CULHMDevice.SubType.KEYMATIC;
import static li.klass.fhem.domain.CULHMDevice.SubType.MOTION;
import static li.klass.fhem.domain.CULHMDevice.SubType.THERMOSTAT;
import static li.klass.fhem.service.graph.description.SeriesType.ACTUATOR;
import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.LITRE_CONTENT;
import static li.klass.fhem.service.graph.description.SeriesType.RAW;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static li.klass.fhem.util.ValueDescriptionUtil.appendPercent;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true)
@SupportsWidget(TemperatureWidgetView.class)
public class CULHMDevice extends DimmableContinuousStatesDevice<CULHMDevice>
        implements DesiredTempDevice, HeatingDevice<CULHMDevice.HeatingMode, CULHMConfiguration, FilledTemperatureInterval, CULHMDevice> {

    private HeatingMode heatingMode = HeatingMode.UNKNOWN;
    private String model;

    public enum SubType {
        DIMMER(DeviceFunctionality.DIMMER),
        SWITCH(DeviceFunctionality.SWITCH),
        HEATING(DeviceFunctionality.HEATING),
        SMOKE_DETECTOR(DeviceFunctionality.SMOKE_DETECTOR),
        THREE_STATE(DeviceFunctionality.WINDOW),
        TEMPERATURE_HUMIDITY(DeviceFunctionality.TEMPERATURE),
        THERMOSTAT(DeviceFunctionality.HEATING),
        FILL_STATE(DeviceFunctionality.FILL_STATE),
        MOTION(DeviceFunctionality.MOTION_DETECTOR),
        KEYMATIC(DeviceFunctionality.KEY);

        private final DeviceFunctionality functionality;

        SubType(DeviceFunctionality functionality) {
            this.functionality = functionality;
        }
    }

    public enum HeatingMode {
        MANUAL, AUTO, CENTRAL, UNKNOWN
    }

    private SubType subType = null;

    public static double MAXIMUM_TEMPERATURE = 30.5;
    public static double MINIMUM_TEMPERATURE = 5.5;

    private static final CULHMConfiguration heatingConfiguration = new CULHMConfiguration();
    private WeekProfile<FilledTemperatureInterval, CULHMConfiguration, CULHMDevice> weekProfile = new WeekProfile<FilledTemperatureInterval, CULHMConfiguration, CULHMDevice>(heatingConfiguration);

    private double desiredTemp = MINIMUM_TEMPERATURE;
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String measuredTemp;
    @ShowField(description = ResourceIdMapper.actuator, showInOverview = true)
    private String actuator;
    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    private String humidity;
    @ShowField(description = ResourceIdMapper.model)
    private String subTypeRaw;
    @ShowField(description = ResourceIdMapper.commandAccepted)
    private String commandAccepted;
    @ShowField(description = ResourceIdMapper.rawValue)
    private String rawValue;
    private double fillContentLitresRaw;
    @ShowField(description = ResourceIdMapper.maximumContent)
    private Integer fillContentLitresMaximum;
    private double fillContentPercentageRaw;
    @ShowField(description = ResourceIdMapper.fillPercentage, showInOverview = true, showInDetail = false)
    private String fillContentPercentage;
    @ShowField(description = ResourceIdMapper.conversion)
    private String rawToReadable;
    @ShowField(description = ResourceIdMapper.battery)
    private String battery;
    @ShowField(description = ResourceIdMapper.brightness)
    private String brightness;
    @ShowField(description = ResourceIdMapper.motion)
    private String motion;

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        super.onChildItemRead(tagName, key, value, attributes);
        weekProfile.readNode(key, value);
    }

    public void readRAWTOREADABLE(String value) {
        this.rawToReadable = value;

        int lastSpace = value.lastIndexOf(" ");
        String lastDefinition = lastSpace == -1 ? value : value.substring(lastSpace + 1);
        String[] parts = lastDefinition.split(":");
        if (parts.length != 2) return;

        int rawValue = Integer.parseInt(parts[0]);

        fillContentLitresMaximum = Integer.parseInt(parts[1]);
    }

    public void readRAWVALUE(String value) {
        this.rawValue = value;
    }

    public void readCONTENT(String value) {
        fillContentLitresRaw = extractLeadingDouble(value.replace("l", ""));
        String fillContentLitres = ValueDescriptionUtil.appendL(fillContentLitresRaw);
        setState(fillContentLitres);
    }

    public void readCOMMANDACCEPTED(String value) {
        this.commandAccepted = value;
    }

    public void readHUMIDITY(String value) {
        humidity = appendPercent(value);
    }

    public void readACTUATOR(String value) {
        subType = HEATING;
        if (value != null && value.endsWith("%")) {
            value = appendPercent(extractLeadingInt(value));
        }
        actuator = value;
    }

    public void readMEASURED_TEMP(String value) {
        measuredTemp = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readTEMPERATURE(String value) {
        measuredTemp = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readDESIRED_TEMP(String value) {
        if (value.equalsIgnoreCase("off")) value = "5.5";
        if (value.equalsIgnoreCase("on")) value = "30.5";

        desiredTemp = extractLeadingDouble(value);
    }

    public void readBATTERY(String value) {
        battery = value;
    }

    public void readBRIGHTNESS(String value) {
        brightness = value;
    }

    public void readMOTION(String value) {
        motion = value;
    }

    public void readMODEL(String value) {
        model = value;
    }

    public void readSUBTYPE(String value) {
        if (value.equalsIgnoreCase("DIMMER") || value.equalsIgnoreCase("BLINDACTUATOR")) {
            subType = SubType.DIMMER;
        } else if (value.equalsIgnoreCase("SWITCH")) {
            subType = SubType.SWITCH;
        } else if (value.equalsIgnoreCase("SMOKEDETECTOR")) {
            subType = SubType.SMOKE_DETECTOR;
        } else if (value.equalsIgnoreCase("THREESTATESENSOR")) {
            subType = SubType.THREE_STATE;
        } else if (value.equalsIgnoreCase("THSensor")) {
            subType = SubType.TEMPERATURE_HUMIDITY;
        } else if (value.equalsIgnoreCase("KFM100")) {
            subType = SubType.FILL_STATE;
        } else if (value.equalsIgnoreCase("THERMOSTAT")) {
            subType = THERMOSTAT;
        } else if (value.equalsIgnoreCase("MOTIONDETECTOR")) {
            subType = MOTION;
        } else if (value.equalsIgnoreCase("KEYMATIC")) {
            subType = KEYMATIC;
        }
        subTypeRaw = value;
    }

    public void readCONTROLMODE(String value) {
        try {
            heatingMode = HeatingMode.valueOf(value.toUpperCase());
        } catch (Exception e) {
            Log.e(CULHMDevice.class.getName(), "cannot set heating mode from value " + value, e);
        }
    }

    public void readMODE(String value) {
        readCONTROLMODE(value);
    }

    @Override
    public void afterXMLRead() {
        super.afterXMLRead();

        weekProfile.afterXMLRead();

        if (getAssociatedDeviceCallback() != null) {
            CULHMDevice device = getAssociatedDeviceCallback().getAssociatedDevice();
            if (device != null) {
                subType = device.getSubType();
            }
        }

        if (subType == SubType.FILL_STATE && fillContentLitresMaximum != null) {
            if (fillContentLitresRaw > fillContentLitresMaximum) {
                fillContentLitresRaw = fillContentLitresMaximum;
            }

            fillContentPercentageRaw = fillContentLitresRaw == 0 ? 0 : fillContentLitresRaw / fillContentLitresMaximum;
            fillContentPercentage = appendPercent((int) (fillContentPercentageRaw * 100));
        }

        if (model != null && model.equalsIgnoreCase("HM-Sen-Wa-Od")) {
            subType = FILL_STATE;


            fillContentPercentageRaw = ValueExtractUtil.extractLeadingDouble(getInternalState()) / 100d;
            fillContentPercentage = ValueDescriptionUtil.appendPercent((int) (fillContentPercentageRaw * 100));

            setState(fillContentPercentage);
        }
    }

    @Override
    public boolean isSupported() {
        return subType != null;
    }

    public boolean isOnByState() {
        if (super.isOnByState()) return true;

        String internalState = getInternalState();
        return internalState.equalsIgnoreCase("on") || internalState.equalsIgnoreCase("on-for-timer") ||
                (subType == SubType.DIMMER && getDimPosition() > 0);
    }

    @Override
    public boolean supportsToggle() {
        return subType == SubType.SWITCH || subType == SubType.DIMMER;
    }

    @Override
    public int getDimUpperBound() {
        return 100;
    }

    @Override
    public boolean supportsDim() {
        return subType == SubType.DIMMER;
    }

    @Override
    public int getDimPosition() {
        if (subType != SubType.DIMMER) return 0;
        return super.getDimPosition();
    }

    @Override
    public int getPositionForDimState(String dimState) {
        if (eventMapReverse.containsKey(dimState)) {
            dimState = eventMapReverse.get(dimState);
        }
        dimState = dimState.replaceAll("[ ]?%", "");
        return super.getPositionForDimState(dimState);
    }

    @Override
    public String formatStateTextToSet(String stateToSet) {
        if (getSubType() == SubType.DIMMER && NumberUtil.isNumeric(stateToSet)) {
            return stateToSet + " %";
        }
        return super.formatStateTextToSet(stateToSet);
    }

    @Override
    public DeviceFunctionality getDeviceFunctionality() {
        if (subType != null) {
            return subType.functionality;
        }
        return DeviceFunctionality.UNKNOWN;
    }

    public String getMeasured() {
        return measured;
    }

    @ShowField(description = ResourceIdMapper.desiredTemperature, showInOverview = true)
    public String getDesiredTempDesc() {
        if (subType != HEATING) return null;

        return ValueDescriptionUtil.desiredTemperatureToString(desiredTemp, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE);
    }

    @Override
    public String getDesiredTempCommandFieldName() {
        return "desired-temp";
    }

    @Override
    public void setDesiredTemp(double desiredTemp) {
        this.desiredTemp = desiredTemp;
    }

    public double getDesiredTemp() {
        return desiredTemp;
    }

    public String getMeasuredTemp() {
        return measuredTemp;
    }

    public String getActuator() {
        return actuator;
    }

    public SubType getSubType() {
        return subType;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getSubTypeRaw() {
        return subTypeRaw;
    }

    public String getCommandAccepted() {
        return commandAccepted;
    }

    public String getRawValue() {
        return rawValue;
    }

    public String getRawToReadable() {
        return rawToReadable;
    }

    public double getFillContentPercentageRaw() {
        return fillContentPercentageRaw;
    }

    public double getFillContentLitresRaw() {
        return fillContentLitresRaw;
    }

    public int getFillContentLitresMaximum() {
        return fillContentLitresMaximum;
    }

    public String getFillContentPercentage() {
        return fillContentPercentage;
    }

    public String getBattery() {
        return battery;
    }

    public String getBrightness() {
        return brightness;
    }

    public String getMotion() {
        return motion;
    }

    @Override
    public void setHeatingMode(HeatingMode heatingMode) {
        this.heatingMode = heatingMode;
    }

    @Override
    public HeatingMode getHeatingMode() {
        return heatingMode;
    }

    @Override
    public HeatingMode[] getIgnoredHeatingModes() {
        return new HeatingMode[0];
    }

    @Override
    public HeatingMode[] getHeatingModes() {
        return HeatingMode.values();
    }

    @Override
    public String getHeatingModeCommandField() {
        return "controlMode";
    }

    @Override
    public WeekProfile<FilledTemperatureInterval, CULHMConfiguration, CULHMDevice> getWeekProfile() {
        return weekProfile;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        if (subType == null) return;

        switch (subType) {
            case TEMPERATURE_HUMIDITY:

                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                        ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:T\\x3a:0:", TEMPERATURE),
                        new ChartSeriesDescription(R.string.humidity, "6:H\\x3a:0:", HUMIDITY)), humidity, measuredTemp);

                if (humidity == null) {
                    addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                            ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:T\\x3a:0:", TEMPERATURE)),
                            measuredTemp);
                }

                break;

            case FILL_STATE:
                addDeviceChartIfNotNull(new DeviceChart(R.string.contentGraph,
                        ChartSeriesDescription.getRegressionValuesInstance(R.string.content, "4:content:0:", LITRE_CONTENT),
                        new ChartSeriesDescription(R.string.rawValue, "4:rawValue:0:", RAW)), getState());

                break;

            case THERMOSTAT:

                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                        ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:measured-temp:0", TEMPERATURE)), measuredTemp);

                break;

            case HEATING:
                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                        ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:T\\x3a:0:", TEMPERATURE),
                        new ChartSeriesDescription(R.string.humidity, "6:H\\x3a:0:", HUMIDITY)), humidity, measuredTemp);

                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureActuatorGraph,
                        ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:measured-temp:0", TEMPERATURE),
                        new ChartSeriesDescription(R.string.actuator, "4:actuator", ACTUATOR)), measuredTemp, actuator);

                break;

        }
    }

    @Override
    public boolean supportsWidget(Class<? extends AppWidgetView> appWidgetClass) {
        if (appWidgetClass.equals(TemperatureWidgetView.class) &&
                !(subType == SubType.TEMPERATURE_HUMIDITY || subType == HEATING)) {
            return false;
        }

        return super.supportsWidget(appWidgetClass);
    }
}
