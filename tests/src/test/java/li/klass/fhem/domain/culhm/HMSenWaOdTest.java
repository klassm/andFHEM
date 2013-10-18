package li.klass.fhem.domain.culhm;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import org.junit.Test;

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

        assertThat(device.getState(), is("108 (%)"));
        assertThat(device.getSubType(), is(CULHMDevice.SubType.FILL_STATE));
        assertThat(device.getFillContentPercentageRaw(), is(closeTo(1.08, 0.001)));
    }

    @Override
    protected String getFileName() {
        return "HM-Sen-Wa-Od.xml";
    }
}
