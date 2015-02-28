package li.klass.fhem.fhem;

import org.assertj.core.util.Files;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import li.klass.fhem.domain.core.FhemDevice;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyDataXmlLoadTest extends DeviceXMLParsingBase {
    @Test
    public void testFunctionalityIsSetOnAllDevices() {
        for (FhemDevice device : roomDeviceList.getAllDevices()) {
            assertThat(device.getDeviceGroup()).isNotNull();
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
