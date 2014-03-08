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
import android.util.Log;

import org.w3c.dom.NamedNodeMap;

import java.util.List;

import li.klass.fhem.AndFHEMApplication;
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
import li.klass.fhem.service.graph.description.SeriesType;
import li.klass.fhem.service.room.DeviceReadCallback;
import li.klass.fhem.util.NumberUtil;
import li.klass.fhem.util.StringUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

import static li.klass.fhem.domain.CULHMDevice.SubType.DIMMER;
import static li.klass.fhem.domain.CULHMDevice.SubType.FILL_STATE;
import static li.klass.fhem.domain.CULHMDevice.SubType.HEATING;
import static li.klass.fhem.domain.CULHMDevice.SubType.KEYMATIC;
import static li.klass.fhem.domain.CULHMDevice.SubType.MOTION;
import static li.klass.fhem.domain.CULHMDevice.SubType.POWERMETER;
import static li.klass.fhem.domain.CULHMDevice.SubType.SHUTTER;
import static li.klass.fhem.domain.CULHMDevice.SubType.SWITCH;
import static li.klass.fhem.domain.CULHMDevice.SubType.THERMOSTAT;
import static li.klass.fhem.service.graph.description.ChartSeriesDescription.getRegressionValuesInstance;
import static li.klass.fhem.service.graph.description.SeriesType.ACTUATOR;
import static li.klass.fhem.service.graph.description.SeriesType.BRIGHTNESS;
import static li.klass.fhem.service.graph.description.SeriesType.CUMULATIVE_USAGE_Wh;
import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.IS_RAINING;
import static li.klass.fhem.service.graph.description.SeriesType.LITRE_CONTENT;
import static li.klass.fhem.service.graph.description.SeriesType.RAIN;
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
        KEYMATIC(DeviceFunctionality.KEY),
        POWERMETER(DeviceFunctionality.SWITCH),
        SHUTTER(DeviceFunctionality.WINDOW)
        ;

        private final DeviceFunctionality functionality;

        SubType(DeviceFunctionality functionality) {
            this.functionality = functionality;
        }
    }

    public enum HeatingMode {
        MANUAL, AUTO, CENTRAL, UNKNOWN
    }

    private HeatingMode heatingMode = HeatingMode.UNKNOWN;

    private String model;
    private String level;
    private SubType subType = null;

    public static double MAXIMUM_TEMPERATURE = 30.5;
    public static double MINIMUM_TEMPERATURE = 5.5;

    private static final CULHMConfiguration heatingConfiguration = new CULHMConfiguration();
    private WeekProfile<FilledTemperatureInterval, CULHMConfiguration, CULHMDevice> weekProfile =
            new WeekProfile<FilledTemperatureInterval, CULHMConfiguration, CULHMDevice>(heatingConfiguration);

    private double desiredTemp = MINIMUM_TEMPERATURE;
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String measuredTemp;
    @ShowField(description = ResourceIdMapper.actuator, showInOverview = true)
    private String actuator;
    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    private String humidity;
    @ShowField(description = ResourceIdMapper.model, showAfter = "definition")
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
    @ShowField(description = ResourceIdMapper.windSpeed)
    private String windSpeed;
    @ShowField(description = ResourceIdMapper.windDirection)
    private String windDirection;
    @ShowField(description = ResourceIdMapper.sunshine)
    private String sunshine;
    @ShowField(description = ResourceIdMapper.isRaining)
    private String isRaining;
    @ShowField(description = ResourceIdMapper.rain)
    private String rain;
    @ShowField(description = ResourceIdMapper.currentUsage)
    private String currentUsage;
    @ShowField(description = ResourceIdMapper.currentVoltage)
    private String currentVoltage;
    @ShowField(description = ResourceIdMapper.cumulativeUsage, showInOverview = true)
    private String cumulativeUsage;

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

    public void readDEVICE(String value) {
        setDeviceReadCallback(new DeviceReadCallback<CULHMDevice>(value) {
            @Override
            public void onCallbackDeviceRead(CULHMDevice callbackDevice) {
                if (callbackDevice != null) {
                    subType = callbackDevice.getSubType();
                }
            }
        });
    }

    public void readSUBTYPE(String value) {
        if ("DIMMER".equalsIgnoreCase(value) || "BLINDACTUATOR".equalsIgnoreCase(value)) {
            subType = DIMMER;
        } else if ("SWITCH".equalsIgnoreCase(value)) {
            subType = SWITCH;
        } else if ("SMOKEDETECTOR".equalsIgnoreCase(value)) {
            subType = SubType.SMOKE_DETECTOR;
        } else if ("THREESTATESENSOR".equalsIgnoreCase(value)) {
            subType = SubType.THREE_STATE;
        } else if ("THSensor".equalsIgnoreCase(value)) {
            subType = SubType.TEMPERATURE_HUMIDITY;
        } else if ("KFM100".equalsIgnoreCase(value)) {
            subType = SubType.FILL_STATE;
        } else if ("THERMOSTAT".equalsIgnoreCase(value)) {
            subType = THERMOSTAT;
        } else if ("MOTIONDETECTOR".equalsIgnoreCase(value)) {
            subType = MOTION;
        } else if (("KEYMATIC").equalsIgnoreCase(value)) {
            subType = KEYMATIC;
        } else if ("powerMeter".equalsIgnoreCase(value)) {
            subType = POWERMETER;
        }
        subTypeRaw = value;
    }

    public void readCONTROLMODE(String value) {
        try {
            heatingMode = HeatingMode.valueOf(value.toUpperCase());
            subType = HEATING;
        } catch (Exception e) {
            Log.e(CULHMDevice.class.getName(), "cannot set heating mode from value " + value, e);
        }
    }

    public void readMODE(String value) {
        if (value.equalsIgnoreCase("MANU")) value = "MANUAL";
        readCONTROLMODE(value);
    }

    public void readWINDSPEED(String value) {
        windSpeed = ValueDescriptionUtil.append(value, "m/s");
    }

    public void readWINDDIRECTION(String value) {
        windDirection = ValueDescriptionUtil.append(value, "°");
    }

    public void readSUNSHINE(String value) {
        sunshine = value;
    }

    public void readISRAINING(String value) {
        Context context = AndFHEMApplication.getContext();
        int stringId = "0".equals(value) ? R.string.no : R.string.yes;
        isRaining = context.getString(stringId);
    }

    public void readRAIN(String value) {
        rain = ValueDescriptionUtil.appendLm2(value);
    }

    public void readPOWER(String value) {
        currentUsage = ValueDescriptionUtil.append(value, "W");
    }

    public void readENERGY(String value) {
        cumulativeUsage = ValueDescriptionUtil.append(value, "W");
    }

    public void readVOLTAGE(String value) {
        currentVoltage = ValueDescriptionUtil.append(value, "A");
    }

    public void readLEVEL(String value) {
        level = value;
    }

    @Override
    public void afterDeviceXMLRead() {
        if (getDeviceFunctionality() == DeviceFunctionality.HEATING) {
            weekProfile.afterXMLRead();
        }

        super.afterDeviceXMLRead();
    }

    @Override
    public void afterAllXMLRead() {

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

        if ("HM-Sen-Wa-Od".equals(model)) {
            subType = FILL_STATE;
            fillContentPercentageRaw = ValueExtractUtil.extractLeadingDouble(level) / 100;
            fillContentPercentage = ValueDescriptionUtil.appendPercent((int) (fillContentPercentageRaw * 100));
        }

        if ("HM-LC-BL1-FM".equals(model)) {
            subType = SHUTTER;
        }

        super.afterAllXMLRead();
    }

    @Override
    public boolean isSupported() {
        return super.isSupported() && subType != null;
    }

    public boolean isOnByState() {
        if (super.isOnByState()) return true;

        String internalState = getInternalState();
        return internalState.equalsIgnoreCase("on") || internalState.equalsIgnoreCase("on-for-timer") ||
                (subType == DIMMER && getDimPosition() > 0);
    }

    @Override
    public boolean supportsToggle() {
        return subType == SWITCH || subType == DIMMER || subType == POWERMETER;
    }

    @Override
    public int getDimLowerBound() {
        if (getStateSliderValue() != null) {
            return super.getDimLowerBound();
        }
        return 0;
    }

    @Override
    public int getDimStep() {
        if (getStateSliderValue() != null) {
            return super.getDimStep();
        }
        return 1;
    }

    @Override
    public int getDimUpperBound() {
        if (getStateSliderValue() != null) {
            return super.getDimUpperBound();
        }
        return 100;
    }

    @Override
    public boolean supportsDim() {
        return subType == DIMMER || subType == SHUTTER;
    }

    @Override
    public int getDimPosition() {
        if (subType != DIMMER && subType != SHUTTER) return 0;
        return super.getDimPosition();
    }

    @Override
    public String formatStateTextToSet(String stateToSet) {
        if ((getSubType() == DIMMER || getSubType() == SHUTTER) && NumberUtil.isNumeric(stateToSet)) {
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

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getSunshine() {
        return sunshine;
    }

    public String getIsRaining() {
        return isRaining;
    }

    public String getRain() {
        return rain;
    }

    public String getCumulativeUsage() {
        return cumulativeUsage;
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

    public String getCurrentUsage() {
        return currentUsage;
    }

    public String getCurrentVoltage() {
        return currentVoltage;
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

        System.out.println("subtype: " + subType);
        if (subType == null) return;

        switch (subType) {
            case TEMPERATURE_HUMIDITY:

                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                        getRegressionValuesInstance(R.string.temperature,
                                "4:T\\x3a:0:", "measured-temp", TEMPERATURE),
                        new ChartSeriesDescription(R.string.humidity, "6:H\\x3a:0:", "humidity",
                                HUMIDITY)
                ), humidity, measuredTemp);

                if (humidity == null) {
                    addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                            getRegressionValuesInstance(R.string.temperature,
                                    "4:T\\x3a:0:", "measured-temp", TEMPERATURE)
                    ), measuredTemp);
                }

                addDeviceChartIfNotNull(
                        new DeviceChart(R.string.brightnessSunshineGraph,
                                new ChartSeriesDescription(R.string.brightness, "4:brightness",
                                        "brightness", BRIGHTNESS),
                                new ChartSeriesDescription(R.string.sunshine,
                                        "4:sunshine", "sunshine", SeriesType.SUNSHINE)
                        ), sunshine, brightness);

                addDeviceChartIfNotNull(
                        new DeviceChart(R.string.rainGraph,
                                ChartSeriesDescription.getDiscreteValuesInstance(R.string.isRaining,
                                        "4:isRaining", "isRaining", IS_RAINING),
                                new ChartSeriesDescription(R.string.rain, "4:rain", "rain", RAIN)
                        ), isRaining, rain
                );

                break;

            case FILL_STATE:
                addDeviceChartIfNotNull(new DeviceChart(R.string.contentGraph,
                        getRegressionValuesInstance(R.string.content, "4:content:0:", "content",
                                LITRE_CONTENT),
                        new ChartSeriesDescription(R.string.rawValue, "4:rawValue:0:", "rawValue",
                                RAW)
                ), getState());

                break;

            case THERMOSTAT:

                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                        getRegressionValuesInstance(R.string.temperature, "4:measured-temp:0",
                                "measured-temp", TEMPERATURE)
                ), measuredTemp);

                break;

            case HEATING:
                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                        getRegressionValuesInstance(R.string.temperature, "4:T\\x3a:0:",
                                "measured-temp", TEMPERATURE),
                        new ChartSeriesDescription(R.string.humidity, "6:H\\x3a:0:", "humidity",
                                HUMIDITY)), humidity, measuredTemp);

                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureActuatorGraph,
                        getRegressionValuesInstance(R.string.temperature, "4:measured-temp:0",
                                "measured-temp", TEMPERATURE),
                        new ChartSeriesDescription(R.string.actuator, "4:actuator", "actuator::int",
                                ACTUATOR)), measuredTemp, actuator);

                break;

            case POWERMETER:
                System.out.println(currentUsage);
                System.out.println(cumulativeUsage);
                addDeviceChartIfNotNull(new DeviceChart(R.string.usageGraph,
                        new ChartSeriesDescription(R.string.currentUsage, "4:current:0", "current",
                                SeriesType.CURRENT_USAGE_WATT)), currentUsage);

                addDeviceChartIfNotNull(new DeviceChart(R.string.usageGraphCumulative,
                        new ChartSeriesDescription(R.string.cumulativeUsage, "4:energy", "energy",
                                CUMULATIVE_USAGE_Wh)), cumulativeUsage);
                break;
        }
    }

    public boolean isLastCommandAccepted() {
        return StringUtil.isBlank(commandAccepted) || "yes".equalsIgnoreCase(commandAccepted);
    }

    @Override
    public boolean supportsWidget(Class<? extends AppWidgetView> appWidgetClass) {
        if (appWidgetClass.equals(TemperatureWidgetView.class) &&
                !(subType == SubType.TEMPERATURE_HUMIDITY || subType == HEATING)) {
            return false;
        }

        return super.supportsWidget(appWidgetClass);
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }

    @Override
    public long getTimeRequiredForStateError() {
        return OUTDATED_DATA_MS_DEFAULT;
    }

    @Override
    protected String getSetListDimStateAttributeName() {
        return "pct";
    }
}
