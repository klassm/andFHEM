package li.klass.fhem.service.device;

import android.content.Context;
import li.klass.fhem.domain.SISPMSDevice;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.ExecuteOnSuccess;

public class SISPMSService {
    public static final SISPMSService INSTANCE = new SISPMSService();

    private SISPMSService() {
    }

    public void toggleState(Context context, SISPMSDevice device, ExecuteOnSuccess executeOnSuccess) {
        if (device.isOn()) {
            CommandExecutionService.INSTANCE.executeSafely(context, "set " + device.getName() + " off", executeOnSuccess);
            device.setState("off");
        } else {
            CommandExecutionService.INSTANCE.executeSafely(context, "set " + device.getName() + " on", executeOnSuccess);
            device.setState("on");
        }
    }
}
