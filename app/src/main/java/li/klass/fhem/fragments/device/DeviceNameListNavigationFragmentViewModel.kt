package li.klass.fhem.fragments.device

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DeviceNameListNavigationFragmentViewModel : ViewModel() {
    val selectedDevice: MutableLiveData<String> = MutableLiveData()
}