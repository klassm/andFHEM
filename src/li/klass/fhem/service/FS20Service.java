package li.klass.fhem.service;

import android.content.Context;
import li.klass.fhem.domain.FS20Device;

public class FS20Service {
    public static final FS20Service INSTANCE = new FS20Service();

    private FS20Service() {
    }
    
    public void setState(Context context, FS20Device device, String newState, ExecuteOnSuccess executeOnSuccess) {
        FHEMService.INSTANCE.executeSafely(context, "set " + device.getName() + " " + newState, executeOnSuccess);
        device.setState(newState);
    }

    public void toggleState(Context context, FS20Device fs20Device, ExecuteOnSuccess executeOnSuccess) {
        if (fs20Device.isOn()) {
            setState(context, fs20Device, "off", executeOnSuccess);
        } else {
            setState(context, fs20Device, "on", executeOnSuccess);
        }
    }

    public void dim(Context context, FS20Device fs20Device, int dimProgress, ExecuteOnSuccess executeOnSuccess) {
        if (! fs20Device.isDimDevice()) return;
        int bestMatch = fs20Device.getBestDimMatchFor(dimProgress);

        String newState;
        if (bestMatch == 0)
            newState = "off";
        else {
            newState = "dim" + String.format("%02d", bestMatch) + "%";
        }

        setState(context, fs20Device, newState, executeOnSuccess);
    }
}
