package li.klass.fhem.adapter.devices.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.activities.graph.ChartingActivity;
import li.klass.fhem.domain.Device;

public abstract class DeviceAdapter<D extends Device> {


    public static final String INTENT_DEVICE_NAME = "deviceName";
    public static final String INTENT_ROOM = "room";

    public boolean supports(Class<? extends Device> deviceClass) {
        return getSupportedDeviceClass().isAssignableFrom(deviceClass);
    }

    @SuppressWarnings("unchecked")
    public View getView(LayoutInflater layoutInflater, View convertView, Device device) {
        if (convertView != null) {
            TextView deviceName = (TextView) convertView.findViewById(R.id.deviceName);
            if (deviceName != null && deviceName.getText().equals(device.getAliasOrName())) {
                return convertView;
            }
        }
        return getDeviceView(layoutInflater, (D) device);
    }

    @SuppressWarnings("unchecked")
    public View getDetailView(Context context, Device device) {
        if (supportsDetailView()) {
            return getDeviceDetailView(context, (D) device);
        }
        return null;
    }

    public void gotoDetailView(Context context, Device device) {
        if (! supportsDetailView()) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtras(new Bundle());
        intent.putExtra(INTENT_DEVICE_NAME, device.getName());
        intent.putExtra(INTENT_ROOM, device.getRoom());

        intent = onFillDeviceDetailIntent(context, device, intent);
        if (intent != null) {
            context.startActivity(intent);
        }
    }


    public abstract int getDetailViewLayout();
    public abstract boolean supportsDetailView();
    protected abstract View getDeviceDetailView(Context context, D device);
    protected abstract Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent);

    public abstract Class<? extends Device> getSupportedDeviceClass();
    protected abstract View getDeviceView(LayoutInflater layoutInflater, D device);


    protected void setTextViewOrHideTableRow(View view, int tableRowId, int textFieldLayoutId, String value) {
        TableRow tableRow = (TableRow) view.findViewById(tableRowId);

        if (hideIfNull(tableRow, value)) {
            return;
        }

        setTextView(view, textFieldLayoutId, value);
    }

    protected void setTextView(View view, int textFieldLayoutId, String value) {
        TextView textView = (TextView) view.findViewById(textFieldLayoutId);
        if (textView != null) {
            textView.setText(value);
        }
    }

    protected boolean hideIfNull(View view, int id, Object valueToCheck) {
        View layoutElement = view.findViewById(id);
        return layoutElement != null && hideIfNull(layoutElement, valueToCheck);
    }

    protected boolean hideIfNull(View layoutElement, Object valueToCheck) {
        if (valueToCheck == null || valueToCheck instanceof String && ((String) valueToCheck).length() == 0) {
            layoutElement.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    protected boolean createPlotButton(final Context context, View view, int buttonLayoutId, Object hideButtonIfNull,
                                       final D device, final int yTitleId, final int columnSpec) {

        if (! hideIfNull(view, buttonLayoutId, hideButtonIfNull)) {
            Button button = (Button) view.findViewById(buttonLayoutId);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String yTitle = context.getResources().getString(yTitleId);
                    ChartingActivity.showChart(context, device, yTitle, columnSpec);
                }
            });

            return false;
        } else {
            return true;
        }
    }

}
