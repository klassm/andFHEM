package li.klass.fhem.domain;

import li.klass.fhem.data.FHEMService;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class FS20Device extends Device<FS20Device> implements Comparable<FS20Device>, Serializable {

    private List<Integer> dimStates = Arrays.asList(0, 6, 100, 12, 18, 25, 31, 37, 43, 50, 56, 62, 68, 75, 81, 87, 93);
    
    private FS20State fs20State;

    public enum FS20State {
        ON, OFF
    }

    public FS20Device() {
        type = DeviceType.FS20;
    }

    @Override
    public void onChildItemRead(String keyValue, String nodeContent) {
        if (keyValue.equals("STATE")) {
            setFs20State(nodeContent);
        }
    }

    private void setFs20State(String state) {
        if (equalsAny(state, "off", "off-for-timer", "reset", "timer")) {
            fs20State = FS20State.OFF;
        } else {
            fs20State = FS20State.ON;
        }
    }
    
    public boolean isOn() {
        return fs20State.equals(FS20State.ON);
    }

    public void toggleState() {
        String command = "set " + getName() + " ";
        if (isOn()) {
            fs20State = FS20State.OFF;
            state = "off";
            command += "off";
        } else {
            fs20State = FS20State.ON;
            state = "on";
            command += "on";
        }

        FHEMService.INSTANCE.executeCommand(command);
    }

    public boolean isDimDevice() {
        return name.contains("dim");
    }

    public void dim(int dimProgress) {

        if (! isDimDevice()) return;

        int bestMatch = -1;
        int smallestDiff = 100;
        for (Integer dimState : dimStates) {
            int diff = dimProgress - dimState;
            if (diff < 0) diff *= -1;
            
            if (bestMatch == -1 || diff < smallestDiff ) {
                bestMatch = dimState;
                smallestDiff = diff;
            }
        }

        String newState;
        if (bestMatch == 0)
            newState = "off";
        else {
            newState = "dim" + String.format("%02d", bestMatch) + "%";
        }
        state = newState;
        setFs20State(newState);

        FHEMService.INSTANCE.executeCommand("set " + name + " " + newState);
    }
    
    public int getFS20DimState() {
        if (fs20State == FS20State.OFF) {
            return 0;
        }
        
        if (state.startsWith("dim")) {
            String dimProgress = state.substring("dim".length(), state.length() - 1);
            return Integer.valueOf(dimProgress);
        }

        return 100;
    }
}
