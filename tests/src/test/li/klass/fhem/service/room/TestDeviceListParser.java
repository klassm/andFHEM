package li.klass.fhem.service.room;

import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.fhem.DummyDataConnection;
import li.klass.fhem.infra.AndFHEMRobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

@RunWith(AndFHEMRobolectricTestRunner.class)
public class TestDeviceListParser {

    @Test
    public void testParseDummyData() throws Exception {
        String xmlList = DummyDataConnection.INSTANCE.xmllist();
        assertNotNull(xmlList);

        DeviceListParser deviceListParser = DeviceListParser.INSTANCE;
        Map<String,RoomDeviceList> result = deviceListParser.parseXMLList(xmlList);

        assertNotNull(result);
    }
}
