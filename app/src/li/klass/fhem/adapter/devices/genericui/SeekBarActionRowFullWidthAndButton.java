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
import android.widget.TableRow;
import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceStateAdditionalInformationType;
import li.klass.fhem.util.DialogUtil;

public abstract class SeekBarActionRowFullWidthAndButton<T extends Device<T>> extends SeekBarActionRowFullWidth<T> {

    private Context context;

    public SeekBarActionRowFullWidthAndButton(Context context, int initialProgress, int maximumProgress) {
        super(initialProgress, maximumProgress, R.layout.device_detail_seekbarrow_with_button);
        this.context = context;
    }

    @Override
    public TableRow createRow(final LayoutInflater inflater, final T device) {
        TableRow row = super.createRow(inflater, device);

        Button button = (Button) row.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = context.getString(R.string.set_value);

                DialogUtil.showInputBox(context, title, "", new DialogUtil.InputDialogListener() {
                    @Override
                    public void onClick(String text) {
                        if (DeviceStateAdditionalInformationType.NUMERIC.matches(text)) {
                            onButtonSetValue(device, Integer.parseInt(text));
                        } else {
                            DialogUtil.showAlertDialog(context, R.string.error, R.string.invalidInput);
                        }
                    }
                });
            }
        });
        if (!showButton()) {
            button.setVisibility(View.GONE);
        }

        return row;
    }

    public abstract void onButtonSetValue(T device, int value);

    protected boolean showButton() {
        return true;
    }
}
