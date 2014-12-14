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

import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.genericview.ShowField;

import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("unused")
public class RemoteControlDevice extends ToggleableDevice<RemoteControlDevice> {
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

    private String iconPath = "icons/remotecontrol";
    private String iconPrefix = "black_btn_";

    @ShowField(description = ResourceIdMapper.channel, showInOverview = true)
    private String channel;
    @ShowField(description = ResourceIdMapper.currentTitle, showInOverview = true,
            showAfter = "channel")
    private String currentTitle;

    private List<List<Entry>> rows = newArrayList();

    public void readRC_ICONPATH(String value) {
        iconPath = value;
    }

    public void readRC_ICONPREFIX(String value) {
        iconPrefix = value;
    }

    public void readCHANNEL(String value) {
        this.channel = value;
    }

    public void readCURRENTTITLE(String value) {
        this.currentTitle = value;
    }

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        super.onChildItemRead(tagName, key, value, attributes);

        if (!key.startsWith("ROW")) {
            return;
        }

        List<Entry> row = newArrayList();
        String[] rowEntries = value.split(",");
        for (String rowEntry : rowEntries) {
            String[] parts = rowEntry.split(":");
            if (parts.length == 1) {
                row.add(new Entry(parts[0]));
            } else {
                row.add(new Entry(parts[0], parts[1]));
            }
        }

        rows.add(row);
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

    public List<List<Entry>> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
