package li.klass.fhem.domain;

import li.klass.fhem.adapter.devices.*;

public enum DeviceType {
    FS20("FS20", FS20Device.class, new FS20Adapter()),
    FHT("FHT", FHTDevice.class, new FHTAdapter()),
    KS300("KS300", KS300Device.class, new KS300Adapter()),
    HMS("HMS", HMSDevice.class, new HMSAdapter()),
    OWTEMP("OWTEMP", OwtempDevice.class, new OwtempAdapter()),
    CUL_WS("CUL_WS", CULWSDevice.class, new CULWSAdapter()),
    SIS_PMS("SIS_PMS", SISPMSDevice.class, new SISPMSAdapter()),
    CUL_FHTTK("CUL_FHTTK", CULFHTTKDevice.class, new CULFHTTKAdapter()),
    FILE_LOG("FileLog", FileLog.class, null),
    RFXX10REC("RFXX10REC", RFXX10RECDevice.class, new RFXX10RECAdapter()),
    OREGON("OREGON", OregonDevice.class, new OregonAdapter()),
    USBWX("USBWX", USBWXDevice.class, new USBWXAdapter());

    
    private String xmllistTag;
    private Class<? extends Device> deviceClass;
    private DeviceAdapter<? extends Device<?>> adapter;

    DeviceType(String xmllistTag, Class<? extends Device> deviceClass, DeviceAdapter<? extends Device<?>> adapter) {
        this.xmllistTag = xmllistTag;
        this.deviceClass = deviceClass;
        this.adapter = adapter;
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
}