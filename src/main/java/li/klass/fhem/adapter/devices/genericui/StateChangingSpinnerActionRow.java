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

import java.util.List;

import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

public class StateChangingSpinnerActionRow extends SpinnerActionRow {

    private final String commandAttribute;

    public StateChangingSpinnerActionRow(Context context, int description, int prompt,
                                         List<String> spinnerValues, String selectedValue,
                                         String commandAttribute) {
        this(context, context.getString(description), context.getString(prompt), spinnerValues, selectedValue, commandAttribute);
    }

    public StateChangingSpinnerActionRow(Context context, String description, String prompt,
                                         List<String> spinnerValues, String selectedValue,
                                         String commandAttribute) {
        super(context, description, prompt, spinnerValues, selectedValue);
        this.commandAttribute = commandAttribute;
    }

    @Override
    public void onItemSelected(Context context, XmlListDevice device, String item) {

        new StateUiService().setSubState(device, commandAttribute, item, context);
    }
}
