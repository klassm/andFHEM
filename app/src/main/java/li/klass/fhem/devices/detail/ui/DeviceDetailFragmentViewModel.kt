package li.klass.fhem.devices.detail.ui

import androidx.lifecycle.ViewModel

interface ExpandHandler {
    fun isExpanded(key: String): Boolean
    fun setExpanded(key: String, expanded: Boolean)
}

class DeviceDetailFragmentViewModel : ViewModel() {
    private val expandedCards = mutableMapOf<String, Boolean>()

    fun expandHandler(): ExpandHandler = object : ExpandHandler {
        override fun isExpanded(key: String): Boolean = expandedCards[key] ?: false

        override fun setExpanded(key: String, expanded: Boolean) {
            expandedCards[key] = expanded
        }
    }

    fun reset() {
        expandedCards.clear()
    }
}