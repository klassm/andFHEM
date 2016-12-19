package li.klass.fhem.adapter.devices;

import android.content.Context;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.*;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.StateChangingTargetStateSelectedCallback;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;

public class StateChangeButtonActionRow extends ButtonActionRow {

    private final Context context;
    private final FhemDevice device;

    public StateChangeButtonActionRow(Context context, FhemDevice device, int layout) {
        super(context, R.string.set, layout);
        this.context = context;
        this.device = device;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onButtonClick() {
        AvailableTargetStatesDialogUtil.showSwitchOptionsMenuFor(
                context, device, new StateChangingTargetStateSelectedCallback(context, new StateUiService()), "state");
    }
}
