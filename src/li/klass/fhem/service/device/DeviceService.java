package li.klass.fhem.service.device;

import android.content.Context;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.activities.CurrentActivityProvider;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.ExecuteOnSuccess;
import li.klass.fhem.service.room.RoomDeviceListListener;
import li.klass.fhem.service.room.RoomListService;

public class DeviceService {
    public static final DeviceService INSTANCE = new DeviceService();

    private DeviceService() {
    }

    public void renameDevice(final Context context, final Device device, final String newName) {
        CommandExecutionService.INSTANCE.executeSafely(context, "rename " + device.getName() + " " + newName, new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {
                device.setName(newName);
                Toast.makeText(context, R.string.deviceRenameSuccess, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void deleteDevice(final Context context, final Device device) {
        CommandExecutionService.INSTANCE.executeSafely(context, "delete " + device.getName(), new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {

                RoomDeviceListListener deleteListener = new RoomDeviceListListener() {

                    @Override
                    public void onRoomListRefresh(RoomDeviceList roomDeviceList) {
                        roomDeviceList.removeDevice(device);
                    }
                };
                RoomListService.INSTANCE.getRoomDeviceList(context, device.getRoom(), false, deleteListener);
                RoomListService.INSTANCE.getAllRoomsDeviceList(context, false, deleteListener);
                Toast.makeText(context, R.string.deviceDeleteSuccess, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    

    public void setAlias(final Context context, final Device device, final String alias) {
        CommandExecutionService.INSTANCE.executeSafely(context, "attr " + device.getName() + " alias " + alias, new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {
                device.setAlias(alias);
                CurrentActivityProvider.INSTANCE.getCurrentActivity().update(false);
                Toast.makeText(context, R.string.deviceAliasSuccess, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void moveDevice(final Context context, final Device device, final String newRoom) {
        CommandExecutionService.INSTANCE.executeSafely(context, "attr " + device.getName() + " room " + newRoom, new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {
                final String oldRoom = device.getRoom();
                device.setRoom(newRoom);

                RoomListService.INSTANCE.getRoomDeviceList(context, oldRoom, false, new RoomDeviceListListener() {

                    @Override
                    public void onRoomListRefresh(RoomDeviceList oldRoomDeviceList) {
                        oldRoomDeviceList.removeDevice(device);
                        if (oldRoomDeviceList.getAllDevices().size() == 0) {
                            RoomListService.INSTANCE.removeDeviceListForRoom(context, oldRoom);
                            CurrentActivityProvider.INSTANCE.getCurrentActivity().update(false);
                            Toast.makeText(context, R.string.deviceMoveSuccess, Toast.LENGTH_LONG).show();
                        }
                    }
                });

                RoomListService.INSTANCE.getOrCreateRoomDeviceList(context, newRoom, false, new RoomDeviceListListener() {
                    @Override
                    public void onRoomListRefresh(RoomDeviceList newRoomDeviceList) {
                        newRoomDeviceList.addDevice(device);
                        CurrentActivityProvider.INSTANCE.getCurrentActivity().update(false);
                    }
                });
            }
        });

    }
}
