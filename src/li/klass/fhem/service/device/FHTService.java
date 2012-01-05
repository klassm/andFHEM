package li.klass.fhem.service.device;

import android.content.Context;
import li.klass.fhem.activities.CurrentActivityProvider;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.ExecuteOnSuccess;

public class FHTService {
    public static final FHTService INSTANCE = new FHTService();

    private FHTService() {
    }

    public void setDesiredTemperature(Context context, final FHTDevice device, final double value) {
        String command = "set " + device.getName() + " desired-temp " + value;
        CommandExecutionService.INSTANCE.executeSafely(context, command, new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {
                device.setDesiredTemp(value);
                CurrentActivityProvider.INSTANCE.getCurrentActivity().update(false);
            }
        });
    }

    public void setMode(Context context, final FHTDevice device, final FHTMode mode) {
        if (device.getMode() != mode) {
            String command = "set " + device.getName() + " mode " + mode.name().toLowerCase();
            CommandExecutionService.INSTANCE.executeSafely(context, command, new ExecuteOnSuccess() {
                @Override
                public void onSuccess() {
                    device.setMode(mode);
                }
            });
        }
    }
}
