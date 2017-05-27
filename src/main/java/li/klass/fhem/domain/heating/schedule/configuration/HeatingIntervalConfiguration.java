package li.klass.fhem.domain.heating.schedule.configuration;

import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval;

public interface HeatingIntervalConfiguration<INTERVAL extends BaseHeatingInterval<INTERVAL>> {
    INTERVAL createHeatingInterval();

    int getMaximumNumberOfHeatingIntervals();

    HeatingConfiguration.NumberOfIntervalsType getNumberOfIntervalsType();
}
