package li.klass.fhem.domain;

import org.junit.Test;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import li.klass.fhem.util.NumberSystemUtil;

import static org.fest.assertions.api.Assertions.assertThat;

public class DMXDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void device_is_properly_read() {
        DMXDevice device = getDeviceFor("LedBett");
        assertThat(device).isNotNull();

        assertThat(device.getRgb()).isEqualTo("FFFFFF");
        assertThat(device.getRGBColor()).isEqualTo(NumberSystemUtil.hexToDecimal("FFFFFF"));
        assertThat(device.getPct()).isEqualToIgnoringCase("10");

        assertThat(device.supportsDim()).isTrue();
    }

    @Override
    protected String getFileName() {
        return "dmx.xml";
    }
}
