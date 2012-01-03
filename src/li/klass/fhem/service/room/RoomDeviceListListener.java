package li.klass.fhem.service.room;

import li.klass.fhem.domain.RoomDeviceList;

public interface RoomDeviceListListener {
    void onRoomListRefresh(RoomDeviceList roomDeviceList);
}
