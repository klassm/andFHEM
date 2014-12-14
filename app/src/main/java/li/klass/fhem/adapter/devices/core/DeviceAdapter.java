/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.adapter.devices.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.activities.graph.ChartingActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceChart;
import li.klass.fhem.fragments.core.DeviceDetailFragment;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DeviceAdapter<D extends Device> {

    private Context context;
    private LayoutInflater inflater;

    protected DeviceAdapter() {
        AndFHEMApplication application = AndFHEMApplication.getApplication();
        if (application != null) {
            application.inject(this);
        }
    }

    /**
     * Indicates whether the current adapter supports the given device class.
     *
     * @param deviceClass class to check
     * @return true if device class is supported
     */
    public boolean supports(Class<? extends Device> deviceClass) {
        return getSupportedDeviceClass().isAssignableFrom(deviceClass);
    }

    public abstract Class<? extends Device> getSupportedDeviceClass();

    /**
     * Creates an overview view for the given device. The device has to match the adapter device type, otherwise
     * a cast exception occurs.
     *
     * @param layoutInflater layoutInflater to create the view
     * @param rawDevice      device used for filling the view
     * @return overview view
     */
    @SuppressWarnings("unchecked")
    public View createOverviewView(LayoutInflater layoutInflater, Device rawDevice, long lastUpdate) {
        checkNotNull(context);

        D device = (D) rawDevice;
        View view = layoutInflater.inflate(getOverviewLayout(device), null);
        fillDeviceOverviewView(view, device, lastUpdate);
        return view;
    }

    /**
     * Gets the overview layout id for the given device.
     *
     * @param device device
     * @return layout id
     */
    protected abstract int getOverviewLayout(D device);

    /**
     * Fills a given device view.
     *
     * @param view       view to fill
     * @param device     content provider
     * @param lastUpdate time when the data was last loaded from the FHEM server.
     */
    protected abstract void fillDeviceOverviewView(View view, D device, long lastUpdate);

    /**
     * Creates a filled detail view for a given device.
     *
     * @param context    context used for inflating the layout.
     * @param device     device used for filling.
     * @param lastUpdate time when the data was last loaded from the FHEM server.
     * @return filled view.
     */
    @SuppressWarnings("unchecked")
    public View createDetailView(Context context, Device device, long lastUpdate) {
        if (supportsDetailView(device)) {
            return getDeviceDetailView(context, (D) device, lastUpdate);
        }
        return null;
    }

    public abstract boolean supportsDetailView(Device device);

    protected abstract View getDeviceDetailView(Context context, D device, long lastUpdate);

    public void gotoDetailView(Context context, Device device) {
        if (!supportsDetailView(device)) {
            return;
        }

        Intent intent = new Intent(Actions.SHOW_FRAGMENT);
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.FRAGMENT_NAME, DeviceDetailFragment.class.getName());
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.ROOM_NAME, (String) device.getRooms().get(0));

        intent = onFillDeviceDetailIntent(context, device, intent);
        if (intent != null) {
            context.sendBroadcast(intent);
        }
    }

    protected abstract Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent);

    public abstract int getDetailViewLayout();

    protected void setTextViewOrHideTableRow(View view, int tableRowId, int textFieldLayoutId, String value) {
        TableRow tableRow = (TableRow) view.findViewById(tableRowId);

        if (hideIfNull(tableRow, value)) {
            return;
        }

        setTextView(view, textFieldLayoutId, value);
    }

    protected boolean hideIfNull(View layoutElement, Object valueToCheck) {
        if (valueToCheck == null || valueToCheck instanceof String && ((String) valueToCheck).length() == 0) {
            layoutElement.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    protected void setTextView(View view, int textFieldLayoutId, String value) {
        TextView textView = (TextView) view.findViewById(textFieldLayoutId);
        if (textView != null) {
            textView.setText(value);
        }
    }

    protected void setTextView(View view, int textFieldLayoutId, int value) {
        checkNotNull(view);

        Context context = view.getContext();
        checkNotNull(context);

        setTextView(view, textFieldLayoutId, context.getString(value));
    }

    protected void fillGraphButton(final Context context, final D device, final DeviceChart deviceChart, Button button) {
        if (deviceChart == null) return;

        button.setText(deviceChart.buttonText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChartingActivity.showChart(context, device, deviceChart.chartSeriesDescriptions);
            }
        });
    }

    public void attach(Context context) {
        checkNotNull(context);

        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Context getContext() {
        return context;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }
}
