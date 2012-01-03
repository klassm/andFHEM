package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

public class RFXX10RECDevice extends Device<RFXX10RECDevice> {
    
    private String lastStateChangeTime;
    private String lastState;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TIME")) {
            measured = nodeContent;
        } else if (keyValue.equals("STATECHANGE")) {
            lastStateChangeTime = attributes.getNamedItem("measured").getTextContent();
            lastState = nodeContent;
        }
    }

    public String getLastStateChangedTime() {
        return lastStateChangeTime;
    }

    public String getLastState() {
        return lastState;
    }
}
