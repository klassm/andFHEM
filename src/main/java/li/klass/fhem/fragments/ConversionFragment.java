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

package li.klass.fhem.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import li.klass.fhem.R;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.util.NumberSystemUtil;

public class ConversionFragment extends BaseFragment {

    private transient EditText inputField;
    private transient TextView resultField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.conversion, container, false);
        inputField = (EditText) view.findViewById(R.id.input);
        Button hexToQuatButton = (Button) view.findViewById(R.id.hexToQuat);
        Button quatToHexButton = (Button) view.findViewById(R.id.quatToHex);
        resultField = (TextView) view.findViewById(R.id.result);

        hexToQuatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String input = getInputText();
                    String result = NumberSystemUtil.hexToQuaternary(input, 0);
                    setResult(result);
                } catch (IllegalArgumentException e) {
                    setResult(getActivity().getString(R.string.error));
                }
            }
        });
        quatToHexButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String input = getInputText();
                    String result = NumberSystemUtil.quaternaryToHex(input);
                    setResult(result);
                } catch (IllegalArgumentException e) {
                    setResult(getActivity().getString(R.string.error));
                }
            }
        });
        return view;
    }

    private String getInputText() {
        return inputField.getText().toString();
    }

    private void setResult(String result) {
        resultField.setText(result);
    }

    @Override
    public void update(boolean doUpdate) {
    }
}
