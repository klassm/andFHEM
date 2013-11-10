package li.klass.fhem.fhem;

import org.junit.Test;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import li.klass.fhem.domain.core.RoomDeviceList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

public class DummyDataXmlLoadTest extends DeviceXMLParsingBase {
    @Test
    public void testFunctionalityIsSetOnAllDevices() {
        for (String room : roomDeviceListMap.keySet()) {
            RoomDeviceList roomDeviceList = roomDeviceListMap.get(room);

            for (Device device : roomDeviceList.getAllDevices()) {
                assertThat(device.getDeviceFunctionality(), is(not(nullValue())));
            }
        }
    }

    @Override
    protected String getFileName() {
        return "dummyData.xml";
    }

    @Override
    protected Class<?> getTestFileBaseClass() {
        return DummyDataConnection.class;
    }
}
