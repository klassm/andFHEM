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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import li.klass.fhem.R;
import li.klass.fhem.domain.core.FhemDevice;

public abstract class DeviceDetailViewButtonAction extends DeviceDetailViewAction {
    private int buttonText;

    protected DeviceDetailViewButtonAction(int buttonText) {
        this.buttonText = buttonText;
    }

    @Override
    public View createView(Context context, LayoutInflater inflater, FhemDevice device, LinearLayout parent) {
        return createButton(context, inflater, device, parent);
    }

    public Button createButton(Context context, LayoutInflater inflater, FhemDevice device, LinearLayout parent) {
        Button button = (Button) inflater.inflate(R.layout.button_device_detail, parent, false);
        button.setOnClickListener(createListener(context, device));
        button.setText(buttonText);

        return button;
    }

    private Button.OnClickListener createListener(final Context context, final FhemDevice device) {
        return new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(context, device);
            }
        };
    }

    public boolean isVisible(FhemDevice device) {
        return true;
    }

    public abstract void onButtonClick(Context context, FhemDevice device);
}
