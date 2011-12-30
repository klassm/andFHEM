package li.klass.fhem.service;

import android.content.Context;
import li.klass.fhem.domain.SISPMSDevice;

public class SISPMSService {
    public static final SISPMSService INSTANCE = new SISPMSService();

    private SISPMSService() {
    }

    public void toggleState(Context context, SISPMSDevice device, ExecuteOnSuccess executeOnSuccess) {
        if (device.isOn()) {
            FHEMService.INSTANCE.executeSafely(context, "set " + device.getName() + " off", executeOnSuccess);
            device.setState("off");
        } else {
            FHEMService.INSTANCE.executeSafely(context, "set " + device.getName() + " on", executeOnSuccess);
            device.setState("on");
        }
    }
}
