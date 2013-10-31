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

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.content.Intent;
import android.widget.ToggleButton;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.ToggleableDevice;

public abstract class YesNoToggleActionRow<D extends ToggleableDevice<D>> extends ToggleActionRow<D> {

    private final String commandAttribute;

    public YesNoToggleActionRow(String commandAttribute, int description) {
        super(description, ToggleActionRow.LAYOUT_DETAIL);
        this.commandAttribute = commandAttribute;
    }

    @Override
    protected void setToogleButtonText(D device, ToggleButton toggleButton) {
        Context context = AndFHEMApplication.getContext();
        toggleButton.setTextOff(context.getString(R.string.no));
        toggleButton.setTextOn(context.getString(R.string.yes));
    }

    @Override
    public abstract boolean isOn(D device);

    @Override
    public void onButtonClick(Context context, D device, boolean isChecked) {
        Intent intent = new Intent(Actions.DEVICE_SET_SUB_STATE);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        intent.putExtra(BundleExtraKeys.STATE_NAME, commandAttribute);
        intent.putExtra(BundleExtraKeys.STATE_VALUE, isChecked ? "on" : "off");
        GenericDeviceAdapter.putUpdateExtra(intent);

        context.startService(intent);
    }
}
