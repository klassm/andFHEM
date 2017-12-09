package li.klass.fhem.domain;

import org.junit.Test;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;

public class DMXDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void device_is_properly_read() {
        GenericDevice device = getDeviceFor("LedBett", GenericDevice.class);
        assertThat(device).isNotNull();

        assertThat(device.getXmlListDevice().getState("rgb", true)).contains("ffffff");
        assertThat(device.getXmlListDevice().getState("pct", true)).contains("10");
    }

    @Override
    protected String getFileName() {
        return "dmx.xml";
    }
}
