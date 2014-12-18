package li.klass.fhem.adapter.devices.core;

import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.util.List;

import li.klass.fhem.R;

public class GenericDeviceOverviewViewHolder {

    public GenericDeviceOverviewViewHolder(View convertView) {
        tableLayout = (TableLayout) convertView.findViewById(R.id.device_overview_generic);
        deviceName = (TextView) convertView.findViewById(R.id.deviceName);
        deviceNameRow = (TableRow) convertView.findViewById(R.id.overviewRow);
    }

    public static class GenericDeviceTableRowHolder {
        TableRow row;
        TextView description;
        TextView value;

    }

    TableLayout tableLayout;
    TableRow deviceNameRow;
    TextView deviceName;
    List<GenericDeviceTableRowHolder> tableRows = Lists.newArrayList() ;

    public void resetHolder() {
        deviceName.setVisibility(View.VISIBLE);
        tableLayout.removeAllViews();
        tableLayout.addView(deviceNameRow);
    }


}
