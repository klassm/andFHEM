package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class RFXX10RECDevice extends Device<RFXX10RECDevice> {
    
    private String measured;
    
    @Override
    protected void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TIME")) {
            measured = nodeContent;
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.RFXX10REC;
    }

    public String getMeasured() {
        return measured;
    }
}
