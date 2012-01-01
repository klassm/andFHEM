package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CULFHTTKDevice extends Device<CULFHTTKDevice> {

    private String lastStateChangeTime;
    private String lastState;
    
    @Override
    public void onChildItemRead(String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("PREVSTATE")) {
            lastState = nodeContent;
        } else if (keyValue.equals("PREVTIMESTAMP") && ! nodeContent.isEmpty()) {
            long timestamp = Long.valueOf(nodeContent);
            Date date = new Date(timestamp * 1000L);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");
            lastStateChangeTime = simpleDateFormat.format(date);
        }
    }

    public String getLastStateChangeTime() {
        return lastStateChangeTime;
    }

    public String getLastState() {
        return lastState;
    }
}
