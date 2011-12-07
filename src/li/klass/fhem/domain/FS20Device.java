package li.klass.fhem.domain;

import li.klass.fhem.dataprovider.FHEMService;
import org.w3c.dom.Node;

public class FS20Device extends Device implements Comparable<FS20Device> {

    private FS20State fs20State;

    public enum FS20State {
        ON, OFF
    }

    @Override
    public void onChildItemRead(String keyValue, String nodeContent) {
        if (keyValue.equals("STATE")) {
            if (equalsAny(nodeContent, "off", "off-for-timer", "reset", "timer")) {
                fs20State = FS20State.OFF;
            } else {
                fs20State = FS20State.ON;
            }
        }
    }

    public int compareTo(FS20Device device) {
        return device.getName().compareTo(getName());
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


    // sets="dim06% dim100% dim12% dim18% dim25% dim31% dim37% dim43% dim50% dim56% dim62% dim68% dim75% dim81% dim87% dim93% dimdown dimup dimupdown off off-for-timer on
    // on-100-for-timer-prev on-for-timer on-old-for-timer on-old-for-timer-prev on-till ramp-off-time ramp-on-time reset sendstate timer toggle"
}
