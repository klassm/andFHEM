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

import org.w3c.dom.NamedNodeMap;

import java.util.Locale;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueDescriptionUtil;

@OverviewViewSettings(showState = false, showMeasured = true)
@SuppressWarnings("unused")
public class FBCallmonitorDevice extends Device<FBCallmonitorDevice> {

    private enum Event {
        CALL(R.string.callMonEventCall),
        RING(R.string.callMonEventRing),
        CONNECT(R.string.callMonEventConnect),
        DISCONNECT(R.string.callMonEventDisconnect),
        MISSED(R.string.callMonEventMissed);

        private final int stringId;

        Event(int stringId) {
            this.stringId = stringId;
        }
    }

    @ShowField(description = ResourceIdMapper.callMonExternalName, showInOverview = true)
    private String externalName;

    @ShowField(description = ResourceIdMapper.callMonExternalNumber, showInOverview = true)
    private String externalNumber;

    @ShowField(description = ResourceIdMapper.callMonInternalNumber)
    private String internalNumber;

    private String missedCallNumber;

    @ShowField(description = ResourceIdMapper.callMonEvent, showInOverview = true)
    private String event;

    @ShowField(description = ResourceIdMapper.callMonDuration, showInOverview = true)
    private String callDuration;

    private Event eventInternal;

    @Override
    public void onChildItemRead(String tagName, String key, String value, NamedNodeMap attributes) {
        super.onChildItemRead(tagName, key, value, attributes);
        if (key.equalsIgnoreCase("event")) {
            String measured = attributes.getNamedItem("measured").getNodeValue();
            setMeasured(measured);
        }
    }

    public void readEXTERNAL_NAME(String value) {
        externalName = value;
    }

    public void readEXTERNAL_NUMBER(String value) {
        externalNumber = value;
    }

    public void readINTERNAL_NUMBER(String value) {
        internalNumber = value;
    }

    public void readMISSED_CALL(String value) {
        int firstSpace = value.indexOf(" ");
        if (firstSpace != -1) {
            value = value.substring(0, firstSpace);
        }
        this.missedCallNumber = value;
    }

    public void readEVENT(String value) {
        eventInternal = Event.valueOf(value.toUpperCase(Locale.getDefault()));
    }

    public void readCALL_DURATION(String value) {
        int seconds = Integer.valueOf(value);
        callDuration = ValueDescriptionUtil.secondsToTimeString(seconds);
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();
        Context context = AndFHEMApplication.getContext();

        int eventStringId;
        if (eventInternal == null) {
            return;
        }

        if (eventInternal == Event.DISCONNECT && missedCallNumber != null
                && missedCallNumber.equals(externalNumber)) {
            eventInternal = Event.MISSED;
        }

        String eventString = context.getString(eventInternal.stringId);
        event = eventString;

        String description = externalNumber;
        if (externalName != null && !externalName.equalsIgnoreCase("unknown")
                && !externalName.equalsIgnoreCase("timeout")) {
            description = externalName;
        }

        setState(description + " (" + eventString + ")");
    }

    public String getExternalName() {
        return externalName;
    }

    public String getExternalNumber() {
        return externalNumber;
    }

    public String getInternalNumber() {
        return internalNumber;
    }

    public String getMissedCallNumber() {
        return missedCallNumber;
    }

    public String getEvent() {
        return event;
    }

    public String getCallDuration() {
        return callDuration;
    }

    @Override
    public boolean triggerStateNotificationOnAttributeChange() {
        return true;
    }

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.CALL_MONITOR;
    }
}
