package li.klass.fhem.appwidget.ui.selection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView
import li.klass.fhem.domain.core.FhemDevice

class WidgetSelectionViewModel : ViewModel() {
    var widgetSize: WidgetSize = WidgetSize.MEDIUM

    val deviceClicked: MutableLiveData<FhemDevice> = MutableLiveData()
    val otherWidgetClicked: MutableLiveData<AppWidgetView> = MutableLiveData()
    val roomClicked: MutableLiveData<String> = MutableLiveData()
}