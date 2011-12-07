package li.klass.fhem.domain;

import android.util.Log;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;

public abstract class Device implements Serializable {

    protected String name;
    protected String room;
    protected String state;

    public void loadXML(Node xml) {
        NodeList childNodes = xml.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);

            String keyValue = item.getAttributes().getNamedItem("key").getTextContent().toUpperCase().trim();
            String nodeContent = item.getAttributes().getNamedItem("value").getTextContent().trim();

            if (keyValue.equals("ROOM")) {
                room = nodeContent;
            } else if (keyValue.equals("NAME")) {
                name = nodeContent;
            } else if (keyValue.equals("STATE")) {
                state = nodeContent;
            }

            onChildItemRead(keyValue, nodeContent);
        }
        Log.e(Device.class.getName(), this.toString());
    }


    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
    }

    public String getState() {
        return state;
    }

    public abstract void onChildItemRead(String keyValue, String nodeContent);

    @Override
    public String toString() {
        return "Device{" +
                "name='" + name + '\'' +
                ", room='" + room + '\'' +
                ", state='" + state + '\'' +
                '}';
    }

    public boolean equalsAny(String key, String... values) {
        for (String value : values) {
            if (key.equals(value)) {
                return true;
            }
        }
        return false;
    }


}
