package li.klass.fhem.room.list.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RoomListNavigationViewModel : ViewModel() {
    val selectedRoom: MutableLiveData<String> = MutableLiveData()
}