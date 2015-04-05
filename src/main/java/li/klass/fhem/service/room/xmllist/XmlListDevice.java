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

import java.util.Map;

public class XmlListDevice {
    private String type;

    private Map<String, DeviceNode> attributes;
    private Map<String, DeviceNode> states;
    private Map<String, DeviceNode> internals;
    private Map<String, DeviceNode> header;

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

    @Override
    public String toString() {
        return "XmlListDevice{" +
                "type='" + type + '\'' +
                ", attributes=" + attributes +
                ", states=" + states +
                ", internals=" + internals +
                '}';
    }
}
