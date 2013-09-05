package li.klass.fhem.fhem;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;
import org.junit.Test;

public class TestXmlLoadTest extends DeviceXMLParsingBase {
    @Test
    public void test() {
        // no exception should happen ...
    }

    @Override
    protected String getFileName() {
        return "test.xml";
    }
}
