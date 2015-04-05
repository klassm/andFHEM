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

import com.google.common.base.Strings;

import java.util.List;
import java.util.Locale;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableContinuousStatesDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.heating.DesiredTempDevice;
import li.klass.fhem.domain.heating.HeatingDevice;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.configuration.CULHMConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.FilledTemperatureInterval;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.graph.description.SeriesType;
import li.klass.fhem.service.room.DeviceReadCallback;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.util.NumberUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

import static li.klass.fhem.domain.CULHMDevice.SubType.DIMMER;
import static li.klass.fhem.domain.CULHMDevice.SubType.FILL_STATE;
import static li.klass.fhem.domain.CULHMDevice.SubType.KEYMATIC;
import static li.klass.fhem.domain.CULHMDevice.SubType.MOTION;
import static li.klass.fhem.domain.CULHMDevice.SubType.POWERMETER;
import static li.klass.fhem.domain.CULHMDevice.SubType.SHUTTER;
import static li.klass.fhem.domain.CULHMDevice.SubType.SWITCH;
import static li.klass.fhem.domain.CULHMDevice.SubType.THERMOSTAT;
import static li.klass.fhem.domain.CULHMDevice.SubType.THPL;
import static li.klass.fhem.service.graph.description.SeriesType.ACTUATOR;
import static li.klass.fhem.service.graph.description.SeriesType.BRIGHTNESS;
import static li.klass.fhem.service.graph.description.SeriesType.CUMULATIVE_USAGE_Wh;
import static li.klass.fhem.service.graph.description.SeriesType.CURRENT_USAGE_WATT;
import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.IS_RAINING;
import static li.klass.fhem.service.graph.description.SeriesType.LITRE_CONTENT;
import static li.klass.fhem.service.graph.description.SeriesType.PRESSURE;
import static li.klass.fhem.service.graph.description.SeriesType.RAIN;
import static li.klass.fhem.service.graph.description.SeriesType.RAW;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;
import static li.klass.fhem.util.ValueDescriptionUtil.appendPercent;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;

