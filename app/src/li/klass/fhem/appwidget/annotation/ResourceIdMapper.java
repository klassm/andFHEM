package li.klass.fhem.appwidget.annotation;

import li.klass.fhem.R;

public enum ResourceIdMapper {
    currentUsage(R.string.currentUsage),
    dayUsage(R.string.dayUsage),
    monthUsage(R.string.monthUsage),
    state(R.string.state),
    lastStateChange(R.string.lastStateChange),
    measured(R.string.measured),
    desiredTemperature(R.string.desiredTemperature),
    temperature(R.string.temperature),
    actuator(R.string.actuator),
    humidity(R.string.humidity),
    model(R.string.model),
    commandAccepted(R.string.commandAccepted),
    rawValue(R.string.rawValue),
    maximumContent(R.string.maximumContent),
    fillPercentage(R.string.fillPercentage),
    conversion(R.string.conversion),
    battery(R.string.battery),
    dayTemperature(R.string.dayTemperature),
    nightTemperature(R.string.nightTemperature),
    windowOpenTemp(R.string.windowOpenTemp),
    ecoTemp(R.string.ecoTemperature),
    comfortTemp(R.string.comfortTemperature),
    warnings(R.string.warnings),
    wind(R.string.wind),
    rain(R.string.rain),
    avgDay(R.string.avgDay),
    avgMonth(R.string.avgMonth),
    isRaining(R.string.isRaining),
    power(R.string.power),
    audio(R.string.audio),
    input(R.string.input),
    forecast(R.string.forecast),
    dewpoint(R.string.dewpoint),
    pressure(R.string.pressure),
    rainRate( R.string.rainRate),
    rainTotal(R.string.rainTotal),
    windAvgSpeed(R.string.windAvgSpeed),
    windDirection(R.string.windDirection),
    windSpeed(R.string.windSpeed),
    uvValue(R.string.uvValue),
    uvRisk(R.string.uvRisk),
    counterA(R.string.counterA),
    counterB(R.string.counterB),
    present(R.string.present),
    delta(R.string.delta),
    lastState(R.string.lastState),
    currentSwitchDevice(R.string.currentSwitchDevice),
    currentSwitchTime(R.string.currentSwitchTime),
    lastSwitchTime(R.string.lastSwitchTime),
    nextSwitchTime(R.string.nextSwitchTime),
    type(R.string.type),
    windchill(R.string.windchill),
    twilight_next_event(R.string.twilight_next_event),
    twilight_next_event_time(R.string.twilight_next_event_time),
    twilight_sunrise(R.string.twilight_sunrise),
    twilight_sunrise_astronomical(R.string.twilight_sunrise_astronomical),
    twilight_sunrise_civil(R.string.twilight_sunrise_civil),
    twilight_sunrise_indoor(R.string.twilight_sunrise_indoor),
    twilight_sunrise_nautical(R.string.twilight_sunrise_nautical),
    twilight_sunrise_weather(R.string.twilight_sunrise_weather),
    twilight_sunset(R.string.twilight_sunset),
    twilight_sunset_astronomical(R.string.twilight_sunset_astronomical),
    twilight_sunset_civil(R.string.twilight_sunset_civil),
    twilight_sunset_indoor(R.string.twilight_sunset_indoor),
    twilight_sunset_nautical(R.string.twilight_sunset_nautical),
    twilight_sunset_weather(R.string.twilight_sunset_weather),
    twilight_light(R.string.twilight_light),
    ip(R.string.ip),
    mac(R.string.mac),
    definition(R.string.definition),
    none(-1);

    private int id;

    private ResourceIdMapper(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
