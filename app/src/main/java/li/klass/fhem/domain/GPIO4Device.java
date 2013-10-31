package li.klass.fhem.domain;

import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ValueDescriptionUtil;

import java.util.List;

import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;

@SuppressWarnings("unused")
public class GPIO4Device extends Device<GPIO4Device> {

    private SubType subType = null;

    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    private String temperature;

    private enum SubType {
        TEMPERATURE
    }

    public void readMODEL(String value) {
        if (value.equals("DS1820")) {
            subType = SubType.TEMPERATURE;
        }
    }

    public void readTEMPERATURE(String value) {
        temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public String getTemperature() {
        return temperature;
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        if (subType == SubType.TEMPERATURE) {
            addDeviceChartIfNotNull(new DeviceChart(R.string.temperatureGraph,
                    ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "4:T", TEMPERATURE)), temperature);
        }
    }

    @Override
    public boolean isSupported() {
        return subType != null;
    }
}
