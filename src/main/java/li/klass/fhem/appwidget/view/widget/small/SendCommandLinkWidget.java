package li.klass.fhem.appwidget.view.widget.small;

import li.klass.fhem.R;
import li.klass.fhem.fragments.FragmentType;

public class SendCommandLinkWidget extends SmallIconWidget {
    @Override
    protected FragmentType getFragment() {
        return FragmentType.SEND_COMMAND;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.send_command;
    }

    @Override
    public int getWidgetName() {
        return R.string.widget_send_command_link;
    }
}
