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
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.common.base.Optional;

import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow;
import li.klass.fhem.adapter.devices.genericui.onoff.OnOffActionRowForToggleables;
import li.klass.fhem.adapter.devices.hook.ButtonHook;
import li.klass.fhem.adapter.devices.hook.DeviceHookProvider;
import li.klass.fhem.adapter.devices.strategy.ToggleableStrategy;
import li.klass.fhem.adapter.devices.strategy.ViewStrategy;
import li.klass.fhem.adapter.devices.strategy.WebcmdStrategy;
import li.klass.fhem.adapter.devices.toggle.OnOffBehavior;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.ToggleableDevice;

import static li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener.NotificationDeviceType.TOGGLEABLE_AND_NOT_DIMMABLE;
import static li.klass.fhem.adapter.devices.genericui.ToggleDeviceActionRow.LAYOUT_DETAIL;

public class ToggleableAdapter extends ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow {
    @Inject
    ToggleableStrategy toggleableOverviewStrategy;
    @Inject
    WebcmdStrategy webcmdStrategy;

    @Inject
    DeviceHookProvider deviceHookProvider;

    @Inject
    OnOffBehavior onOffBehavior;

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return ToggleableDevice.class;
    }

    @SuppressWarnings("unchecked")
    private <T extends ToggleableDevice<T>> void addToggleDeviceActionRow(Context context, T device,
                                                                          TableLayout tableLayout, int layoutId) {
        tableLayout.addView(new ToggleDeviceActionRow(getInflater(), layoutId, onOffBehavior)
                .createRow(context, device, device.getAliasOrName()));
    }

    private <T extends ToggleableDevice<T>> void addOnOffActionRow(Context context, T device, TableLayout tableLayout, int layoutId, String connectionId) {
        tableLayout.addView(new OnOffActionRowForToggleables(layoutId, deviceHookProvider, onOffBehavior, Optional.of(R.string.blank), connectionId)
                .createRow(getInflater(), device, context));
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();
        registerFieldListener("state", new FieldNameAddedToDetailListener(TOGGLEABLE_AND_NOT_DIMMABLE) {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, String connectionId, TableRow fieldTableRow) {
                if (!(device instanceof ToggleableDevice) || !((ToggleableDevice) device).supportsToggle()) {
                    return;
                }
                addDetailSwitchActionRow(context, (ToggleableDevice) device, tableLayout, connectionId);
            }
        });
    }

    protected void addDetailSwitchActionRow(Context context, ToggleableDevice device, TableLayout layout, String connectionId) {
        ButtonHook hook = deviceHookProvider.buttonHookFor(device);
        if (hook != ButtonHook.NORMAL && hook != ButtonHook.TOGGLE_DEVICE) {
            addOnOffActionRow(context, device, layout, OnOffActionRowForToggleables.LAYOUT_DETAIL, connectionId);
        } else {
            addToggleDeviceActionRow(context, device, layout, LAYOUT_DETAIL);
        }
    }

    @Override
    public Class getOverviewViewHolderClass() {
        return GenericDeviceOverviewViewHolder.class;
    }

    @Override
    protected void fillOverviewStrategies(List<ViewStrategy> overviewStrategies) {
        super.fillOverviewStrategies(overviewStrategies);
        overviewStrategies.add(toggleableOverviewStrategy);
        overviewStrategies.add(webcmdStrategy);
    }
}
