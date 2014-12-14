package li.klass.fhem.appwidget.view.widget.small;

import li.klass.fhem.R;
import li.klass.fhem.fragments.FragmentType;

public class RoomsLinkWidget extends SmallIconWidget {
    @Override
    protected FragmentType getFragment() {
        return FragmentType.ROOM_LIST;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.room_list;
    }

    @Override
    public int getWidgetName() {
        return R.string.widget_room_link;
    }
}