@OverviewViewSettings(showState = true)
@SupportsWidget(TemperatureWidgetView.class)
public class CULHMDevice extends DimmableContinuousStatesDevice<CULHMDevice>
        implements DesiredTempDevice, HeatingDevice<CULHMDevice.HeatingMode, CULHMConfiguration, FilledTemperatureInterval, CULHMDevice> {

    public static double MINIMUM_TEMPERATURE = 5.5;
    public static double MAXIMUM_TEMPERATURE = 30.5;

    private static final CULHMConfiguration HEATING_CONFIGURATION = new CULHMConfiguration();

    private WeekProfile<FilledTemperatureInterval, CULHMConfiguration, CULHMDevice> weekProfile =
            new WeekProfile<>(HEATING_CONFIGURATION);

    private double desiredTemp = MINIMUM_TEMPERATURE;

    private HeatingMode heatingMode = HeatingMode.UNKNOWN;

    private SubType subType = null;

    private double fillContentLitresRaw;
    private double fillContentPercentageRaw;

    @XmllistAttribute("MODEL")
    private String model;

    @XmllistAttribute("LEVEL")
    private String level;

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    @XmllistAttribute({"MEASURED_TEMP", "TEMPERATURE"})
    private String measuredTemp;

    @ShowField(description = ResourceIdMapper.actuator, showInOverview = true)
    private String actuator;

    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    @WidgetTemperatureAdditionalField
    @XmllistAttribute("HUMIDITY")
    private String humidity;

    @ShowField(description = ResourceIdMapper.model, showAfter = "definition")
    private String subTypeRaw;

    @ShowField(description = ResourceIdMapper.commandAccepted)
    private String commandAccepted;

    @ShowField(description = ResourceIdMapper.rawValue)
    @XmllistAttribute("RAWVALUE")
    private String rawValue;

    @ShowField(description = ResourceIdMapper.maximumContent)
    private Integer fillContentLitresMaximum;

    @ShowField(description = ResourceIdMapper.fillPercentage, showInOverview = true, showInDetail = false)
    private String fillContentPercentage;

    @ShowField(description = ResourceIdMapper.conversion)
    private String rawToReadable;

    @ShowField(description = ResourceIdMapper.battery)
    @XmllistAttribute("BATTERY")
    private String battery;

    @ShowField(description = ResourceIdMapper.brightness)
    @XmllistAttribute("BRIGHTNESS")
    private String brightness;

    @ShowField(description = ResourceIdMapper.motion)
    @XmllistAttribute("MOTION")
    private String motion;

    @ShowField(description = ResourceIdMapper.windSpeed)
    @XmllistAttribute("WINDSPEED")
    private String windSpeed;

    @ShowField(description = ResourceIdMapper.windDirection)
    @XmllistAttribute("WINDDIRECTION")
    private String windDirection;

    @ShowField(description = ResourceIdMapper.sunshine)
    @XmllistAttribute("SUNSHINE")
    private String sunshine;

    @ShowField(description = ResourceIdMapper.isRaining)
    private String isRaining;

    @ShowField(description = ResourceIdMapper.rain)
    @XmllistAttribute("RAIN")
    private String rain;

    @ShowField(description = ResourceIdMapper.currentPower)
    @XmllistAttribute("POWER")
    private String power;

    @ShowField(description = ResourceIdMapper.voltage)
    @XmllistAttribute("VOLTAGE")
    private String currentVoltage;

    @ShowField(description = ResourceIdMapper.cumulativeUsage, showInOverview = true)
    @XmllistAttribute("ENERGY")
    private String cumulativeUsage;

    @ShowField(description = ResourceIdMapper.brightness)
    @XmllistAttribute("LUMINOSITY")
    private String luminosity;

    @ShowField(description = ResourceIdMapper.batteryVoltage)
    @XmllistAttribute("BATVOLTAGE")
    private String batteryVoltage;

    @ShowField(description = ResourceIdMapper.pressure)
    @XmllistAttribute("PRESSURE")
    private String pressure;

    @ShowField(description = ResourceIdMapper.pressureNN)
    @XmllistAttribute("PRESSURE_NN")
    private String pressureNN;

    @ShowField(description = ResourceIdMapper.energy_frequency)
    @XmllistAttribute("FREQUENCY")
    private String frequency;

    @ShowField(description = ResourceIdMapper.current)
    @XmllistAttribute("CURRENT")
    private String current;

    @Override
    public void onChildItemRead(DeviceNode.DeviceNodeType type, String key, String value, DeviceNode node) {
        super.onChildItemRead(type, key, value, node);
        weekProfile.readNode(key, value);
    }

    @XmllistAttribute("CONTENT")
    public void setContent(String value) {
        fillContentLitresRaw = extractLeadingDouble(value);
        String fillContentLitres = ValueDescriptionUtil.appendL(fillContentLitresRaw);
        setState(fillContentLitres);
    }

    @XmllistAttribute("DESIRED_TEMP")
    public void setDesiredTemp(String value) {
        if (value.equalsIgnoreCase("off")) value = "5.5";
        if (value.equalsIgnoreCase("on")) value = "30.5";

        desiredTemp = extractLeadingDouble(value);
    }

    @XmllistAttribute("DEVICE")
    public void setDevice(String value) {
        setDeviceReadCallback(new DeviceReadCallback<CULHMDevice>(value) {
            @Override
            public void onCallbackDeviceRead(CULHMDevice callbackDevice) {
                if (callbackDevice != null && callbackDevice.getSubType() != null) {
                    subType = callbackDevice.getSubType();
                }
            }
        });
    }

    public SubType getSubType() {
        return subType;
    }

    @XmllistAttribute("SUBTYPE")
    public void setSubType(String value) {
        if ("DIMMER".equalsIgnoreCase(value) || "BLINDACTUATOR".equalsIgnoreCase(value)) {
            subType = DIMMER;
        } else if ("SWITCH".equalsIgnoreCase(value)) {
            subType = SWITCH;
        } else if ("SMOKEDETECTOR".equalsIgnoreCase(value)) {
            subType = SubType.SMOKE_DETECTOR;
        } else if ("THREESTATESENSOR".equalsIgnoreCase(value)) {
            subType = SubType.THREE_STATE;
        } else if ("THSensor".equalsIgnoreCase(value)) {
            subType = SubType.TH;
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
        } else if ("THPLSensor".equalsIgnoreCase(value)) {
            subType = THPL;
        }
        subTypeRaw = value;
    }

    @XmllistAttribute("MODE")
    public void setMode(String value) {
        if (value.equalsIgnoreCase("MANU")) {
            value = "MANUAL";
        }
        setControlMode(value);
    }

    @XmllistAttribute("CONTROLMODE")
    public void setControlMode(String value) {
        try {
            heatingMode = HeatingMode.valueOf(value.toUpperCase(Locale.getDefault()));
            subType = THERMOSTAT;
        } catch (Exception e) {
            Log.e(CULHMDevice.class.getName(), "cannot set heating mode from value " + value, e);
        }
    }

    @Override
    public void afterDeviceXMLRead(Context context) {
        if (actuator != null) {
            subType = THERMOSTAT;
        }
        if (getDeviceGroup() == DeviceFunctionality.HEATING) {
            weekProfile.afterXMLRead();
        }

        super.afterDeviceXMLRead(context);
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        if (subType != null) {
            return subType.functionality;
        }
        return DeviceFunctionality.UNKNOWN;
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

        if ("HM-Sen-Wa-Od".equals(model)) {
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

        if ("HM-LC-BL1-FM".equals(model) || "HM-LC-Bl1PBU-FM".equals(model)) {
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
    public int getDimPosition() {
        if (subType != DIMMER && subType != SHUTTER) return 0;
        return super.getDimPosition();
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
    public String formatStateTextToSet(String stateToSet) {
        if ((getSubType() == DIMMER || getSubType() == SHUTTER) && NumberUtil.isDecimalNumber(stateToSet)) {
            return stateToSet + " %";
        }
        return super.formatStateTextToSet(stateToSet);
    }

    @ShowField(description = ResourceIdMapper.desiredTemperature, showInOverview = true)
    public String getDesiredTempDesc() {
        if (subType != THERMOSTAT) return null;

        return ValueDescriptionUtil.desiredTemperatureToString(desiredTemp, MINIMUM_TEMPERATURE, MAXIMUM_TEMPERATURE);
    }

    @Override
    public String getDesiredTempCommandFieldName() {
        return "desired-temp";
    }

    public double getDesiredTemp() {
        return desiredTemp;
    }

    @Override
    public void setDesiredTemp(double desiredTemp) {
        this.desiredTemp = desiredTemp;
    }

    public String getMeasuredTemp() {
        return measuredTemp;
    }

    public String getActuator() {
        return actuator;
    }

    @XmllistAttribute("ACTUATOR")
    public void setActuator(String value) {
        subType = THERMOSTAT;
        actuator = value;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getRawValue() {
        return rawValue;
    }

    public String getRawToReadable() {
        return rawToReadable;
    }

    @XmllistAttribute("RAWTOREADABLE")
    public void setRawToReadable(String value) {
        this.rawToReadable = value;

        int lastSpace = value.lastIndexOf(" ");
        String lastDefinition = lastSpace == -1 ? value : value.substring(lastSpace + 1);
        String[] parts = lastDefinition.split(":");
        if (parts.length != 2) return;

        fillContentLitresMaximum = Integer.parseInt(parts[1]);
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

    @XmllistAttribute("ISRAINING")
    public void setIsRaining(String value) {
        Context context = AndFHEMApplication.getContext();
        int stringId = "0".equals(value) ? R.string.no : R.string.yes;
        isRaining = context.getString(stringId);
    }

    public String getRain() {
        return rain;
    }

    public String getCumulativeUsage() {
        return cumulativeUsage;
    }

    @Override
    public HeatingMode getHeatingMode() {
        return heatingMode;
    }

    @Override
    public void setHeatingMode(HeatingMode heatingMode) {
        this.heatingMode = heatingMode;
    }

    @Override
    public HeatingMode[] getIgnoredHeatingModes() {
        return new HeatingMode[0];
    }

    @Override
    public HeatingMode[] getHeatingModes() {
        return HeatingMode.values();
    }

    public String getPower() {
        return power;
    }

    public String getCurrentVoltage() {
        return currentVoltage;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getCurrent() {
        return current;
    }

    @Override
    public String getHeatingModeCommandField() {
        return "controlMode";
    }

    public String getLuminosity() {
        return luminosity;
    }

    public String getBatteryVoltage() {
        return batteryVoltage;
    }

    public String getPressure() {
        return pressure;
    }

    public String getPressureNN() {
        return pressureNN;
    }

    @Override
    public WeekProfile<FilledTemperatureInterval, CULHMConfiguration, CULHMDevice> getWeekProfile() {
        return weekProfile;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries, Context context) {
        super.fillDeviceCharts(chartSeries, context);

        if (subType == null) return;

        switch (subType) {
            case TH:

                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.temperature, context)
                                .withFileLogSpec("4:T\\x3a:0:")
                                .withDbLogSpec("measured-temp")
                                .withSeriesType(TEMPERATURE)
                                .withShowRegression(true)
                                .build(),
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.humidity, context).withFileLogSpec("6:H\\x3a:0:")
                                .withDbLogSpec("humidity")
                                .withSeriesType(HUMIDITY)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                                .build()
                ), humidity, measuredTemp);

                if (humidity == null) {
                    addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                            new ChartSeriesDescription.Builder()
                                    .withColumnName(R.string.temperature, context)
                                    .withFileLogSpec("4:T\\x3a:0:")
                                    .withDbLogSpec("measured-temp")
                                    .withSeriesType(TEMPERATURE)
                                    .withShowRegression(true)
                                    .build()
                    ), measuredTemp);
                }

                addDeviceChartIfNotNull(
                        new DeviceChart(R.string.brightnessSunshineGraph,
                                new ChartSeriesDescription.Builder()
                                        .withColumnName(R.string.brightness, context).withFileLogSpec("4:brightness")
                                        .withDbLogSpec("brightness")
                                        .withSeriesType(BRIGHTNESS)
                                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("brightness", 0, 1000))
                                        .build(),
                                new ChartSeriesDescription.Builder()
                                        .withColumnName(R.string.sunshine, context).withFileLogSpec("4:sunshine")
                                        .withDbLogSpec("sunshine")
                                        .withSeriesType(SeriesType.SUNSHINE)
                                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("sunshine", 0, 0))
                                        .build()
                        ), sunshine, brightness
                );

                addDeviceChartIfNotNull(
                        new DeviceChart(R.string.rainGraph,
                                new ChartSeriesDescription.Builder()
                                        .withColumnName(R.string.isRaining, context)
                                        .withFileLogSpec("4:isRaining")
                                        .withDbLogSpec("isRaining")
                                        .withSeriesType(IS_RAINING)
                                        .withShowDiscreteValues(true)
                                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("isRaining", 0, 1))
                                        .build(),
                                new ChartSeriesDescription.Builder()
                                        .withColumnName(R.string.rain, context).withFileLogSpec("4:rain")
                                        .withDbLogSpec("rain")
                                        .withSeriesType(RAIN)
                                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("rain", 0, 70))
                                        .build()
                        ), isRaining, rain
                );

                addDeviceChartIfNotNull(
                        new DeviceChart(R.string.windSpeedGraph,
                                new ChartSeriesDescription.Builder()
                                        .withColumnName(R.string.windSpeed, context)
                                        .withFileLogSpec("4:windSpeed")
                                        .withDbLogSpec("windSpeed")
                                        .withSeriesType(SeriesType.WIND)
                                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("windSpeed", 0, 0))
                                        .build()),
                        windSpeed
                );

                break;

            case FILL_STATE:
                addDeviceChartIfNotNull(new DeviceChart(R.string.contentGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.content, context)
                                .withFileLogSpec("4:content:0:")
                                .withDbLogSpec("content")
                                .withSeriesType(LITRE_CONTENT)
                                .withShowRegression(true)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("content", 0, 0))
                                .build(),
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.rawValue, context).withFileLogSpec("4:rawValue:0:")
                                .withDbLogSpec("rawValue")
                                .withSeriesType(RAW)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("rawValue", 0, 300))
                                .build()
                ), getState());

                break;

            case THERMOSTAT:

                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.temperature, context)
                                .withFileLogSpec("4:T\\x3a:0:")
                                .withDbLogSpec("measured-temp")
                                .withSeriesType(TEMPERATURE)
                                .withShowRegression(true)
                                .build(),
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.humidity, context).withFileLogSpec("6:H\\x3a:0:")
                                .withDbLogSpec("humidity")
                                .withSeriesType(HUMIDITY)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                                .build()
                ), humidity, measuredTemp);

                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureActuatorGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.temperature, context)
                                .withFileLogSpec("4:measured-temp:0")
                                .withDbLogSpec("measured-temp")
                                .withSeriesType(TEMPERATURE)
                                .withShowRegression(true)
                                .build(),
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.actuator, context).withFileLogSpec("4:actuator")
                                .withDbLogSpec("actuator::int")
                                .withSeriesType(ACTUATOR)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("actuator", 0, 100))
                                .build()
                ), measuredTemp, actuator);

                break;

            case POWERMETER:
                addDeviceChartIfNotNull(new DeviceChart(R.string.usageGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.currentUsage, context).withFileLogSpec("4:current:0")
                                .withDbLogSpec("current")
                                .withSeriesType(CURRENT_USAGE_WATT)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("current", 0, 0))
                                .build()
                ), power);

                addDeviceChartIfNotNull(new DeviceChart(R.string.usageGraphCumulative,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.cumulativeUsage, context).withFileLogSpec("4:energy")
                                .withDbLogSpec("energy")
                                .withSeriesType(CUMULATIVE_USAGE_Wh)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("energy", 0, 0))
                                .build()
                ), cumulativeUsage);
                break;

            case THPL:
                addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureHumidityGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.temperature, context)
                                .withFileLogSpec("4:temperature:0")
                                .withDbLogSpec("temperature")
                                .withSeriesType(TEMPERATURE)
                                .withShowRegression(true)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("temperature", 0, 0))
                                .build(),
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.humidity, context).withFileLogSpec("4:humidity:0")
                                .withDbLogSpec("humidity")
                                .withSeriesType(HUMIDITY)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                                .build()
                ), humidity, measuredTemp);

                addDeviceChartIfNotNull(new DeviceChart(R.string.pressureGraph,
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.pressure, context)
                                .withFileLogSpec("4:pressure:0")
                                .withDbLogSpec("pressure")
                                .withSeriesType(PRESSURE)
                                .withShowRegression(true)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("pressure", 0, 0))
                                .build(),
                        new ChartSeriesDescription.Builder()
                                .withColumnName(R.string.pressureNN, context).withFileLogSpec("4:pressure-nn:0")
                                .withDbLogSpec("pressure-nn")
                                .withSeriesType(HUMIDITY)
                                .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("pressure-nn", 0, 0))
                                .build()
                ), pressure, pressureNN);

                addDeviceChartIfNotNull(
                        new DeviceChart(R.string.brightnessGraph,
                                new ChartSeriesDescription.Builder()
                                        .withColumnName(R.string.brightness, context).withFileLogSpec("4:luminosity:0")
                                        .withDbLogSpec("luminosity")
                                        .withSeriesType(BRIGHTNESS)
                                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("luminosity", 0, 0))
                                        .build()
                        ), luminosity
                );

                break;
        }
    }

    public boolean isLastCommandAccepted() {
        return Strings.isNullOrEmpty(commandAccepted) || "yes".equalsIgnoreCase(commandAccepted);
    }

    @Override
    public boolean supportsWidget(Class<? extends DeviceAppWidgetView> appWidgetClass) {
        return !(appWidgetClass.equals(TemperatureWidgetView.class) &&
                !(subType == SubType.TH || subType == THERMOSTAT)) &&
                super.supportsWidget(appWidgetClass);
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

    public String getSubTypeRaw() {
        return subTypeRaw;
    }

    public String getCommandAccepted() {
        return commandAccepted;
    }

    @XmllistAttribute("COMMANDACCEPTED")
    public void setCommandAccepted(String value) {
        this.commandAccepted = value;
    }

    public enum SubType {
        DIMMER(DeviceFunctionality.DIMMER),
        SWITCH(DeviceFunctionality.SWITCH),
        SMOKE_DETECTOR(DeviceFunctionality.SMOKE_DETECTOR),
        THREE_STATE(DeviceFunctionality.WINDOW),
        TH(DeviceFunctionality.TEMPERATURE),
        THPL(DeviceFunctionality.TEMPERATURE),
        THERMOSTAT(DeviceFunctionality.HEATING),
        FILL_STATE(DeviceFunctionality.FILL_STATE),
        MOTION(DeviceFunctionality.MOTION_DETECTOR),
        KEYMATIC(DeviceFunctionality.KEY),
        POWERMETER(DeviceFunctionality.SWITCH),
        SHUTTER(DeviceFunctionality.WINDOW);

        private final DeviceFunctionality functionality;

        SubType(DeviceFunctionality functionality) {
            this.functionality = functionality;
        }
    }

    public enum HeatingMode {
        MANUAL, AUTO, CENTRAL, BOOST, UNKNOWN
    }
}
