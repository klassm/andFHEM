package li.klass.fhem.adapter.devices.genericui;

import org.junit.Test;

import static li.klass.fhem.adapter.devices.genericui.DimActionRowFullWidth.dimProgressToDimState;
import static li.klass.fhem.adapter.devices.genericui.DimActionRowFullWidth.toDimProgress;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DimActionRowFullWidthTest {

    @Test
    public void testOneToHundredDimProgressConversion() {
        assertThat(toDimProgress(50, 0, 1), is(50));
        assertThat(toDimProgress(100, 0, 1), is(100));
        assertThat(toDimProgress(0, 0, 1), is(0));

        assertThat(dimProgressToDimState(100, 0, 1), is(100));
        assertThat(dimProgressToDimState(50, 0, 1), is(50));
        assertThat(dimProgressToDimState(0, 0, 1), is(0));
    }

    @Test
    public void testShiftedLowerBoundConversion() {
        assertThat(toDimProgress(50, 20, 1), is(30));
        assertThat(dimProgressToDimState(30, 20, 1), is(50));
    }

    @Test
    public void testStepDifferentFrom1Conversion() {
        assertThat(toDimProgress(20, 0, 2), is(10));
        assertThat(dimProgressToDimState(10, 0, 2), is(20));

        assertThat(toDimProgress(20, 0, 3), is(6)); // lower rounding
        assertThat(dimProgressToDimState(6, 0, 3), is(18));
    }

    @Test
    public void testStepDifferentFrom1AndShiftedLowerBoundConversion() {
        assertThat(toDimProgress(20, 10, 2), is(5));
        assertThat(dimProgressToDimState(5, 10, 2), is(20));
    }
}
