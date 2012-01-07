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

/**
 * Class accumulating all device actions like renaming, moving or deleting.
 */
public class DeviceService {

    public static final DeviceService INSTANCE = new DeviceService();

    private DeviceService() {
    }

    /**
     * Rename a device.
     * @param context context in which rename action was started.
     * @param device concerned device
     * @param newName new device name
     */
    public void renameDevice(final Context context, final Device device, final String newName) {
        CommandExecutionService.INSTANCE.executeSafely(context, "rename " + device.getName() + " " + newName, new ExecuteOnSuccess() {
            @Override
            public void onSuccess() {
                device.setName(newName);
                Toast.makeText(context, R.string.deviceRenameSuccess, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Deletes a device.
     * @param context context in which rename action was started.
     * @param device concerned device
     */
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

    /**
     * Sets an alias for a device.
     * @param context context in which rename action was started.
     * @param device concerned device
     * @param alias new alias to set
     */
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

    /**
     * Moves a device.
     * @param context context in which rename action was started.
     * @param device concerned device
     * @param newRoom new room to move the concerned device to.
     */
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
