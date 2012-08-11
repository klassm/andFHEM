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
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.AppWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

import java.util.List;

@SuppressWarnings("unused")
@DetailOverviewViewSettings(showState = true)
@FloorplanViewSettings(showState = true)
@SupportsWidget(TemperatureWidgetView.class)
public class CULHMDevice extends ToggleableDevice<CULHMDevice> {

    public enum SubType {
        DIMMER, SWITCH, HEATING, SMOKE_DETECTOR, THREE_STATE, TEMPERATURE_HUMIDITY, KFM100
    }

    private SubType subType = null;
    private int dimProgress = -1;

    @ShowField(description = R.string.measured)
    private String measured;
    @ShowField(description = R.string.desiredTemperature)
    private String desiredTemp;
    @ShowField(description = R.string.temperature, showInOverview = true)
    @WidgetTemperatureField
    private String measuredTemp;
    @ShowField(description = R.string.actuator, showInOverview = true)
    private String actuator;
    @ShowField(description = R.string.humidity, showInOverview = true)
    private String humidity;
    @ShowField(description = R.string.model)
    private String subTypeRaw;
    @ShowField(description = R.string.commandAccepted)
    private String commandAccepted;
    @ShowField(description = R.string.rawValue)
    private String rawValue;
    private double fillContentLitresRaw;
    @ShowField(description = R.string.maximumContent)
    private Integer fillContentLitresMaximum;
    private double fillContentPercentageRaw;
    @ShowField(description = R.string.fillPercentage, showInOverview = true, showInDetail = false)
    private String fillContentPercentage;
    @ShowField(description = R.string.conversion)
    private String rawToReadable;

    public void readRAWTOREADABLE(String value) {
        this.rawToReadable = value;

        int lastSpace = value.lastIndexOf(" ");
        String lastDefinition = lastSpace == -1 ? value : value.substring(lastSpace + 1);
        String[] parts = lastDefinition.split(":");
        if (parts.length != 2) return;

        int rawValue = Integer.parseInt(parts[0]);
        int realValue = Integer.parseInt(parts[1]);

        fillContentLitresMaximum = realValue / rawValue * 255;
    }

    public void readRAWVALUE(String value) {
        this.rawValue = value;
    }

    public void readCONTENT(String value) {
        fillContentLitresRaw = ValueExtractUtil.extractLeadingDouble(value.replace("l", ""));
        String fillContentLitres = ValueDescriptionUtil.appendL(fillContentLitresRaw);
        setState(fillContentLitres);
    }

    public void readCOMMANDACCEPTED(String value) {
        this.commandAccepted = value;
    }

    public void readHumidity(String value) {
        humidity = ValueDescriptionUtil.appendPercent(value);
    }

    public void readACTUATOR(String value) {
        subType = SubType.HEATING;
        actuator = value;
    }

    @Override
    public void readSTATE(String value) {
        super.readSTATE(value);
        if (value.endsWith("%")) {
            dimProgress = ValueExtractUtil.extractLeadingInt(value);
        }
    }

    public void readTEMPERATURE(String value) {
        this.measuredTemp = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readMEASURED_TEMP(String value) {
        measuredTemp = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readDESIRED_TEMP(String value) {
        desiredTemp = ValueDescriptionUtil.appendTemperature(value);
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
            subType = SubType.KFM100;
        }
        subTypeRaw = value;
    }

    @Override
    public void afterXMLRead() {
        super.afterXMLRead();

        if (subType == SubType.KFM100) {
            fillContentPercentageRaw = fillContentLitresRaw == 0 ? 0 : fillContentLitresRaw / fillContentLitresMaximum;
            fillContentPercentage = ValueDescriptionUtil.appendPercent((int) (fillContentPercentageRaw * 100));
        }
    }

    @Override
    public boolean isSupported() {
        return subType != null;
    }

    public boolean isOn() {
        String internalState = getInternalState();
        return internalState.equalsIgnoreCase("on") || internalState.equalsIgnoreCase("on-for-timer");
    }

    @Override
    public boolean supportsToggle() {
        return subType == SubType.SWITCH;
    }

    public int getDimProgress() {
        if (dimProgress == -1) {
            return isOn() ? 100 : 0;
        }
        return dimProgress;
    }

    public void setDimProgress(int dimProgress) {
        this.dimProgress = dimProgress;
    }

    public String getMeasured() {
        return measured;
    }

    public String getDesiredTemp() {
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

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        if (subType == null) return;

        switch (subType) {
            case TEMPERATURE_HUMIDITY:

                addDeviceChartIfNotNull(measuredTemp, new DeviceChart(R.string.temperatureGraph, R.string.yAxisTemperature,
                        ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:")));
                addDeviceChartIfNotNull(humidity, new DeviceChart(R.string.humidityGraph, R.string.yAxisHumidity,
                        new ChartSeriesDescription(R.string.humidity, "6:")));

                break;

            case KFM100:
                addDeviceChartIfNotNull(getState(), new DeviceChart(R.string.contentGraph, R.string.yAxisLitreContent,
                        ChartSeriesDescription.getRegressionValuesInstance(R.string.content, "4:content:0:"),
                        new ChartSeriesDescription(R.string.rawValue, "4:rawValue:0:")));

                break;
        }
    }

    @Override
    public boolean supportsWidget(Class<? extends AppWidgetView> appWidgetClass) {
        if (appWidgetClass.equals(TemperatureWidgetView.class) &&
                ! (subType == SubType.TEMPERATURE_HUMIDITY || subType == SubType.HEATING)) {
            return false;
        }

        return super.supportsWidget(appWidgetClass);
    }

    @Override
    public int compareTo(CULHMDevice other) {
        int result = subType.compareTo(other.getSubType());
        if (result != 0) return result;

        return name.compareTo(other.getName());
    }
}
