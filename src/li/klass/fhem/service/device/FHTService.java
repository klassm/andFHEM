package li.klass.fhem.service.device;

import android.content.Context;
import li.klass.fhem.activities.CurrentActivityProvider;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.domain.fht.FHTDayControl;
import li.klass.fhem.domain.fht.FHTMode;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.ExecuteOnSuccess;
import li.klass.fhem.util.DayUtil;

import java.util.ArrayList;
import java.util.List;

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

    public void setTimetableFor(Context context, final FHTDevice fhtDevice) {
        if (! fhtDevice.hasChangedDayControlMapValues()) {
            return;
        }

        List<String> changeParts = new ArrayList<String>();
        for (FHTDayControl fhtDayControl : fhtDevice.getDayControlMap().values()) {
            String shortDayName = DayUtil.getShortNameForStringId(fhtDayControl.getDayId());

            if (! fhtDayControl.getFrom1().equals(fhtDayControl.getFrom1Changed())) {
                changeParts.add(shortDayName + "-from1 " + fhtDayControl.getFrom1Changed());
            }
            if (! fhtDayControl.getFrom2().equals(fhtDayControl.getFrom2Changed())) {
                changeParts.add(shortDayName + "-from2 " + fhtDayControl.getFrom2Changed());
            }
            if (! fhtDayControl.getTo1().equals(fhtDayControl.getTo1Changed())) {
                changeParts.add(shortDayName + "-to1 " + fhtDayControl.getTo1Changed());
            }
            if (! fhtDayControl.getTo2().equals(fhtDayControl.getTo2Changed())) {
                changeParts.add(shortDayName + "-to2 " + fhtDayControl.getTo2Changed());
            }
        }

        for (String command : generateTimetableCommands(fhtDevice, changeParts)) {
            CommandExecutionService.INSTANCE.executeSafely(context, command, new ExecuteOnSuccess() {
                @Override
                public void onSuccess() {
                    fhtDevice.setChangedDayControlMapValuesAsCurrent();
                    CurrentActivityProvider.INSTANCE.getCurrentActivity().update(false);
                }
            });
        }
    }

    public List<String> generateTimetableCommands(FHTDevice device, List<String> commandParts) {
        List<String> commands = new ArrayList<String>();
        StringBuilder currentCommand = new StringBuilder();
        int currentCommandSize = 0;

        for (String commandPart : commandParts) {
            if (currentCommandSize >= 8) {
                commands.add("set " + device.getName() + " " + currentCommand.toString());
                currentCommand = new StringBuilder();
                currentCommandSize = 0;
            }
            currentCommand.append(commandPart).append(" ");

            currentCommandSize++;
        }

        if (currentCommand.length() > 0) {
            commands.add("set " + device.getName() + " " + currentCommand.toString());
        }

        return commands;
    }
}
