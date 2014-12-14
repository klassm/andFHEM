package li.klass.fhem.domain.culhm;

import org.hamcrest.Matchers;
import org.junit.Test;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.number.IsCloseTo.closeTo;

public class HMSenWaOdTest extends DeviceXMLParsingBase {

    @Test
    public void testDeviceAttributesRead() {
        CULHMDevice device = getDefaultDevice();
        assertThat(device, is(not(nullValue())));

        assertThat(device.getState(), is("24 (%)"));
        assertThat(device.getSubType(), is(CULHMDevice.SubType.FILL_STATE));
        assertThat(device.getFillContentPercentageRaw(), is(closeTo(0.24, 0.001)));

        assertThat(device.isSupported(), Matchers.is(true));
    }

    @Override
    protected String getFileName() {
        return "HM-Sen-Wa-Od.xml";
    }
}
