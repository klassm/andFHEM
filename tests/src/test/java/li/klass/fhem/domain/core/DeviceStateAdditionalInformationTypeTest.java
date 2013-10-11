package li.klass.fhem.domain.core;

import org.junit.Test;

import static li.klass.fhem.domain.core.DeviceStateAdditionalInformationType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DeviceStateAdditionalInformationTypeTest {
    @Test
    public void testDecimalQuarterMatches() {
        assertThat(DEC_QUARTER.matches("2"), is(true));
        assertThat(DEC_QUARTER.matches("2.0"), is(true));
        assertThat(DEC_QUARTER.matches("2.25"), is(true));
        assertThat(DEC_QUARTER.matches("2.5"), is(true));
        assertThat(DEC_QUARTER.matches("2.50"), is(true));
        assertThat(DEC_QUARTER.matches("2.75"), is(true));
        assertThat(DEC_QUARTER.matches("3"), is(true));
        assertThat(DEC_QUARTER.matches("3.1"), is(false));
    }

    @Test
    public void testNumericMatches() {
        assertThat(NUMERIC.matches("2"), is(true));
        assertThat(NUMERIC.matches("5"), is(true));
        assertThat(NUMERIC.matches("10"), is(true));
        assertThat(NUMERIC.matches("100"), is(true));
        assertThat(NUMERIC.matches("10.25"), is(false));
        assertThat(NUMERIC.matches("abc"), is(false));
    }

    @Test
    public void testAnyMatches() {
        assertThat(ANY.matches("2"), is(true));
        assertThat(ANY.matches("abc"), is(true));
        assertThat(ANY.matches("2.6"), is(true));
    }

    @Test
    public void testTimeMatches() {
        assertThat(TIME.matches("24:00"), is(true));
        assertThat(TIME.matches("23:24"), is(true));
        assertThat(TIME.matches("23.24"), is(false));
    }

    @Test
    public void testTimeWithSecondMatches() {
        assertThat(TIME_WITH_SECOND.matches("24:00:00"), is(true));
        assertThat(TIME_WITH_SECOND.matches("23:24:10"), is(true));
        assertThat(TIME_WITH_SECOND.matches("23.24"), is(false));
    }

    @Test
    public void testTemperatureMatches() {
        assertThat(TEMPERATURE.matches("24"), is(true));
        assertThat(TEMPERATURE.matches("24.25"), is(true));
        assertThat(TEMPERATURE.matches("23:24"), is(false));
    }
}
