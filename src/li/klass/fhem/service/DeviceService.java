package li.klass.fhem.service;

import android.content.Context;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;

public class DeviceService {
    public static final DeviceService INSTANCE = new DeviceService();

    private DeviceService() {
    }

    public void renameDevice(final Context context, final Device device, final String newName) {
        FHEMService.INSTANCE.executeSafely(context, "rename " + device.getName() + " " + newName, new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {
                device.setName(newName);
                Toast.makeText(context, R.string.deviceRenameSuccess, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void deleteDevice(final Context context, final Device device) {
        FHEMService.INSTANCE.executeSafely(context, "delete " + device.getName(), new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {
                RoomListService.INSTANCE.deviceListForAllRooms(false).removeDevice(device);
                RoomListService.INSTANCE.deviceListForRoom(device.getRoom(), false).removeDevice(device);
                Toast.makeText(context, R.string.deviceDeleteSuccess, Toast.LENGTH_LONG).show();
            }
        });
    }
}
