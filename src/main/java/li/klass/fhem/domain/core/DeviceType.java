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

import java.util.Locale;
import java.util.Map;

import li.klass.fhem.adapter.devices.FS20ZDRDeviceAdapter;
import li.klass.fhem.adapter.devices.FloorplanAdapter;
import li.klass.fhem.adapter.devices.GCMSendDeviceAdapter;
import li.klass.fhem.adapter.devices.HarmonyDeviceAdapter;
import li.klass.fhem.adapter.devices.PioneerAvrZoneDeviceAdapter;
import li.klass.fhem.adapter.devices.RemoteControlAdapter;
import li.klass.fhem.adapter.devices.SBPlayerDeviceAdapter;
import li.klass.fhem.adapter.devices.SonosPlayerAdapter;
import li.klass.fhem.adapter.devices.WeatherAdapter;
import li.klass.fhem.adapter.devices.WebLinkAdapter;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow;
import li.klass.fhem.adapter.devices.core.GenericOverviewDetailDeviceAdapter;
import li.klass.fhem.adapter.devices.core.ToggleableAdapter;
import li.klass.fhem.domain.AtDevice;
import li.klass.fhem.domain.FBCallmonitorDevice;
import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.FS20ZDRDevice;
import li.klass.fhem.domain.FloorplanDevice;
import li.klass.fhem.domain.GCMSendDevice;
import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.domain.HCSDevice;
import li.klass.fhem.domain.HarmonyDevice;
import li.klass.fhem.domain.OwDevice;
import li.klass.fhem.domain.PioneerAvrZoneDevice;
import li.klass.fhem.domain.RemoteControlDevice;
import li.klass.fhem.domain.SBPlayerDevice;
import li.klass.fhem.domain.SonosPlayerDevice;
import li.klass.fhem.domain.WeatherDevice;
import li.klass.fhem.domain.WebLinkDevice;
import li.klass.fhem.update.backend.xmllist.XmlListDevice;

import static com.google.common.collect.Maps.newHashMap;

public enum DeviceType {

    WEATHER("Weather", WeatherDevice.class, new WeatherAdapter()),
    FLOORPLAN("FLOORPLAN", FloorplanDevice.class, new FloorplanAdapter(), DeviceVisibility.FHEMWEB_ONLY),
    AT("at", AtDevice.class, null),
    HCS("HCS", HCSDevice.class, new ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow()),
    OWDEVICE("OWDevice", OwDevice.class, new ToggleableAdapter()),
    SONOS_PLAYER("SONOSPLAYER", SonosPlayerDevice.class, new SonosPlayerAdapter()),
    GCM_SEND("gcmsend", GCMSendDevice.class, new GCMSendDeviceAdapter()),
    FB_CALLMONITOR("FB_CALLMONITOR", FBCallmonitorDevice.class),
    FS20_ZDR("fs20_zdr", FS20ZDRDevice.class, new FS20ZDRDeviceAdapter()),
    REMOTECONTROL("remotecontrol", RemoteControlDevice.class, new RemoteControlAdapter(), DeviceVisibility.FHEMWEB_ONLY),
    WEB_LINK("weblink", WebLinkDevice.class, new WebLinkAdapter()),
    FHEMWEB("FHEMWEB", FHEMWEBDevice.class),
    PIONEERAVRZONE("PIONEERAVRZONE", PioneerAvrZoneDevice.class, new PioneerAvrZoneDeviceAdapter()),
    SB_PLAYER("SB_PLAYER", SBPlayerDevice.class, new SBPlayerDeviceAdapter()),
    HARMONY("harmony", HarmonyDevice.class, new HarmonyDeviceAdapter()),

    GENERIC("__generic__", GenericDevice.class, new GenericOverviewDetailDeviceAdapter());

    private static final Map<Class<?>, DeviceType> DEVICE_TO_DEVICE_TYPE = newHashMap();
    private static final Map<String, DeviceType> TAG_TO_DEVICE_TYPE = newHashMap();

    static {
        for (DeviceType deviceType : values()) {
            DEVICE_TO_DEVICE_TYPE.put(deviceType.getDeviceClass(), deviceType);
            TAG_TO_DEVICE_TYPE.put(deviceType.getXmllistTag().toLowerCase(Locale.getDefault()), deviceType);
        }
    }

    private String xmllistTag;
    private Class<? extends FhemDevice> deviceClass;
    private DeviceAdapter adapter;
    private DeviceVisibility visibility = null;

    <T extends FhemDevice> DeviceType(String xmllistTag, Class<T> deviceClass) {
        this(xmllistTag, deviceClass, new ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow());
    }

    DeviceType(String xmllistTag, Class<? extends FhemDevice> deviceClass, DeviceAdapter adapter) {
        this.xmllistTag = xmllistTag;
        this.deviceClass = deviceClass;
        this.adapter = adapter;
    }

    DeviceType(String xmllistTag, Class<? extends FhemDevice> deviceClass, DeviceAdapter adapter, DeviceVisibility visibility) {
        this(xmllistTag, deviceClass, adapter);
        this.visibility = visibility;
    }

    public static <T extends FhemDevice> DeviceAdapter getAdapterFor(T device) {
        DeviceType type = getDeviceTypeFor(device);
        return type == null ? null : type.getAdapter();
    }

    public static <T extends FhemDevice> DeviceType getDeviceTypeFor(T device) {
        if (device == null) return null;
        XmlListDevice xmlListDevice = device.getXmlListDevice();
        if (xmlListDevice == null) return null;
        return getDeviceTypeFor(xmlListDevice.getType());
    }

    @SuppressWarnings("unchecked")
    public DeviceAdapter getAdapter() {
        return adapter;
    }

    public static <T extends FhemDevice> DeviceType getDeviceTypeFor(Class<T> clazz) {
        DeviceType result = DEVICE_TO_DEVICE_TYPE.get(clazz);
        return result == null ? GENERIC : result;
    }

    public static DeviceType getDeviceTypeFor(String xmllistTag) {
        DeviceType result = TAG_TO_DEVICE_TYPE.get(xmllistTag.toLowerCase(Locale.getDefault()));
        return result == null ? GENERIC : result;
    }

    public Class<? extends FhemDevice> getDeviceClass() {
        return deviceClass;
    }

    public String getXmllistTag() {
        return xmllistTag;
    }

    public DeviceVisibility getVisibility() {
        return visibility;
    }
}