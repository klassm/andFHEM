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

import java.io.Serializable;

import li.klass.fhem.appwidget.view.widget.base.DeviceAppWidgetView;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.OverviewViewSettingsCache;
import li.klass.fhem.service.room.AllDevicesReadCallback;
import li.klass.fhem.service.room.DeviceReadCallback;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

public abstract class Device implements Serializable {

    private transient AllDevicesReadCallback allDevicesReadCallback;
    private transient DeviceReadCallback deviceReadCallback;

    private XmlListDevice xmlListDevice;

    private OverviewViewSettings overviewViewSettings;

    public Device() {
        //Optimization to prevent the expensive calls to Annotations in getView()
        overviewViewSettings = getClass().getAnnotation(OverviewViewSettings.class);
        if (overviewViewSettings != null) {
            overviewViewSettings = new OverviewViewSettingsCache(overviewViewSettings);
        }
    }

    public void setXmlListDevice(XmlListDevice xmlListDevice) {
        this.xmlListDevice = xmlListDevice;
    }

    public OverviewViewSettings getOverviewViewSettings() {
        return overviewViewSettings;
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
     * Indicates whether the internal device state should be updated with the value given in state to set
     *
     * @param stateToSet state to set
     * @return true to update the internal state, else false
     */
    public boolean shouldUpdateStateOnDevice(String stateToSet) {
        return true;
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

    public boolean isSensorDevice() {
        return false;
    }

    /**
     * Trigger a state notification if a device attribute has changed via GCM
     *
     * @return true or false
     */
    public boolean triggerStateNotificationOnAttributeChange() {
        return false;
    }


    /**
     * Functionality of the device.
     *
     * @return NEVER null!
     */
    public abstract DeviceFunctionality getDeviceGroup();

    public boolean supportsWidget(Class<? extends DeviceAppWidgetView> appWidgetClass) {
        return true;
    }

    public XmlListDevice getXmlListDevice() {
        return xmlListDevice;
    }
}
