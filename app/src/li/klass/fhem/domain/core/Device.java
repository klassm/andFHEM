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

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.view.widget.AppWidgetView;
import li.klass.fhem.domain.FileLogDevice;
import li.klass.fhem.domain.floorplan.Coordinate;
import li.klass.fhem.domain.floorplan.FloorplanPosition;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.room.AssociatedDeviceCallback;
import li.klass.fhem.util.DateFormatUtil;
import li.klass.fhem.util.StringUtil;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("unused")
public abstract class Device<T extends Device> implements Serializable, Comparable<T> {

    protected String[] rooms;

    protected String name;

    private String state;

    protected String alias;

    @ShowField(description = ResourceIdMapper.measured)
    protected String measured;

    @ShowField(description = ResourceIdMapper.definition)
    protected String definition;
    protected Map<String, String> eventMapReverse = new HashMap<String, String>();
    protected Map<String, String> eventMap = new HashMap<String, String>();
    private String[] availableTargetStates;

    private Map<String, FloorplanPosition> floorPlanPositionMap = new HashMap<String, FloorplanPosition>();

    protected volatile FileLogDevice fileLog;
    private List<DeviceChart> deviceCharts = new ArrayList<DeviceChart>();
    private transient AssociatedDeviceCallback associatedDeviceCallback;


    public void readROOM(String value) {
        setRoomConcatenated(value);
    }

    public void readNAME(String value) {
        name = value;
    }

    public void readSTATE(String tagName, NamedNodeMap attributes, String value) {
        if (tagName.equals("INT")) {
            state = formatTargetState(value);
        }
    }

    public void gcmState(String value) {
        state = value;
        measured = DateFormatUtil.toReadable(new Date());
    }

    public void readDEF(String value) {
        definition = value;
    }

    public void readEVENTMAP(String value) {
        parseEventMap(value);
    }

    public void readALIAS(String value) {
        alias = value;
    }

    public void readMEASURED(String value) {
        measured = value;
    }

    public void afterXMLRead() {
        if (fileLog != null) {
            fillDeviceCharts(deviceCharts);
        }
    }

