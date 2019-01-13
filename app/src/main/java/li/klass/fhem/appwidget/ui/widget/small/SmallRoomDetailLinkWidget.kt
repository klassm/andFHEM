package li.klass.fhem.appwidget.ui.widget.small

import li.klass.fhem.R
import li.klass.fhem.appwidget.ui.widget.WidgetSize
import li.klass.fhem.appwidget.ui.widget.WidgetType
import li.klass.fhem.appwidget.ui.widget.base.RoomDetailLinkWidget
import javax.inject.Inject

class SmallRoomDetailLinkWidget @Inject constructor() : RoomDetailLinkWidget() {

    override val widgetSize = WidgetSize.SMALL

    override fun getContentView(): Int = R.layout.appwidget_room_link_small

    override val widgetType = WidgetType.ROOM_DETAIL_LINK_SMALL
}