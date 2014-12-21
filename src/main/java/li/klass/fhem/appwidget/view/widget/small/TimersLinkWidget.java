package li.klass.fhem.appwidget.view.widget.small;

import li.klass.fhem.R;
import li.klass.fhem.fragments.FragmentType;

public class TimersLinkWidget extends SmallIconWidget {
    @Override
    protected FragmentType getFragment() {
        return FragmentType.TIMER_OVERVIEW;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.timer;
    }

    @Override
    public int getWidgetName() {
        return R.string.widget_timer_link;
    }
}
