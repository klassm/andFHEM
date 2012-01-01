package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class LGTVDevice extends Device<LGTVDevice> {
    private String power;
    private String audio;
    private String input;

    @Override
    protected void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("POWER")) {
            this.power = nodeContent;
        } else if (keyValue.equals("AUDIO")) {
            this.audio = nodeContent;
        } else if (keyValue.equals("INPUT")) {
            this.input = nodeContent;
        }
    }

    public String getPower() {
        return power;
    }

    public String getAudio() {
        return audio;
    }

    public String getInput() {
        return input;
    }
}
