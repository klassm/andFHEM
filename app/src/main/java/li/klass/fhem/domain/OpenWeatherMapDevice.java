package li.klass.fhem.domain;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine1;
import li.klass.fhem.appwidget.annotation.WidgetMediumLine2;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureAdditionalField;
import li.klass.fhem.appwidget.annotation.WidgetTemperatureField;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;

@SuppressWarnings("unused")
@SupportsWidget({TemperatureWidgetView.class, MediumInformationWidgetView.class})
public class OpenWeatherMapDevice extends Device<OpenWeatherMapDevice> {
    @ShowField(description = ResourceIdMapper.temperature, showInOverview = true)
    @WidgetTemperatureField
    @WidgetMediumLine1
    private String temperature;
    @ShowField(description = ResourceIdMapper.humidity, showInOverview = true)
    @WidgetMediumLine2
    @WidgetTemperatureAdditionalField
    private String humidity;
    @ShowField(description = ResourceIdMapper.windDirection, showInOverview = true)
    private String windDirection;
    @ShowField(description = ResourceIdMapper.windSpeed, showInOverview = true)
    private String windSpeed;
    @ShowField(description = ResourceIdMapper.temperatureMinimum)
    private String temperatureMinimum;
    @ShowField(description = ResourceIdMapper.temperatureMaximum)
    private String temperatureMaximum;
    @ShowField(description = ResourceIdMapper.sunrise)
    private String sunrise;
    @ShowField(description = ResourceIdMapper.sunset)
    private String sunset;

    public void readC_TEMPERATURE(String value) {
        temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readC_HUMIDITY(String value) {
        humidity = ValueDescriptionUtil.appendPercent(value);
    }

    public void readC_WINDDIR(String value) {
        windDirection = ValueDescriptionUtil.append(value, "°");
    }

    public void readC_WINDSPEED(String value) {
        windSpeed = ValueDescriptionUtil.appendKmH(value);
    }

    public void readC_TEMPMAX(String value) {
        temperatureMaximum = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readC_TEMPMIN(String value) {
        temperatureMinimum = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readC_SUNRISE(String value) {
        sunrise = value.replace("T", " ");
    }

    public void readC_SUNSET(String value) {
        sunset = value.replace("T", " ");
    }

    public String getTemperature() {
        return temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getTemperatureMinimum() {
        return temperatureMinimum;
    }

    public String getTemperatureMaximum() {
        return temperatureMaximum;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.WEATHER;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }

    @Override
    public long getTimeRequiredForStateError() {
        return OUTDATED_DATA_MS_DEFAULT;
    }
}
