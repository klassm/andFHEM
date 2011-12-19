package li.klass.fhem.adapter.devices;

import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static li.klass.fhem.domain.DeviceType.*;

public class DeviceAdapterProvider {
    public static final DeviceAdapterProvider INSTANCE = new DeviceAdapterProvider();

    private Map<DeviceType, DeviceAdapter<? extends Device<?>>> deviceAdapters = new HashMap<DeviceType, DeviceAdapter<? extends Device<?>>>();

    private DeviceAdapterProvider() {
        deviceAdapters = new HashMap<DeviceType, DeviceAdapter<? extends Device<?>>>();
        deviceAdapters.put(FS20, new FS20Adapter());
        deviceAdapters.put(CUL_WS, new CULWSAdapter());
        deviceAdapters.put(HMS, new HMSAdapter());
        deviceAdapters.put(OWTEMP, new OwtempAdapter());
        deviceAdapters.put(KS300, new KS300Adapter());
        deviceAdapters.put(FHT, new FHTAdapter());
        deviceAdapters.put(SIS_PMS, new SISPMSAdapter());
        deviceAdapters.put(CUL_FHTTK, new CULFHTTKAdapter());
    }
    
    public Collection<DeviceAdapter<? extends Device<?>>> getAllAdapters() {
        return Collections.unmodifiableCollection(deviceAdapters.values());
    }

    public DeviceAdapter<? extends Device<?>> getAdapterFor(Device device) {
        return getAdapterFor(device.getDeviceType());
    }

    public DeviceAdapter<? extends Device<?>> getAdapterFor(DeviceType deviceType) {
        return deviceAdapters.get(deviceType);
    }
}
