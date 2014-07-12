package li.klass.fhem.appwidget.view.widget.small;

import li.klass.fhem.R;
import li.klass.fhem.fragments.FragmentType;

public class AllDevicesLinkWidget extends SmallIconWidget {
    @Override
    protected FragmentType getFragment() {
        return FragmentType.ALL_DEVICES;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.all_devices;
    }

    @Override
    public int getWidgetName() {
        return R.string.widget_all_devices_link;
    }
}
