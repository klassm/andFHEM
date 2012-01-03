package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class FileLog extends Device<FileLog> {

    private String concerningDeviceName;

    @Override
    public void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("REGEXP")) {
            this.concerningDeviceName = extractConcerningDeviceNameFromDefinition(nodeContent);
        }
    }

    private String extractConcerningDeviceNameFromDefinition(String definition) {
        int firstColonPosition = definition.indexOf(":");
        if (firstColonPosition != -1) {
            return definition.substring(0, firstColonPosition);
        }

        return definition;
    }

    public String getConcerningDeviceName() {
        return concerningDeviceName;
    }
}
