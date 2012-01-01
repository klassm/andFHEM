package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class SISPMSDevice extends Device<SISPMSDevice> {
    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (nodeContent.equals("STATE")) {
            this.measured = attributes.getNamedItem("measured").getTextContent();
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SIS_PMS;
    }

    public boolean isOn() {
        return state.equalsIgnoreCase("on");
    }
}
