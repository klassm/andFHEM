package li.klass.fhem.service;

import android.content.Context;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.activities.CurrentActivityProvider;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;

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

    public void moveDevice(final Context context, final Device device, final String newRoom) {
        FHEMService.INSTANCE.executeSafely(context, "attr " + device.getName() + " room " + newRoom, new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {
                String oldRoom = device.getRoom();
                device.setRoom(newRoom);

                RoomDeviceList oldRoomDeviceList = RoomListService.INSTANCE.deviceListForRoom(oldRoom, false);
                oldRoomDeviceList.removeDevice(device);

                RoomDeviceList newRoomList = RoomListService.INSTANCE.getOrCreateDeviceListForRoom(newRoom, false);
                newRoomList.addDevice(device);

                if (oldRoomDeviceList.getAllDevices().size() == 0) {
                    RoomListService.INSTANCE.removeDeviceListForRoom(oldRoom);
                }


                CurrentActivityProvider.INSTANCE.getCurrentActivity().update(false);
                Toast.makeText(context, R.string.deviceMoveSuccess, Toast.LENGTH_LONG).show();
            }
        });

    }
}
