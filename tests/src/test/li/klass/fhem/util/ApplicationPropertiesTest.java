package li.klass.fhem.util;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class ApplicationPropertiesTest {

    @Test
    public void testLoadFile() throws Exception {
        InputStream stream = ApplicationProperties.class.getResource("applicationTest.properties").openStream();
        ApplicationProperties.INSTANCE.load(stream);

        String value = ApplicationProperties.INSTANCE.getStringApplicationProperty("value");
        assertEquals("myTestValue", value);
    }
}
