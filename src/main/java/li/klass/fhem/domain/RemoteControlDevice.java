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

package li.klass.fhem.domain;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.room.xmllist.DeviceNode;

import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("unused")
public class RemoteControlDevice extends ToggleableDevice<RemoteControlDevice> {
    public class Row implements Serializable, Comparable<Row> {
        public final int index;
        public final List<Entry> entries;

        public Row(int index, List<Entry> entries) {
            this.index = index;
            this.entries = entries;
        }

        @Override
        public int compareTo(@NotNull Row another) {
            return ((Integer) index).compareTo(another.index);
        }
    }

    public class Entry implements Serializable {
        public final String command;
        public final String icon;

        public Entry(String command, String icon) {
            this.command = command;
            this.icon = icon;
        }

        public Entry(String icon) {
            this(icon, icon);
        }

        public String getIconPath() {
            return "/" + iconPath + "/" + iconPrefix + icon + ".png";
        }
    }

    @XmllistAttribute("RC_ICONPATH")
    private String iconPath = "icons/remotecontrol";

    @XmllistAttribute("RC_ICONPREFIX")
    private String iconPrefix = "black_btn_";

    @ShowField(description = ResourceIdMapper.channel, showInOverview = true)
    @XmllistAttribute("CHANNEL")
    private String channel;

    @ShowField(description = ResourceIdMapper.currentTitle, showInOverview = true, showAfter = "channel")
    @XmllistAttribute("CURRENTTITLE")
    private String currentTitle;

    private List<Row> rows = newArrayList();

    @Override
    public void onChildItemRead(DeviceNode.DeviceNodeType type, String key, String value, DeviceNode node) {
        super.onChildItemRead(type, key, value, node);

        if (!key.startsWith("row")) {
            return;
        }

        key = key.replace("row0", "").replace("row", "");
        int index = Integer.parseInt(key);


        List<Entry> entries = newArrayList();
        String[] rowEntries = value.split(",");
        for (String rowEntry : rowEntries) {
            String[] parts = rowEntry.split(":");
            if (parts.length == 1) {
                entries.add(new Entry(parts[0]));
            } else {
                entries.add(new Entry(parts[0], parts[1]));
            }
        }
        rows.add(new Row(index, entries));
    }

    @Override
    public void afterDeviceXMLRead(Context context) {
        super.afterDeviceXMLRead(context);
        Collections.sort(rows);
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.REMOTE_CONTROL;
    }

    public String getIconPath() {
        return iconPath;
    }

    public String getIconPrefix() {
        return iconPrefix;
    }

    public String getChannel() {
        return channel;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
