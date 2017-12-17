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
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Set;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.graph.backend.gplot.SvgGraphDefinition;
import li.klass.fhem.graph.ui.GraphActivity;
import li.klass.fhem.ui.FragmentType;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DeviceAdapter {

    protected DeviceAdapter() {
        AndFHEMApplication application = AndFHEMApplication.Companion.getApplication();
        if (application != null) {
            inject(application.getDaggerComponent());
            onAfterInject();
        }
    }

    protected void onAfterInject() {
    }

    protected abstract void inject(ApplicationComponent daggerComponent);

    /**
     * Indicates whether the current adapter supports the given device class.
     *
     * @param deviceClass class to check
     * @return true if device class is supported
     */
    public boolean supports(Class<? extends FhemDevice> deviceClass) {
        return getSupportedDeviceClass().isAssignableFrom(deviceClass);
    }

    public abstract Class<? extends FhemDevice> getSupportedDeviceClass();

    /**
     * Creates an overview view for the given device. The device has to match the adapter device type, otherwise
     * a cast exception occurs.
     *  @param convertView    the view that can be reused
     * @param rawDevice      device used for filling the view  @return overview view
     * @param context*/
    @SuppressWarnings("unchecked")
    public abstract View createOverviewView(View convertView, FhemDevice rawDevice, Context context);

    /**
     * Creates a filled detail view for a given device.
     * @param context    context used for inflating the layout.
     * @param device     device used for filling.
     * @param grapDefinitions
     * @param connectionId
     */
    @SuppressWarnings("unchecked")
    public View createDetailView(Context context, FhemDevice device, Set<SvgGraphDefinition> grapDefinitions, String connectionId) {
        if (supportsDetailView(device)) {
            return getDeviceDetailView(context, device, grapDefinitions, connectionId);
        }
        return null;
    }

    public abstract boolean supportsDetailView(FhemDevice device);

    protected abstract View getDeviceDetailView(Context context, FhemDevice device, Set<SvgGraphDefinition> graphDefinitions, String connectionId);

    public void gotoDetailView(Context context, FhemDevice device) {
        if (!supportsDetailView(device)) {
            return;
        }

        Intent intent = new Intent(Actions.SHOW_FRAGMENT)
                .putExtra(BundleExtraKeys.FRAGMENT, FragmentType.DEVICE_DETAIL)
                .putExtra(BundleExtraKeys.DEVICE_NAME, device.getName())
                .putExtra(BundleExtraKeys.ROOM_NAME, device.getRooms().get(0))
                .putExtra(BundleExtraKeys.DEVICE_DISPLAY_NAME, device.getAliasOrName());

        intent = onFillDeviceDetailIntent(context, device, intent);
        if (intent != null) {
            context.sendBroadcast(intent);
        }
    }

    protected abstract Intent onFillDeviceDetailIntent(Context context, FhemDevice device, Intent intent);

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

    protected void setTextView(TextView textView, String value) {
        CharSequence toSet = value.contains("<") ? Html.fromHtml(value) : value;
        if (textView != null) {
            textView.setText(toSet);
        }
    }

    protected void setTextView(View view, int textFieldLayoutId, int value) {
        checkNotNull(view);

        Context context = view.getContext();
        checkNotNull(context);

        setTextView(view, textFieldLayoutId, context.getString(value));
    }

    protected void fillGraphButton(final Context context, final FhemDevice device, final String connectionId, final SvgGraphDefinition svgGraphDefinition, Button button) {
        if (svgGraphDefinition == null) return;

        button.setText(svgGraphDefinition.getName());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GraphActivity.Companion.showChart(context, device, connectionId, svgGraphDefinition);
            }
        });
    }

    public abstract void attachGraphs(Context context, View detailView, Set<SvgGraphDefinition> graphDefinitions, String connectionId, FhemDevice device);

    public boolean loadGraphs() {
        return true;
    }
}