    private void parseEventMap(String content) {
        eventMap = new HashMap<String, String>();
        eventMapReverse = new HashMap<String, String>();

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

    public String getAliasOrName() {
        if (alias != null && alias.length() != 0) {
            return alias;
        }
        return getName();
    }

    public String getName() {
        return name;
    }

    public String[] getRooms() {
        if (rooms == null) {
            return new String[]{AndFHEMApplication.getContext().getResources().getString(R.string.unsortedRoomName)};
        }
        return rooms;
    }

    public String getRoomConcatenated() {
        return StringUtil.concatenate(getRooms(), ",");
    }

    /**
     * Checks whether a device is in a given room.
     *
     * @param room room to check
     * @return true if the device is in the room
     */
    public boolean isInRoom(String room) {
        for (String internalRoom : getRooms()) {
            if (internalRoom.equals(room)) {
                return true;
            }
        }
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeasured() {
        return measured;
    }

    /**
     * Called for each device node in the <i>xmllist</i>.
     *
     * @param tagName    contains the current tag name (i.e. STATE, ATTR or INT)
     * @param key        name of the key (i.e. ROOM)
     * @param value      value of the tag
     * @param attributes additional tag attributes
     */
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        if (key.startsWith("FP_")) {
            String[] commaParts = value.split(",");
            if (commaParts.length <= 2) {
                return;
            }
            int y = Integer.valueOf(commaParts[0]);
            int x = Integer.valueOf(commaParts[1]);
            int viewType = Integer.valueOf(commaParts[2]);

            floorPlanPositionMap.put(key.substring(3), new FloorplanPosition(x, y, viewType));
        } else if (key.endsWith("_TIME") && !key.startsWith("WEEK")) {
            measured = value;
        }
    }

    public void onAttributeRead(String attributeName, String attributeValue) {
        if (attributeName.equals("SETS")) {
            String setsText = attributeValue.replaceAll("\\*", "");
            if (StringUtil.isBlank(setsText)) return;

            parseAvailableTargetStates(setsText);
        }
    }

    private void parseAvailableTargetStates(String setsText) {
        setsText = setsText.trim();
        String lowercase = setsText.toLowerCase();

        if (lowercase.equals("") || lowercase.equals("*") || lowercase.contains("no set function")
                || lowercase.contains("needs one parameter")) {
            return;
        }

        String[] targetStates;
        if (setsText.startsWith("state:")) {
            setsText = setsText.substring("state:".length());

            targetStates = setsText.split(",");
        } else {
            targetStates = setsText.split(" ");
            for (int i = 0; i < targetStates.length; i++) {
                String targetState = targetStates[i];
                if (targetState.contains(":")) {
                    targetStates[i] = targetState.substring(0, targetState.indexOf(":"));
                }
            }
        }
        this.availableTargetStates = targetStates;
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

    public FileLogDevice getFileLog() {
        return fileLog;
    }

    public void setFileLog(FileLogDevice fileLog) {
        this.fileLog = fileLog;
    }

    public List<DeviceChart> getDeviceCharts() {
        return deviceCharts;
    }

    /**
     * Override me if you want to provide charts for a device
     *
     * @param chartSeries fill me with chart descriptions
     */
    protected void fillDeviceCharts(List<DeviceChart> chartSeries) {
        deviceCharts.clear();
        if (fileLog == null) return;

        for (FileLogDevice.CustomGraph customGraph : fileLog.getCustomGraphs()) {
            addDeviceChartIfNotNull(new DeviceChart(customGraph.description, new ChartSeriesDescription(
                    customGraph.description, customGraph.columnSpecification
            )));
        }
    }

    protected void addDeviceChartIfNotNull(DeviceChart holder, Object... notNull) {
        for (Object o : notNull) {
            if (o == null) return;
        }
        deviceCharts.add(holder);
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

    public void setRoomConcatenated(String roomsConcatenated) {
        this.rooms = roomsConcatenated.split(",");
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public String getDefinition() {
        return definition;
    }

    public boolean isSupported() {
        return true;
    }

    public String getEventMapStateForCurrentState() {
        return getEventMapStateFor(getState());
    }

    public String getEventMapStateFor(String state) {
        if (eventMap.containsKey(state)) {
            return eventMap.get(state);
        }

        return state;
    }

    public Map<String, String> getEventMap() {
        return eventMap;
    }

    public boolean isOnFloorplan(String floorplan) {
        if (floorPlanPositionMap == null || floorplan == null) return false;
        return floorPlanPositionMap.containsKey(floorplan.toUpperCase());
    }

    public FloorplanPosition getFloorplanPositionFor(String floorplan) {
        return floorPlanPositionMap.get(floorplan.toUpperCase());
    }

    public void setCoordinateFor(String floorplan, Coordinate coordinate) {
        String key = floorplan.toUpperCase();
        if (!floorPlanPositionMap.containsKey(key)) return;

        FloorplanPosition floorplanPosition = floorPlanPositionMap.get(key);
        FloorplanPosition newPosition = new FloorplanPosition(coordinate.x, coordinate.y, floorplanPosition.viewType);

        floorPlanPositionMap.put(key, newPosition);
    }

    public String[] getAvailableTargetStates() {
        return availableTargetStates;
    }

    public boolean supportsWidget(Class<? extends AppWidgetView> appWidgetClass) {
        return true;
    }

    public String formatTargetState(String targetState) {
        return targetState;
    }

    public String formatStateTextToSet(String stateToSet) {
        return stateToSet;
    }

    @Override
    public String toString() {
        return "Device{" +
                "rooms=" + (rooms == null ? null : Arrays.asList(rooms)) +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                ", alias='" + alias + '\'' +
                ", measured='" + measured + '\'' +
                ", definition='" + definition + '\'' +
                ", eventMapReverse=" + eventMapReverse +
                ", eventMap=" + eventMap +
                ", availableTargetStates=" + (availableTargetStates == null ? null : Arrays.asList(availableTargetStates)) +
                ", floorPlanPositionMap=" + floorPlanPositionMap +
                ", fileLog=" + fileLog +
                ", deviceCharts=" + deviceCharts +
                '}';
    }

    public void setAssociatedDeviceCallback(AssociatedDeviceCallback associatedDeviceCallback) {
        this.associatedDeviceCallback = associatedDeviceCallback;
    }

    public AssociatedDeviceCallback getAssociatedDeviceCallback() {
        return associatedDeviceCallback;
    }

    /**
     * Trigger a state notification if a device attribute has changed via GCM
     *
     * @return true or false
     */
    public boolean triggerStateNotificationOnAttributeChange() {
        return false;
    }
}
