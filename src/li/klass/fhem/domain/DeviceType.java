/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.domain;

import li.klass.fhem.adapter.devices.*;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.domain.fht.FHT8VDevice;
import li.klass.fhem.fhem.ConnectionType;
import li.klass.fhem.util.ApplicationProperties;

public enum DeviceType {
    KS300("KS300", KS300Device.class),
    WEATHER("Weather", WeatherDevice.class, new WeatherAdapter()),
    FLOORPLAN("FLOORPLAN", FloorplanDevice.class, new FloorplanAdapter(), ConnectionType.FHEMWEB),
    FHT("FHT", FHTDevice.class, new FHTAdapter()),
    HMS("HMS", HMSDevice.class),
    WOL("WOL", WOLDevice.class, new WOLAdapter()),
    IT("IT", IntertechnoDevice.class, new IntertechnoAdapter()),
    OWTEMP("OWTEMP", OwtempDevice.class),
    CUL_FHTTK("CUL_FHTTK", CULFHTTKDevice.class),
    RFXX10REC("RFXX10REC", RFXX10RECDevice.class),
    OREGON("OREGON", OregonDevice.class),
    CUL_EM("CUL_EM", CULEMDevice.class),
    OWCOUNT("OWCOUNT", OwcountDevice.class),
    SIS_PMS("SIS_PMS", SISPMSDevice.class, new SISPMSAdapter()),
    USBWX("USBWX", USBWXDevice.class),
    CUL_WS("CUL_WS", CULWSDevice.class),
    FS20("FS20", FS20Device.class, new FS20Adapter()),
    FILE_LOG("FileLog", FileLogDevice.class, null, ConnectionType.NEVER),
    OWFS("OWFS", OWFSDevice.class),
    LGTV("LGTV", LGTVDevice.class),
    RFXCOM("RFXCOM", RFXCOMDevice.class),
    CUL_HM("CUL_HM", CULHMDevice.class, new CULHMAdapter()),
    WATCHDOG("watchdog", WatchdogDevice.class),
    HOLIDAY("HOL", HOLDevice.class, new HOLAdapter()),
    PID("PID", PIDDevice.class),
    FHT8V("FHT8V", FHT8VDevice.class),
    TRX_WEATHER("TRX_WEATHER", TRXWeatherDevice.class),
    TRX("TRX", TRXDevice.class),
    DUMMY("dummy", DummyDevice.class, new DummyAdapter());

    private String xmllistTag;
    private Class<? extends Device> deviceClass;
    private DeviceAdapter<? extends Device<?>> adapter;
    private ConnectionType showDeviceOnlyInConnection = null;

    <T extends Device<T>> DeviceType(String xmllistTag, Class<T> deviceClass) {
        this(xmllistTag, deviceClass, new GenericDeviceAdapter<T>(deviceClass));
    }

    DeviceType(String xmllistTag, Class<? extends Device> deviceClass, DeviceAdapter<? extends Device<?>> adapter) {
        this.xmllistTag = xmllistTag;
        this.deviceClass = deviceClass;
        this.adapter = adapter;
    }

    DeviceType(String xmllistTag, Class<? extends Device> deviceClass, DeviceAdapter<? extends Device<?>> adapter, ConnectionType showConnectionOnlyIn) {
        this(xmllistTag, deviceClass, adapter);
        showDeviceOnlyInConnection = showConnectionOnlyIn;
    }

    public String getXmllistTag() {
        return xmllistTag;
    }

    public Class<? extends Device> getDeviceClass() {
        return deviceClass;
    }

    @SuppressWarnings("unchecked")
    public <T extends Device> DeviceAdapter<T> getAdapter() {
        return (DeviceAdapter<T>) adapter;
    }

    public boolean mayShowInCurrentConnectionType() {
        if (showDeviceOnlyInConnection == null) return true;

        ConnectionType connectionType = ApplicationProperties.INSTANCE.getConnectionType();
        return connectionType == showDeviceOnlyInConnection;
    }

    public static <T extends Device> DeviceAdapter<T> getAdapterFor(T device) {
        DeviceType deviceType = getDeviceTypeFor(device);
        if (deviceType == null) {
            return null;
        } else {
            return deviceType.getAdapter();
        }
    }

    public static <T extends Device> DeviceType getDeviceTypeFor(T device) {
        if (device == null) return null;

        DeviceType[] deviceTypes = DeviceType.values();
        for (DeviceType deviceType : deviceTypes) {
            if (deviceType.getDeviceClass().isAssignableFrom(device.getClass())) {
                return deviceType;
            }
        }
        return null;
    }
}