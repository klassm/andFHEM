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

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.w3c.dom.NamedNodeMap;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.domain.log.CustomGraph;
import li.klass.fhem.domain.log.LogDevice;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.graph.description.SeriesType;
import li.klass.fhem.service.room.AllDevicesReadCallback;
import li.klass.fhem.service.room.DeviceReadCallback;
import li.klass.fhem.util.DateFormatUtil;
import li.klass.fhem.util.StringUtil;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

@SuppressWarnings("unused")
public abstract class Device<T extends Device> implements Serializable, Comparable<T> {

    public static final long OUTDATED_DATA_MS_DEFAULT = 2 * 60 * 60 * 1000;
    public static final long NEVER_OUTDATE_DATA = 0;
    protected List<String> rooms;
    protected List<String> webCmd = newArrayList();
    protected String name;
    protected String alias;
    @ShowField(description = ResourceIdMapper.definition, showAfter = "roomConcatenated")
    protected String definition;
    protected Map<String, String> eventMapReverse = newHashMap();
    protected Map<String, String> eventMap = newHashMap();
    protected SetList setList = new SetList();
    protected volatile List<LogDevice> logDevices = newArrayList();
    @ShowField(description = ResourceIdMapper.state, showAfter = "measured")
    private String state;
    @ShowField(description = ResourceIdMapper.measured, showAfter = "definition")
    private String measured;
    private long lastMeasureTime = -1;
    private String group;
    private List<DeviceChart> deviceCharts = newArrayList();
    private transient AllDevicesReadCallback allDevicesReadCallback;
    private transient DeviceReadCallback deviceReadCallback;
    private String widgetName;
    private boolean alwaysHidden = false;
    private boolean hasStatisticsDevice = false;

    public void readROOM(String value) {
        setRoomConcatenated(value);
    }

    public void readNAME(String value) {
        name = value;
    }

    public void readWIDGET_NAME(String value) {
        this.widgetName = value;
    }

    public void readSTATE(String tagName, NamedNodeMap attributes, String value) {
        if (tagName.equals("INT")) {
            state = formatTargetState(value);
        }
    }

    public void readWEBCMD(String value) {
        webCmd = newArrayList(value.split(":"));
    }

    public void gcmState(String value) {
        state = value;
        setMeasured(DateFormatUtil.toReadable(new DateTime()));
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
        setMeasured(value);
    }

    public void readALWAYS_HIDDEN(String value) {
        alwaysHidden = "true".equalsIgnoreCase(value);
    }

    public void readGROUP(String value) {
        group = value;
    }

    public void afterDeviceXMLRead() {
    }

