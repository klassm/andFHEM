package li.klass.fhem.devices.list.ui

import android.os.Parcelable
import androidx.lifecycle.ViewModel

class DeviceListFragmentViewModel : ViewModel() {
    var listState = mutableMapOf<String, Parcelable?>()

    fun setState(saveKey: String, state: Parcelable?) {
        listState[saveKey] = state
    }

    fun getState(saveKey: String) = listState[saveKey]
}