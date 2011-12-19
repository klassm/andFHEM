package li.klass.fhem.data;

import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;
import org.w3c.dom.NamedNodeMap;

public class FileLog extends Device<FileLog> {

    private String concerningDevice;

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("REGEXP")) {
            this.concerningDevice = extractConcerningDeviceNameFromDefinition(nodeContent);
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.FILE_LOG;
    }

    private String extractConcerningDeviceNameFromDefinition(String definition) {
        int firstColonPosition = definition.indexOf(":");
        if (firstColonPosition != -1) {
            return definition.substring(0, firstColonPosition);
        }

        return definition;
    }

    public String getConcerningDevice() {
        return concerningDevice;
    }
}
