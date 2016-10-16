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

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public class XmlListDevice implements Serializable {

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private String type;

    private Map<String, DeviceNode> attributes = new HashMap<>();
    private Map<String, DeviceNode> states = new HashMap<>();
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

    public boolean containsInternal(String key) {
        return internals.containsKey(key);
    }

    public boolean containsState(String state) {
        return containsAnyOfStates(Collections.singleton(state));
    }

    public boolean containsAnyOfStates(Collection<String> toFind) {
        for (String state : toFind) {
            if (states.containsKey(state)) {
                return true;
            }

        }
        return false;
    }

    public boolean containsAttribute(String attribute) {
        return attributes.containsKey(attribute);
    }

    public Optional<String> getState(String state) {
        if (containsState(state)) {
            return Optional.of(states.get(state).getValue());
        }
        return Optional.absent();
    }

    public Optional<String> getFirstStateOf(Collection<String> toFind) {
        for (String state : toFind) {
            Optional<String> found = getState(state);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.absent();
    }

    public Optional<String> getAttribute(String attribute) {
        if (containsAttribute(attribute)) {
            return Optional.of(attributes.get(attribute).getValue());
        }
        return Optional.absent();
    }

    public Optional<String> getInternal(String key) {
        if (containsInternal(key)) {
            return Optional.of(internals.get(key).getValue());
        }
        return Optional.absent();
    }

    public void setState(String key, String value) {
        getStates().put(key, new DeviceNode(DeviceNode.DeviceNodeType.STATE, key, value, measuredNow()));
    }

    public void setInternal(String key, String value) {
        getInternals().put(key, new DeviceNode(DeviceNode.DeviceNodeType.INT, key, value, measuredNow()));
    }

    public void setHeader(String key, String value) {
        getHeader().put(key, new DeviceNode(DeviceNode.DeviceNodeType.HEADER, key, value, measuredNow()));
    }

    public void setAttribute(String key, String value) {
        value = trimToNull(value);
        if (value == null) {
            getAttributes().remove(key);
            return;
        }
        if (!(value.equalsIgnoreCase(getAttribute(key).orNull()))) {
            getAttributes().put(key, new DeviceNode(DeviceNode.DeviceNodeType.ATTR, key, value, measuredNow()));
        }
    }

    public String getName() {
        DeviceNode nameNode = getInternals().get("NAME");
        return nameNode == null ? null : nameNode.getValue();
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

    public Optional<String> stateValueFor(String key) {
        if (states.containsKey(key)) {
            return Optional.of(states.get(key).getValue());
        }
        return Optional.absent();
    }

    private String measuredNow() {
        return new DateTime().toString(DATE_PATTERN);
    }
}
