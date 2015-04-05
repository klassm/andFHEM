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

import java.util.List;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine3;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.util.ValueUtil;

import static li.klass.fhem.service.graph.description.SeriesType.HUMIDITY;
import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;

@SupportsWidget({TemperatureWidgetView.class, MediumInformationWidgetView.class})
public class HMSDevice extends FhemDevice<HMSDevice> {
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    @WidgetMediumLine1
    private String temperature;

    @ShowField(description = ResourceIdMapper.battery)
    @WidgetMediumLine3
    @XmllistAttribute("battery")
    private String battery;

    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    @WidgetMediumLine2
    @WidgetTemperatureAdditionalField
    private String humidity;

    @ShowField(description = ResourceIdMapper.model)
    @XmllistAttribute("type")
    private String model;

    @ShowField(description = ResourceIdMapper.state, showInOverview = true)
    private String switchDetect;

    @ShowField(description = ResourceIdMapper.waterDetect, showInOverview = true)
    private String waterDetect;

    @XmllistAttribute("temperature")
    public void setTemperature(String value) {
        temperature = ValueUtil.formatTemperature(value);
    }

    @XmllistAttribute("humidity")
    public void setHumidity(String value) {
        this.humidity = ValueUtil.formatHumidity(value);
    }

    @XmllistAttribute("switch_detect")
    public void setSwitchDetect(String value) {
        Context context = AndFHEMApplication.getContext();
        this.switchDetect = value.equalsIgnoreCase("ON")
                ? context.getString(R.string.on) : context.getString(R.string.off);
    }

    @XmllistAttribute("water_detect")
    public void setWaterDetect(String value) {
        waterDetect = value.equalsIgnoreCase("ON") ? "yes" : "no";
    }

    @Override
    public void onChildItemRead(DeviceNode.DeviceNodeType type, String key, String value, DeviceNode node) {
        super.onChildItemRead(type, key, value, node);

        if ("temperature".equalsIgnoreCase(key)) {
            setMeasured(node.getMeasured());
        }
    }

    public String getTemperature() {
        return temperature;
    }

    public String getBattery() {
        return battery;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getModel() {
        return model;
    }

    public String getSwitchDetect() {
        return switchDetect;
    }

    public String getWaterDetect() {
        return waterDetect;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries, Context context) {
        super.fillDeviceCharts(chartSeries, context);

        addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature, context)
                        .withFileLogSpec("4:T\\x3a:0:")
                        .withDbLogSpec("temperature::int1")
                        .withSeriesType(TEMPERATURE)
                        .withShowRegression(true)
                        .build()
        ), temperature);

        addDeviceChartIfNotNull(new DeviceChart(R.string.humidityGraph,
                new ChartSeriesDescription.Builder()
                        .withColumnName(R.string.temperature, context).withFileLogSpec("6:H\\x3a:0:")
                        .withDbLogSpec("humidity::int")
                        .withSeriesType(HUMIDITY)
                        .withYAxisMinMaxValue(getLogDevices().get(0).getYAxisMinMaxValueFor("humidity", 0, 100))
                        .build()
        ), humidity);
    }

    @Override
    public boolean supportsWidget(Class<? extends DeviceAppWidgetView> appWidgetClass) {
        return !(appWidgetClass.equals(TemperatureWidgetView.class) && model.equals("HMS100TFK"));
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.TEMPERATURE;
    }

    @Override
    protected boolean useTimeAndWeekAttributesForMeasureTime() {
        return false;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }

    @Override
    public long getTimeRequiredForStateError() {
        return 5 * 60 * 60 * 1000;
    }
}
