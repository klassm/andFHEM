package li.klass.fhem.fhem;

import org.fest.util.Files;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

public class DummyDataXmlLoadTest extends DeviceXMLParsingBase {
    @Test
    public void testFunctionalityIsSetOnAllDevices() {
        for (Device device : roomDeviceList.getAllDevices()) {
            assertThat(device.getDeviceGroup(), is(not(nullValue())));
        }
    }

    @Test
    public void testCanSerialize() throws IOException {
        File file = Files.newTemporaryFile();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
        try {
            objectOutputStream.writeObject(roomDeviceList);
        } finally {
            objectOutputStream.close();
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
