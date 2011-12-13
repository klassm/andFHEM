package li.klass.fhem.domain;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;

public abstract class Device<T extends Device> implements Serializable, Comparable<T> {

    protected String name;
    protected String room = "unknown";
    protected String state;
    protected DeviceType type;

    public void loadXML(Node xml) {
        NodeList childNodes = xml.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item == null || item.getAttributes() == null) continue;

            Node keyAttribute = item.getAttributes().getNamedItem("key");
            if (keyAttribute == null) continue;

            String keyValue = keyAttribute.getTextContent().toUpperCase().trim();
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
    }


    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
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

    public boolean isHiddenDevice() {
        return room.equalsIgnoreCase("hidden");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        return !(name != null ? !name.equals(device.name) : device.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public int compareTo(T t) {
        return getName().compareTo(t.getName());
    }
}
