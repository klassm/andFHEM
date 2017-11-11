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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.room.list.backend.xmllist.XmlListDevice;

public abstract class SpinnerActionRow {
    private String description;
    private String prompt;
    private List<String> spinnerValues;
    private int selectedPosition;
    private int temporarySelectedPosition;
    private Context context;
    private TableRow rowView;

    private boolean ignoreItemSelection = false;

    public SpinnerActionRow(Context context, int description, int prompt, List<String> spinnerValues, int selectedPosition) {
        this(context, context.getString(description), context.getString(prompt), spinnerValues, selectedPosition);
    }

    public SpinnerActionRow(Context context, String description, String prompt, List<String> spinnerValues, String selectedValue) {
        if (!spinnerValues.contains(selectedValue)) {
            selectedValue = context.getString(R.string.selectValue);
            spinnerValues = ImmutableList.<String>builder().add(selectedValue).addAll(spinnerValues).build();
        }
        this.description = description;
        this.prompt = prompt;
        this.spinnerValues = spinnerValues;
        this.selectedPosition = spinnerValues.indexOf(selectedValue);
        this.context = context;
    }

    public SpinnerActionRow(Context context, String description, String prompt, List<String> spinnerValues, int selectedPosition) {
        this.description = description;
        this.prompt = prompt;
        this.spinnerValues = spinnerValues;
        this.selectedPosition = selectedPosition;
        this.context = context;
    }

    public TableRow createRow(final XmlListDevice device, final String connectionId, ViewGroup viewGroup) {
        ignoreItemSelection = true;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = (TableRow) inflater.inflate(R.layout.device_detail_spinnerrow, viewGroup, false);

        ((TextView) rowView.findViewById(R.id.description)).setText(description);
        final Spinner spinner = (Spinner) rowView.findViewById(R.id.spinner);
        spinner.setPrompt(prompt);

        ArrayAdapter adapter = new ArrayAdapter<>(context, R.layout.spinnercontent, spinnerValues);
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

                SpinnerActionRow.this.onItemSelected(context, device, connectionId, spinnerValues.get(position));
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

    public abstract void onItemSelected(final Context context, XmlListDevice device, String connectionId, String item);
}
