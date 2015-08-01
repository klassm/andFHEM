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

package li.klass.fhem.domain.core;

import android.content.Context;

import com.google.common.base.Joiner;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.OverviewViewSettingsCache;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.util.DateFormatUtil;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType;
import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType.GCM_UPDATE;
import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType.INT;
import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType.STATE;

public abstract class FhemDevice<T extends FhemDevice<T>> extends HookedDevice<T> implements Comparable<T> {
    public static final long OUTDATED_DATA_MS_DEFAULT = 2 * 60 * 60 * 1000;

    protected List<String> webCmd = newArrayList();
    protected String name;
    protected String alias;

    @ShowField(description = ResourceIdMapper.definition, showAfter = "roomConcatenated")
    protected String definition;
    protected Map<String, String> eventMapReverse = newHashMap();
    protected Map<String, String> eventMap = newHashMap();
    protected SetList setList = new SetList();
    @ShowField(description = ResourceIdMapper.state, showAfter = "measured")
    private String state;
    @ShowField(description = ResourceIdMapper.measured, showAfter = "definition")
    private String measured;
    private long lastMeasureTime = -1;
    private Set<SvgGraphDefinition> svgGraphDefinitions = newHashSet();

    private boolean hasStatisticsDevice = false;

    private DeviceFunctionality deviceFunctionality;

    private OverviewViewSettings overviewViewSettingsCache;

    public final OverviewViewSettings getOverviewViewSettingsCache() {
        return overviewViewSettingsCache;
    }

    protected OverviewViewSettings getExplicitOverviewSettings() {
        return null;
    }

    @XmllistAttribute("state")
    public void setState(String value, DeviceNode node) {
        if (node.getType() == INT || node.getType() == GCM_UPDATE) {
            this.state = value;
        }
    }

    @XmllistAttribute("WEBCMD")
    public void setWebcmd(String value) {
        webCmd = newArrayList(value.split(":"));
    }

    @XmllistAttribute("DEF")
    public void setDefinition(String value) {
        definition = value;
    }

    @XmllistAttribute("EVENTMAP")
    public void setEventmap(String value) {
        parseEventMap(value);
    }

    public void afterDeviceXMLRead(Context context) {
        this.definition = getDefinition();

        if (deviceConfiguration.isPresent()) {
            deviceFunctionality = deviceConfiguration.get().getDefaultGroup();
        }

        //Optimization to prevent the expensive calls to Annotations in getView()
        overviewViewSettingsCache = getExplicitOverviewSettings();
        if (overviewViewSettingsCache == null) {
            OverviewViewSettings annotation = getClass().getAnnotation(OverviewViewSettings.class);
            if (annotation != null) {
                overviewViewSettingsCache = new OverviewViewSettingsCache(annotation);
            }
        }
    }

    public void afterAllXMLRead() {
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return deviceFunctionality == null ? DeviceFunctionality.UNKNOWN : deviceFunctionality;
    }

    private void parseEventMap(String content) {
        eventMap = newHashMap();
        eventMapReverse = newHashMap();

        if (content.startsWith("/")) {
            parseSlashesEventMap(content);
        } else {
            parseSpacesEventMap(content);
        }
    }

    private void parseSpacesEventMap(String content) {
        String[] events = content.split(" ");
        for (String event : events) {
            String[] eventParts = event.split(":");
            if (eventParts.length < 2) continue;
            putEventToEventMap(eventParts[0], eventParts[1]);
        }
    }

    private void parseSlashesEventMap(String content) {
        String[] events = content.split("/");
        for (int i = 1; i < events.length; i++) {
            String event = events[i];
            String[] eventParts = event.split(":");

            String key = eventParts[0];
            String value = eventParts.length > 1 ? eventParts[1] : eventParts[0];

            putEventToEventMap(key, value);
        }
    }

    protected void putEventToEventMap(String key, String value) {
        eventMap.put(key, value);
        eventMapReverse.put(value, key);
    }

    @ShowField(description = ResourceIdMapper.deviceName, showAfter = ShowField.FIRST)
    public String getAliasOrName() {
        if (alias != null && alias.length() != 0) {
            return alias;
        }
        return getName();
    }

    public String getName() {
        return name;
    }

    @XmllistAttribute("NAME")
    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRooms() {
        DeviceNode room = getXmlListDevice().getAttributes().get("room");
        if (room == null) {
            return newArrayList("Unsorted");
        }
        return Arrays.asList(getRoomConcatenated().split(","));
    }

    public void setRooms(List<String> rooms) {
        setRoomConcatenated(Joiner.on(",").join(rooms));
    }

    @ShowField(description = ResourceIdMapper.rooms, showAfter = "aliasOrName")
    public String getRoomConcatenated() {
        DeviceNode room = getXmlListDevice().getAttributes().get("room");
        if (room == null) {
            return "Unsorted";
        }
        return room.getValue();
    }

    public void setRoomConcatenated(String roomsConcatenated) {
        getXmlListDevice().getAttributes().put("room", new DeviceNode(DeviceNodeType.ATTR, "room", roomsConcatenated, null));
    }

    /**
     * Checks whether a device is in a given room.
     *
     * @param room room to check
     * @return true if the device is in the room
     */
    public boolean isInRoom(String room) {
        return getRooms().contains(room);
    }

