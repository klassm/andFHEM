package li.klass.fhem.domain;

import li.klass.fhem.appwidget.annotation.*;
import li.klass.fhem.appwidget.view.widget.medium.MediumInformationWidgetView;
import li.klass.fhem.appwidget.view.widget.medium.TemperatureWidgetView;
import li.klass.fhem.domain.core.Device;
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

    public void readG_TEMPERATURE(String value) {
        temperature = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readG_HUMIDITY(String value) {
        humidity = ValueDescriptionUtil.appendPercent(value);
    }

    public void readG_WINDDIR(String value) {
        windDirection = ValueDescriptionUtil.append(value, "°");
    }

    public void readG_WINDSPEED(String value) {
        windSpeed = ValueDescriptionUtil.appendKmH(value);
    }

    public void readG_TEMPMAX(String value) {
        temperatureMaximum = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readG_TEMPMIN(String value) {
        temperatureMinimum = ValueDescriptionUtil.appendTemperature(value);
    }

    public void readG_SUNRISE(String value) {
        sunrise = value.replace("T", " ");
    }

    public void readG_SUNSET(String value) {
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
}
