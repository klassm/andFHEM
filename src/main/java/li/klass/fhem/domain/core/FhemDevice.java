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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import li.klass.fhem.domain.EventMap;
import li.klass.fhem.domain.EventMapParser;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.OverviewViewSettingsCache;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.resources.ResourceIdMapper;
import li.klass.fhem.service.room.xmllist.DeviceNode;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.util.DateFormatUtil;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType;
import static li.klass.fhem.service.room.xmllist.DeviceNode.DeviceNodeType.STATE;

public abstract class FhemDevice<T extends FhemDevice<T>> extends HookedDevice<T> implements Comparable<T> {

    public static final Function<FhemDevice, XmlListDevice> TO_XMLLIST_DEVICE = new Function<FhemDevice, XmlListDevice>() {
        @Override
        public XmlListDevice apply(FhemDevice input) {
            return input == null ? null : input.getXmlListDevice();
        }
    };
    protected List<String> webCmd = newArrayList();


    @ShowField(description = ResourceIdMapper.definition, showAfter = "roomConcatenated")
    protected String definition;
    protected EventMap eventMap = new EventMap(Collections.<String, String>emptyMap());
    protected SetList setList = new SetList();
    @ShowField(description = ResourceIdMapper.measured, showAfter = "definition")
    private String measured;
    private long lastMeasureTime = -1;

    private DeviceFunctionality deviceFunctionality;

    private OverviewViewSettings overviewViewSettingsCache;

    public final OverviewViewSettings getOverviewViewSettingsCache() {
        return overviewViewSettingsCache;
    }

    protected OverviewViewSettings getExplicitOverviewSettings() {
        return null;
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
        eventMap = EventMapParser.INSTANCE.parseEventMap(content);
    }

    protected void putEventToEventMap(String key, String value) {
        eventMap = eventMap.put(key, value);
    }

    @ShowField(description = ResourceIdMapper.deviceName, showAfter = ShowField.FIRST)
    public String getAliasOrName() {
        String alias = getAlias();
        if (alias != null && alias.length() != 0) {
            return alias;
        }
        return getName();
    }

    public String getName() {
        return getXmlListDevice().getName();
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
        getXmlListDevice().getAttributes().put("room", new DeviceNode(DeviceNodeType.ATTR, "room", roomsConcatenated, (DateTime) null));
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

    public String getMeasured() {
        return measured;
    }

    @XmllistAttribute("MEASURED")
    public void setMeasured(String measuredIn) {
        this.measured = DateFormatUtil.formatTime(measuredIn);
        this.lastMeasureTime = DateFormatUtil.toMilliSeconds(measuredIn);
    }

    public void setMeasured(DateTime measuredIn) {
        measuredIn = measuredIn != null ? measuredIn : DateTime.now();
        this.measured = DateFormatUtil.formatTime(measuredIn);
        this.lastMeasureTime = measuredIn.getMillis();
    }

    public long getLastMeasureTime() {
        return lastMeasureTime;
    }

    /**
     * Called for each device node in the <i>xmllist</i>.
     *
     * @param type  contains the current tag name (i.e. State, ATTR or INT)
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

        FhemDevice other = (FhemDevice) o;
        String name = getName();
        return (other.getName() == null && name == null) || (name != null && name.equals(other.getName()));

    }

    @Override
    public int hashCode() {
        String name = getName();
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public final int compareTo(@NotNull T other) {
        String comparableAttribute = firstNonNull(sortBy, getAliasOrName());
        String otherComparableAttribute = firstNonNull(other.sortBy, other.getAliasOrName());

        return comparableAttribute.compareTo(otherComparableAttribute);
    }

    @ShowField(description = ResourceIdMapper.state, showAfter = "measured")
    public String getState() {
        XmlListDevice xmlListDevice = getXmlListDevice();
        Optional<String> state = xmlListDevice.getInternal("STATE");
        return state.or(xmlListDevice.getState("state")).or("");
    }

    public void setState(String state) {
        String value = eventMap.getValueFor(state);
        if (value != null){
            state = value;
        }

        XmlListDevice device = getXmlListDevice();
        device.setState("state", state);
        if (device.getInternals().containsKey("STATE")) {
            device.setInternal("STATE", state);
        }
        if (device.getHeader().containsKey("state")) {
            device.setHeader("state", state);
        }
    }

    public String getInternalState() {
        String state = getState();
        String value = eventMap.getKeyFor(state);
        if (value == null) return state;
        return value;
    }

    public String getAlias() {
        return getXmlListDevice().getAttribute("alias").orNull();
    }

    public String getDefinition() {
        return definition;
    }

    public String getEventMapStateFor(String state) {
        return eventMap.getOr(state, state);
    }

    public String getReverseEventMapStateFor(String state) {
        return eventMap.getKeyOr(state, state);
    }

    public EventMap getEventMap() {
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
            String eventMapKey = eventMap.getKeyOr(key, key);
            eventMapKeys.add(eventMapKey);
        }
        return eventMapKeys.toArray(new String[eventMapKeys.size()]);
    }

    public String formatTargetState(String targetState) {
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

    @Override
    public String toString() {
        String name = getName();
        String alias = getAlias();
        return "Device{" +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", measured='" + measured + '\'' +
                ", definition='" + definition + '\'' +
                ", eventMap=" + eventMap +
                ", setList=" + setList.toString() +
                '}';
    }
}