    public boolean isInAnyRoomOf(List<String> rooms) {
        for (String room : rooms) {
            if (isInRoom(room)) {
                return true;
            }
        }
        return false;
    }

    public String getMeasured() {
        return measured;
    }

    @XmllistAttribute("MEASURED")
    public void setMeasured(String measuredIn) {
        this.measured = DateFormatUtil.formatTime(measuredIn);
        this.lastMeasureTime = DateFormatUtil.toMilliSeconds(measuredIn);
    }

    public long getLastMeasureTime() {
        return lastMeasureTime;
    }

    /**
     * Called for each device node in the <i>xmllist</i>.
     *
     * @param type  contains the current tag name (i.e. STATE, ATTR or INT)
     * @param key   name of the key (i.e. ROOM)
     * @param value value of the tag
     * @param node  additional tag node
     */
    public void onChildItemRead(DeviceNodeType type, String key, String value, DeviceNode node) {
        if (key.endsWith("_TIME") && !key.startsWith("WEEK") && useTimeAndWeekAttributesForMeasureTime()) {
            setMeasured(value);
        }

        if (node.getType() == STATE && "STATE".equalsIgnoreCase(node.getKey()) && measured == null) {
            setMeasured(node.getMeasured());
        }
    }

    @XmllistAttribute("SETS")
    public void setSetList(String value) {
        String setsText = value.replaceAll("\\*", "");
        if (isNullOrEmpty(setsText)) return;

        setList.parse(setsText);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FhemDevice device = (FhemDevice) o;

        return !(name != null ? !name.equals(device.name) : device.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public final int compareTo(@NotNull T other) {
        String comparableAttribute = firstNonNull(sortBy, getAliasOrName());
        String otherComparableAttribute = firstNonNull(other.sortBy, other.getAliasOrName());

        return comparableAttribute.compareTo(otherComparableAttribute);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        if (eventMap.containsKey(state)) {
            this.state = eventMap.get(state);
        } else {
            this.state = state;
        }
    }

    public String getInternalState() {
        String state = getState();
        if (eventMapReverse == null || !eventMapReverse.containsKey(state)) return state;
        return eventMapReverse.get(state);
    }

    public String getAlias() {
        return alias;
    }

    @XmllistAttribute("ALIAS")
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDefinition() {
        return definition;
    }

    public String getEventMapStateFor(String state) {
        if (eventMap.containsKey(state)) {
            return eventMap.get(state);
        }

        return state;
    }

    public String getReverseEventMapStateFor(String state) {
        return eventMapReverse.containsKey(state) ? eventMapReverse.get(state) : state;
    }

    public Map<String, String> getEventMap() {
        return eventMap;
    }

    public SetList getSetList() {
        return setList;
    }

    /**
     * Generate an array of available target states, but respect any set event maps.
     *
     * @return array of available target states
     */
    public String[] getAvailableTargetStatesEventMapTexts() {
        if (setList == null) return new String[]{};

        List<String> sortedKeys = setList.getSortedKeys();
        List<String> eventMapKeys = newArrayList();
        for (String key : sortedKeys) {
            String eventMapKey = eventMapReverse.containsKey(key) ? eventMapReverse.get(key) : key;
            eventMapKeys.add(eventMapKey);
        }
        return eventMapKeys.toArray(new String[eventMapKeys.size()]);
    }

    public String formatTargetState(String targetState) {
        if (eventMapReverse != null && eventMapReverse.containsKey(targetState)) {
            return eventMapReverse.get(targetState);
        }
        return targetState;
    }

    public String formatStateTextToSet(String stateToSet) {
        return stateToSet;
    }


    public List<String> getWebCmd() {
        return webCmd;
    }


    public String getWidgetName() {
        return firstNonNull(widgetName, getAliasOrName());
    }

    public List<String> getInternalDeviceGroupOrGroupAttributes(Context context) {
        List<String> groups = newArrayList();
        DeviceNode groupAttribute = getXmlListDevice().getAttributes().get("group");
        if (groupAttribute != null) {
            groups.addAll(asList(groupAttribute.getValue().split(",")));
        } else {
            groups.add(getDeviceGroup().getCaptionText(context));
        }
        return groups;
    }

    protected boolean useTimeAndWeekAttributesForMeasureTime() {
        return true;
    }

    public boolean hasStatisticsDevice() {
        return hasStatisticsDevice;
    }

    public void setHasStatisticsDevice(boolean hasStatisticsDevice) {
        this.hasStatisticsDevice = hasStatisticsDevice;
    }

    public void addSvgGraphDefinition(SvgGraphDefinition svgGraphDefinition) {
        svgGraphDefinitions.add(svgGraphDefinition);
    }

    public Set<SvgGraphDefinition> getSvgGraphDefinitions() {
        return svgGraphDefinitions;
    }

    @Override
    public String toString() {
        return "Device{" +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                ", alias='" + alias + '\'' +
                ", measured='" + measured + '\'' +
                ", definition='" + definition + '\'' +
                ", eventMapReverse=" + eventMapReverse +
                ", eventMap=" + eventMap +
                ", setList=" + setList.toString() +
                '}';
    }
}
