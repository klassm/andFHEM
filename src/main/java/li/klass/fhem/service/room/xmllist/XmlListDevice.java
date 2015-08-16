/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.service.room.xmllist;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class XmlListDevice implements Serializable {

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private String type;

    private Map<String, DeviceNode> attributes = newHashMap();
    private Map<String, DeviceNode> states = newHashMap();
    private Map<String, DeviceNode> internals = new HashMap<>();
    private Map<String, DeviceNode> header = new HashMap<>();

    public XmlListDevice(String type) {
        this.type = type;
    }

    public XmlListDevice(String type, Map<String, DeviceNode> attributes, Map<String, DeviceNode> states,
                         Map<String, DeviceNode> internals, Map<String, DeviceNode> header) {
        this.type = type;
        this.attributes = attributes;
        this.states = states;
        this.internals = internals;
        this.header = header;
    }

    public String getType() {
        return type;
    }

    public Map<String, DeviceNode> getAttributes() {
        return attributes;
    }

    public Map<String, DeviceNode> getStates() {
        return states;
    }

    public Map<String, DeviceNode> getInternals() {
        return internals;
    }

    public Map<String, DeviceNode> getHeader() {
        return header;
    }

    public void setState(String key, String value) {
        getStates().put("state", new DeviceNode(DeviceNode.DeviceNodeType.STATE, key, value, measuredNow()));
    }
    @Override
    public String toString() {
        return "XmlListDevice{" +
                "type='" + type + '\'' +
                ", attributes=" + attributes +
                ", states=" + states +
                ", internals=" + internals +
                '}';
    }

    public Optional<String> attributeValueFor(String key) {
        if (attributes.containsKey(key)) {
            return Optional.of(attributes.get(key).getValue());
        }
        return Optional.absent();
    }

    private String measuredNow() {
        return new DateTime().toString(DATE_PATTERN);
    }
}
