package li.klass.fhem.adapter.devices;

import android.content.Context;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.AvailableTargetStatesDialogUtil;
import li.klass.fhem.adapter.devices.genericui.ButtonActionRow;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.StateChangingTargetStateSelectedCallback;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;

public class StateChangeButtonActionRow extends ButtonActionRow {

    private final Context context;
    private final FhemDevice device;
    private final String connectionId;

    public StateChangeButtonActionRow(Context context, FhemDevice device, int layout, String connectionId) {
        super(context, R.string.set, layout);
        this.context = context;
        this.device = device;
        this.connectionId = connectionId;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onButtonClick() {
        AvailableTargetStatesDialogUtil.showSwitchOptionsMenuFor(
                context, device, new StateChangingTargetStateSelectedCallback(context, new StateUiService(), connectionId), "state");
    }
}
