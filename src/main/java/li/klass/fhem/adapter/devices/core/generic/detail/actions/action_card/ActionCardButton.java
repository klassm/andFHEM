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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import li.klass.fhem.R;
import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.room.list.backend.xmllist.XmlListDevice;

public abstract class ActionCardButton implements ActionCardAction {

    private final String buttonText;

    public ActionCardButton(int buttonText, Context context) {
        this.buttonText = context.getString(buttonText);
    }

    @Override
    public View createView(final XmlListDevice device, final String connectionId, final Context context, LayoutInflater inflater, ViewGroup parent) {
        Button button = (Button) inflater.inflate(R.layout.button_device_detail, parent, false);
        button.setText(buttonText);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionCardButton.this.onClick(device, connectionId, context);
            }
        });

        return button;
    }

    protected abstract void onClick(XmlListDevice device, String connectionId, Context context);

    @Override
    public boolean supports(GenericDevice genericDevice) {
        return true;
    }
}
