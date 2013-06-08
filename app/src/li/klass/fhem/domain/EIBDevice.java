package li.klass.fhem.domain;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.domain.genericview.FloorplanViewSettings;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.util.ArrayUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

import java.util.List;

import static li.klass.fhem.service.graph.description.SeriesType.TEMPERATURE;

@DetailOverviewViewSettings(showState = true)
@FloorplanViewSettings
@SuppressWarnings("unused")
public class EIBDevice extends DimmableDevice<EIBDevice> {

    private String model;

    @Override
    public boolean isOnByState() {
        if (super.isOnByState()) return true;

        String internalState = getInternalState();
        return internalState.equalsIgnoreCase("on") || internalState.equalsIgnoreCase("on-for-timer") ||
                internalState.equalsIgnoreCase("on-till");
    }

    public void readMODEL(String value) {
        if (value.equals("dpt10")) value = "time";
        model = value;
    }

    @Override
    public void afterXMLRead() {
        super.afterXMLRead();

        if (model == null || model.equals("time") || getInternalState().equalsIgnoreCase("???")) return;

        double value = ValueExtractUtil.extractLeadingDouble(getInternalState());
        String description = "";
        if (model.equalsIgnoreCase("speedsensor")) {
            description = ValueDescriptionUtil.M_S;
        } else if (model.equalsIgnoreCase("tempsensor")) {
            description = ValueDescriptionUtil.C;
        } else if (model.equalsIgnoreCase("brightness") || model.equalsIgnoreCase("lightsensor")) {
            description = ValueDescriptionUtil.LUX;
        } else if (model.equals("percent")) {
            description = ValueDescriptionUtil.PERCENT;
            setState(ValueDescriptionUtil.append(ValueExtractUtil.extractLeadingInt(getInternalState()), description));
            return;
        }

        setState(ValueDescriptionUtil.append(value, description));
    }

    public String getModel() {
        return model;
    }

    @Override
    public boolean supportsToggle() {
        if (model != null && model.equalsIgnoreCase("time")) return false;
        return ArrayUtil.contains(getAvailableTargetStates(), "on", "off");
    }

    @Override
    public int getDimUpperBound() {
        return 100;
    }

    @Override
    public String getDimStateForPosition(int position) {
        return position + "";
    }

    @Override
    public int getPositionForDimState(String dimState) {
        return ValueExtractUtil.extractLeadingInt(dimState);
    }

    @Override
    public boolean supportsDim() {
        return model != null && model.equals("percent");
    }

    @Override
    public String formatStateTextToSet(String stateToSet) {
        if (model != null && model.equals("percent")) {
            return ValueDescriptionUtil.appendPercent(stateToSet);
        }
        return super.formatStateTextToSet(stateToSet);
    }

    @Override
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        super.fillDeviceCharts(chartSeries);

        if (model != null && model.equals("tempsensor")) {
            addDeviceChartIfNotNull(
                    new DeviceChart(R.string.temperatureGraph,
                            ChartSeriesDescription.getRegressionValuesInstance(R.string.temperature, "3:", TEMPERATURE)
                    ), getInternalState()
            );
        }
    }
}
