package li.klass.fhem.appwidget.view.widget.small;

import li.klass.fhem.R;
import li.klass.fhem.fragments.FragmentType;

public class FavoritesLinkWidget extends SmallIconWidget {
    @Override
    protected FragmentType getFragment() {
        return FragmentType.FAVORITES;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.favorites;
    }

    @Override
    public int getWidgetName() {
        return R.string.widget_favorites_link;
    }
}
