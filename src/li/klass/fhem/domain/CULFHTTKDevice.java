package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class CULFHTTKDevice extends Device<CULFHTTKDevice> {

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.CUL_FHTTK;
    }
}
