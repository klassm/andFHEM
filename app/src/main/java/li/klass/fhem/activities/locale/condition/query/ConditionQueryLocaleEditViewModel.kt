package li.klass.fhem.activities.locale.condition.query

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConditionQueryLocaleEditViewModel : ViewModel() {
    val deviceName = MutableLiveData<String>()
    val attributeValue = MutableLiveData<String>()
    val attributeName = MutableLiveData<String>()
    val attributeType = MutableLiveData<String>()
    val allAttributesSet = MediatorLiveData<Boolean>()

    init {
        allAttributesSet.addSource(deviceName) { checkAllSet() }
        allAttributesSet.addSource(attributeValue) { checkAllSet() }
        allAttributesSet.addSource(attributeName) { checkAllSet() }
        allAttributesSet.addSource(attributeType) { checkAllSet() }
    }

    private fun checkAllSet() {
        allAttributesSet.value = deviceName.value?.isNotBlank() == true &&
                attributeValue.value?.isNotBlank() == true &&
                attributeName.value?.isNotBlank() == true &&
                attributeType.value?.isNotBlank() == true
    }

}