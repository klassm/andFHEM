package li.klass.fhem.domain;

import org.junit.Test;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.fest.assertions.api.Assertions.assertThat;

public class WithingsDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void body_device_is_read_correctly() {
        WithingsDevice device = getDeviceFor("body");
        assertThat(device).isNotNull();

        assertThat(device.getName()).isEqualTo("body");
        assertThat(device.getSubType()).isEqualTo(WithingsDevice.SubType.USER);

        assertThat(device.getFatFreeMass()).isEqualTo("68.0 (kg)");
        assertThat(device.getFatMassWeight()).isEqualTo("17.0 (kg)");
        assertThat(device.getFatRatio()).isEqualTo("20.0");
        assertThat(device.getHeartPulse()).isEqualTo("70");
        assertThat(device.getWeight()).isEqualTo("85.0 (kg)");
        assertThat(device.getHeight()).isEqualTo("1.9 (m)");

        assertThat(device.getMeasured()).isEqualTo("2014-07-13 13:18:55");
    }

    @Test
    public void scale_device_is_read_correctly() {
        WithingsDevice device = getDeviceFor("scale");
        assertThat(device).isNotNull();

        assertThat(device.getName()).isEqualTo("scale");
        assertThat(device.getSubType()).isEqualTo(WithingsDevice.SubType.DEVICE);

        assertThat(device.getBatteryLevel()).isEqualTo("91 (%)");
        assertThat(device.getCo2()).isEqualTo("967 (ppm)");
        assertThat(device.getTemperature()).isEqualTo("23.6 (°C)");

        assertThat(device.getMeasured()).isEqualTo("2014-07-13 16:23:13");
    }

    @Test
    public void withings_account_device_is_ignored() {
        WithingsDevice device = getDeviceFor("withings");
        assertThat(device).isNull();
    }

    @Override
    protected String getFileName() {
        return "withings.xml";
    }
}
