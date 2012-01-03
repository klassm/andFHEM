package li.klass.fhem.service.room;

import java.util.List;

public interface RoomListListener {
    void onRoomListRefresh(List<String> rooms);
}
