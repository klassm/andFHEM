package li.klass.fhem.domain;

import android.content.Context;
import li.klass.fhem.data.FHEMService;
import org.w3c.dom.NamedNodeMap;

public class SISPMSDevice extends Device<SISPMSDevice> {
    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SIS_PMS;
    }

    public boolean isOn() {
        return state.equalsIgnoreCase("on");
    }

    public void toggleState(Context context) {
        if (isOn()) {
            FHEMService.INSTANCE.executeSafely(context, "set " + getName() + " off");
            state = "off";
        } else {
            FHEMService.INSTANCE.executeSafely(context, "set " + getName() + " on");
            state = "on";
        }
    }
}
