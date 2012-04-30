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

import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.adapter.devices.*;
import li.klass.fhem.adapter.devices.core.HOLDevice;
import li.klass.fhem.domain.fht.FHT8VDevice;

public enum DeviceType {
    KS300("KS300", KS300Device.class, new KS300Adapter()),
    WEATHER("Weather", WeatherDevice.class, new WeatherAdapter()),
    FHT("FHT", FHTDevice.class, new FHTAdapter()),
    HMS("HMS", HMSDevice.class, new HMSAdapter()),
    WOL("WOL", WOLDevice.class, new WOLAdapter()),
    OWTEMP("OWTEMP", OwtempDevice.class, new OwtempAdapter()),
    CUL_FHTTK("CUL_FHTTK", CULFHTTKDevice.class, new CULFHTTKAdapter()),
    RFXX10REC("RFXX10REC", RFXX10RECDevice.class, new RFXX10RECAdapter()),
    OREGON("OREGON", OregonDevice.class, new OregonAdapter()),
    CUL_EM("CUL_EM", CULEMDevice.class, new CULEMAdapter()),
    OWCOUNT("OWCOUNT", OwcountDevice.class, new OwcountAdapter()),
    SIS_PMS("SIS_PMS", SISPMSDevice.class, new SISPMSAdapter()),
    USBWX("USBWX", USBWXDevice.class, new USBWXAdapter()),
    CUL_WS("CUL_WS", CULWSDevice.class, new CULWSAdapter()),
    FS20("FS20", FS20Device.class, new FS20Adapter()),
    FILE_LOG("FileLog", FileLogDevice.class, null, false),
    OWFS("OWFS", OWFSDevice.class, new OWFSAdapter()),
    LGTV("LGTV", LGTVDevice.class, new LGTVAdapter()),
    RFXCOM("RFXCOM", RFXCOMDevice.class, new RFXCOMAdapter()),
    CUL_HM("CUL_HM", CULHMDevice.class, new CULHMAdapter()),
    WATCHDOG("watchdog", WatchdogDevice.class, new WatchdogAdapter()),
    HOLIDAY("HOL", HOLDevice.class, new HOLAdapter()),
    PID("PID", PIDDevice.class, new PIDAdapter()),
    FHT8V("FHT8V", FHT8VDevice.class, new FHT8VAdapter()),
    TRX_WEATHER("TRX_WEATHER", TRXWeatherDevice.class, new TRXWeatherAdapter()),
    TRX("TRX", TRXDevice.class, new TRXAdapter());

    private String xmllistTag;
    private Class<? extends Device> deviceClass;
    private DeviceAdapter<? extends Device<?>> adapter;
    private boolean doShowInDeviceList = true;

    DeviceType(String xmllistTag, Class<? extends Device> deviceClass, DeviceAdapter<? extends Device<?>> adapter) {
        this.xmllistTag = xmllistTag;
        this.deviceClass = deviceClass;
        this.adapter = adapter;
    }

    DeviceType(String xmllistTag, Class<? extends Device> deviceClass, DeviceAdapter<? extends Device<?>> adapter, boolean doShow) {
        this.xmllistTag = xmllistTag;
        this.deviceClass = deviceClass;
        this.adapter = adapter;
        doShowInDeviceList = doShow;
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

    public boolean isDoShowInDeviceList() {
        return doShowInDeviceList;
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