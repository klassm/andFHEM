/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.adapter.devices.genericui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;

public abstract class DeviceDetailViewAction<T extends Device> {
    private int buttonText;

    protected DeviceDetailViewAction(int buttonText) {
        this.buttonText = buttonText;
    }

    public Button createButton(Context context, LayoutInflater inflater, T device) {
        Button button = (Button) inflater.inflate(R.layout.button, null).findViewById(R.id.button);
        button.setOnClickListener(createListener(context, device));
        button.setText(buttonText);

        return button;
    }

    private Button.OnClickListener createListener(final Context context, final T device) {
        return new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClick(context, device);
            }
        };
    }
    public abstract void onButtonClick(Context context, T device);
}
