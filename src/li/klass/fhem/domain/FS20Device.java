package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FS20Device extends Device<FS20Device> implements Comparable<FS20Device>, Serializable {

    private List<Integer> dimStates = Arrays.asList(0, 6, 100, 12, 18, 25, 31, 37, 43, 50, 56, 62, 68, 75, 81, 87, 93);
    private static final List<String> dimModels = Arrays.asList("FS20DI", "FS20DI10", "FS20DU");
    
    private String model;
    private List<String> setOptions = Collections.emptyList();

    public enum FS20State {
        ON, OFF
    }

    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("STATE")) {
            Node measured = attributes.getNamedItem("measured");
            if (measured != null) {
                this.measured = measured.getTextContent();
            }
        } else if (keyValue.equalsIgnoreCase("MODEL")) {
            this.model = nodeContent.toUpperCase();
        }
    }

    @Override
    protected void onAttributeRead(String attributeKey, String attributeValue) {
        super.onAttributeRead(attributeKey, attributeValue);

        if (attributeKey.equals("SETS")) {
            setOptions = Arrays.asList(attributeValue.split(" "));
            Collections.sort(setOptions);
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.FS20;
    }

    
    public boolean isOn() {
        return getFs20State() == FS20State.ON;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isDimDevice() {
        return dimModels.contains(model);
    }
    
    public int getBestDimMatchFor(int dimProgress) {
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

        return bestMatch;
    }

    public FS20State getFs20State() {
        if (equalsAny(state, "off", "off-for-timer", "reset", "timer")) {
            return FS20State.OFF;
        }
        return FS20State.ON;
    }

    public int getFS20DimState() {
        if (getFs20State() == FS20State.OFF) {
            return 0;
        }
        
        if (state.startsWith("dim")) {
            String dimProgress = state.substring("dim".length(), state.length() - 1);
            return Integer.valueOf(dimProgress);
        }

        return 100;
    }

    public List<String> getSetOptions() {
        return setOptions;
    }
}