    public void afterAllXMLRead() {
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

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRooms() {
        if (rooms == null || rooms.size() == 0) {
            return newArrayList(AndFHEMApplication.getContext().getResources().getString(R.string.unsortedRoomName));
        }
        return rooms;
    }

    public void setRooms(List<String> rooms) {
        this.rooms = rooms;
    }

    @ShowField(description = ResourceIdMapper.room, showAfter = "aliasOrName")
    public String getRoomConcatenated() {
        return StringUtil.concatenate(getRooms(), ",");
    }

    public void setRoomConcatenated(String roomsConcatenated) {
        this.rooms = newArrayList(roomsConcatenated.split(","));
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

    public void setMeasured(String measured) {
        this.measured = measured;
        this.lastMeasureTime = DateFormatUtil.toMilliSeconds(measured);
    }

    public long getLastMeasureTime() {
        return lastMeasureTime;
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
        if (key.endsWith("_TIME") && !key.startsWith("WEEK") && useTimeAndWeekAttributesForMeasureTime()) {
            setMeasured(value);
        }
    }

    public void onAttributeRead(String attributeName, String attributeValue) {
        if (attributeName.equals("SETS")) {
            String setsText = attributeValue.replaceAll("\\*", "");
            if (StringUtil.isBlank(setsText)) return;

            setList.parse(setsText);
        }
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
    public final int compareTo(@NotNull T t) {
        return getAliasOrName().compareTo(t.getAliasOrName());
    }

    public List<LogDevice> getLogDevices() {
        return logDevices;
    }

    public void addLogDevice(LogDevice logDevice) {
        logDevices.add(logDevice);

        // Unfortunately, this is called multiple times (whenever a log devices registers
        // itself for the device. However, we have no idea in which order the callbacks are
        // called, so we cannot register a listeners when all devices have been read ...
        if (!logDevices.isEmpty()) {
            fillDeviceCharts(deviceCharts);
        }
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

        if (hasStatisticsDevice) {
            deviceCharts.add(new DeviceChart(R.string.averagesGraph,
                    new ChartSeriesDescription.Builder()
                            .withSeriesType(SeriesType.AVERAGE_HOUR)
                            .withColumnName(R.string.avgHour)
                            .withFileLogSpec("5:Hour\\x3a:0:")
                            .withDbLogSpec("hour")
                            .build(),
                    new ChartSeriesDescription.Builder()
                            .withSeriesType(SeriesType.AVERAGE_DAY)
                            .withColumnName(R.string.avgDay)
                            .withFileLogSpec("7:Day\\x3a:0:")
                            .withDbLogSpec("day")
                            .build(),
                    new ChartSeriesDescription.Builder()
                            .withSeriesType(SeriesType.AVERAGE_MONTH)
                            .withColumnName(R.string.avgMonth)
                            .withFileLogSpec("9:Month\\x3a:0:")
                            .withDbLogSpec("month")
                            .build(),
                    new ChartSeriesDescription.Builder()
                            .withSeriesType(SeriesType.AVERAGE_YEAR)
                            .withColumnName(R.string.avgYear)
                            .withFileLogSpec("11:Year\\x3a:0:")
                            .withDbLogSpec("year")
                            .build()
            ));
        }

        for (LogDevice<?> logDevice : logDevices) {
            List<CustomGraph> customGraphs = logDevice.getCustomGraphs();

            for (CustomGraph customGraph : customGraphs) {
                ChartSeriesDescription seriesDescription = new ChartSeriesDescription.Builder()
                        .withColumnName(customGraph.description)
                        .withFileLogSpec(customGraph.columnSpecification)
                        .withDbLogSpec(customGraph.columnSpecification)
                        .withFallbackYAxisName(customGraph.yAxisName)
                        .withYAxisMinMaxValue(customGraph.yAxisMinMax)
                        .build();

                addDeviceChartIfNotNull(new DeviceChart(customGraph.description, seriesDescription));
            }
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDefinition() {
        return definition;
    }

    public boolean isSupported() {
        return !alwaysHidden;
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

    public boolean supportsWidget(Class<? extends DeviceAppWidgetView> appWidgetClass) {
        return true;
    }

    public String getWidgetName() {
        if (widgetName == null) return getAliasOrName();
        return widgetName;
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

    /**
     * Indicates whether the internal device state should be updated with the value given in state to set
     *
     * @param stateToSet state to set
     * @return true to update the internal state, else false
     */
    public boolean shouldUpdateStateOnDevice(String stateToSet) {
        return true;
    }

    @Override
    public String toString() {
        return "Device{" +
                "rooms=" + (rooms == null ? null : rooms) +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                ", alias='" + alias + '\'' +
                ", measured='" + measured + '\'' +
                ", definition='" + definition + '\'' +
                ", eventMapReverse=" + eventMapReverse +
                ", eventMap=" + eventMap +
                ", setList=" + setList.toString() +
                ", logDevices=" + logDevices.size() +
                ", deviceCharts=" + deviceCharts +
                '}';
    }

    public AllDevicesReadCallback getDeviceReadCallback() {
        return deviceReadCallback;
    }

    public void setDeviceReadCallback(DeviceReadCallback deviceReadCallback) {
        this.deviceReadCallback = deviceReadCallback;
    }

    public AllDevicesReadCallback getAllDeviceReadCallback() {
        return allDevicesReadCallback;
    }

    public void setAllDeviceReadCallback(AllDevicesReadCallback allDevicesReadCallback) {
        this.allDevicesReadCallback = allDevicesReadCallback;
    }

    /**
     * Trigger a state notification if a device attribute has changed via GCM
     *
     * @return true or false
     */
    public boolean triggerStateNotificationOnAttributeChange() {
        return false;
    }

    public List<String> getWebCmd() {
        return webCmd;
    }

    /**
     * Hook called for each xml attribute of a device. If false is returned, the attribute is ignored.
     * Note that the given key is provided in the form it is present within the xmllist. Thus,
     * it is not, as provided within #onChildItemRead, uppercase.
     *
     * @param key node key
     * @return true if the attribute is accepted, else false
     */
    public boolean acceptXmlKey(String key) {
        return true;
    }

    /**
     * Functionality of the device.
     *
     * @return NEVER null!
     */
    public abstract DeviceFunctionality getDeviceGroup();

    public List<String> getInternalDeviceGroupOrGroupAttributes() {
        List<String> groups = newArrayList();
        if (!isNullOrEmpty(group)) {
            groups.addAll(asList(group.split(",")));
        } else {
            groups.add(getDeviceGroup().getCaptionText(AndFHEMApplication.getContext()));
        }
        return groups;
    }

    protected boolean useTimeAndWeekAttributesForMeasureTime() {
        return true;
    }

    public long getTimeRequiredForStateError() {
        return NEVER_OUTDATE_DATA;
    }

    public boolean isOutdatedData(long lastUpdateTime) {
        long timeRequiredForStateError = getTimeRequiredForStateError();
        return timeRequiredForStateError != NEVER_OUTDATE_DATA
                && lastMeasureTime != -1
                && lastUpdateTime - lastMeasureTime > timeRequiredForStateError;

    }

    public boolean isSensorDevice() {
        return false;
    }

    public boolean hasStatisticsDevice() {
        return hasStatisticsDevice;
    }

    public void setHasStatisticsDevice(boolean hasStatisticsDevice) {
        this.hasStatisticsDevice = hasStatisticsDevice;
    }
}
