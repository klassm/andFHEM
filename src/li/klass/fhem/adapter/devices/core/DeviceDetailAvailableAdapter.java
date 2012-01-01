package li.klass.fhem.adapter.devices.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;

public abstract class DeviceDetailAvailableAdapter<D extends Device<D>> extends DeviceAdapter<D> {
    @Override
    public boolean supportsDetailView() {
        return true;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, getDeviceDetailActivity());
        return intent;
    }

    @Override
    protected final View getDeviceDetailView(Context context, D device) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(getDetailViewLayout(), null);
        fillDeviceDetailView(context, view, device);

        setTextViewOrHideTableRow(view, R.id.tableRowRoom, R.id.room, device.getRoom());
        setTextViewOrHideTableRow(view, R.id.tableRowMeasured, R.id.measured, device.getMeasured());

        hideIfNull(view, R.id.graphLayout, device.getFileLog());

        return view;
    }
    
    protected abstract void fillDeviceDetailView(Context context, View view, D device);
    protected abstract Class<? extends Activity> getDeviceDetailActivity();
}
