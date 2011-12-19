package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class CULFHTTKDevice extends Device<CULFHTTKDevice> {

    private String measured;

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("CUL_TIME")) {
            measured = nodeContent;
        }
    }

    public String getMeasured() {
        return measured;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.CUL_FHTTK;
    }
}
