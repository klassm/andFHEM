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
import android.widget.*;
import li.klass.fhem.R;
import li.klass.fhem.domain.core.Device;

import java.util.List;

public abstract class SpinnerActionRow<T extends Device> {
    private int description;
    private int prompt;
    private List<String> spinnerValues;
    private int selectedPosition;
    private int temporarySelectedPosition;
    private Context context;
    private TableRow rowView;

    private boolean ignoreItemSelection = false;

    public SpinnerActionRow(Context context, int description, int prompt, List<String> spinnerValues, int selectedPosition) {
        this.description = description;
        this.prompt = prompt;
        this.spinnerValues = spinnerValues;
        this.selectedPosition = selectedPosition;
        this.context = context;
    }

    public TableRow createRow(final T device) {
        ignoreItemSelection = true;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = (TableRow) inflater.inflate(R.layout.device_detail_spinnerrow, null);

        ((TextView) rowView.findViewById(R.id.description)).setText(description);
        final Spinner spinner = (Spinner) rowView.findViewById(R.id.spinner);
        spinner.setPrompt(context.getString(prompt));

        ArrayAdapter adapter = new ArrayAdapter<String>(context, R.layout.spinnercontent, spinnerValues);
        spinner.setAdapter(adapter);

        spinner.setSelection(selectedPosition);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (ignoreItemSelection || selectedPosition == position) {
                    revertSelection();
                    return;
                }
                temporarySelectedPosition = position;

                SpinnerActionRow.this.onItemSelected(context, device, spinnerValues.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ignoreItemSelection = false;

        return rowView;
    }

    public void revertSelection() {
        Spinner spinner = (Spinner) rowView.findViewById(R.id.spinner);
        spinner.setSelection(selectedPosition);
    }

    public void commitSelection() {
        selectedPosition = temporarySelectedPosition;
    }

    public abstract void onItemSelected(final Context context, T device, String item);
}
