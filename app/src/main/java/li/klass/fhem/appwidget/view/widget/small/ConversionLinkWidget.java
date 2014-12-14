package li.klass.fhem.appwidget.view.widget.small;

import li.klass.fhem.R;
import li.klass.fhem.fragments.FragmentType;

public class ConversionLinkWidget extends SmallIconWidget {
    @Override
    protected FragmentType getFragment() {
        return FragmentType.CONVERSION;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.conversion;
    }

    @Override
    public int getWidgetName() {
        return R.string.widget_conversion_link;
    }
}
