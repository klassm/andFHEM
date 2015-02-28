package li.klass.fhem.domain.culhm;

import org.junit.Test;

import li.klass.fhem.domain.CULHMDevice;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static li.klass.fhem.domain.CULHMDevice.SubType.FILL_STATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class HMSenWaOdTest extends DeviceXMLParsingBase {

    @Test
    public void testDeviceAttributesRead() {
        CULHMDevice device = getDefaultDevice(CULHMDevice.class);
        assertThat(device).isNotNull();

        assertThat(device.getState()).isEqualTo("24 (%)");
        assertThat(device.getSubType()).isEqualTo(FILL_STATE);
        assertThat(device.getFillContentPercentageRaw()).isCloseTo(0.24, offset(0.001));

        assertThat(device.isSupported()).isTrue();
    }

    @Override
    protected String getFileName() {
        return "HM-Sen-Wa-Od.xml";
    }
}
