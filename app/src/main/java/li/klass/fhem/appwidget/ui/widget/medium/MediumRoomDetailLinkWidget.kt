package li.klass.fhem.appwidget.ui.widget.medium

import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.base.RoomDetailLinkWidget
import javax.inject.Inject

class MediumRoomDetailLinkWidget @Inject constructor() : RoomDetailLinkWidget() {

    override val widgetSize = WidgetSize.MEDIUM

    override fun getContentView(): Int = R.layout.appwidget_room_link_medium

    override val widgetType = WidgetType.ROOM_DETAIL_LINK
}