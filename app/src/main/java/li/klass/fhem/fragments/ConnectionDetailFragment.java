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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.util.DialogUtil;

import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION;
import static li.klass.fhem.constants.ResultCodes.SUCCESS;

public class ConnectionDetailFragment extends BaseFragment {

    private interface ConnectionTypeDetailChangedListener {
        void onChanged();
    }

    private static final String TAG = ConnectionDetailFragment.class.getName();
    private String connectionId;
    private boolean isModify = false;
    private ServerType connectionType;
    private ConnectionTypeDetailChangedListener detailChangedListener = null;

    @SuppressWarnings("unused")
    public ConnectionDetailFragment(Bundle bundle) {
        super(bundle);
        if (bundle.containsKey(BundleExtraKeys.CONNECTION_ID)) {
            connectionId = bundle.getString(BundleExtraKeys.CONNECTION_ID);
            isModify = true;
        }
    }

    @SuppressWarnings("unused")
    public ConnectionDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            return view;
        }

        view = inflater.inflate(R.layout.connection_detail, null);
        assert (view != null);

        Spinner connectionTypeSpinner = (Spinner) view.findViewById(R.id.connectionType);
        if (isModify) {
            connectionTypeSpinner.setEnabled(false);
        }

        final List<ServerType> connectionTypes = getServerTypes();

        ArrayAdapter<ServerType> adapter = new ArrayAdapter<ServerType>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, connectionTypes);
        connectionTypeSpinner.setAdapter(adapter);

        connectionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                handleConnectionTypeChange(connectionTypes.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button saveButton = (Button) view.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSave();
            }
        });

        return view;
    }

    private int selectionIndexFor(ServerType serverType) {
        List<ServerType> serverTypes = getServerTypes();
        for (int i = 0; i < serverTypes.size(); i++) {
            if (serverType == serverTypes.get(i)) return i;
        }
        return -1;
    }

    private List<ServerType> getServerTypes() {
        final List<ServerType> connectionTypes = new ArrayList<ServerType>();
        connectionTypes.addAll(Arrays.asList(ServerType.values()));
        connectionTypes.remove(ServerType.DUMMY);
        return connectionTypes;
    }

    private void handleConnectionTypeChange(ServerType connectionType) {
        this.connectionType = connectionType;
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View view;
        if (connectionType == ServerType.FHEMWEB) {
            view = inflater.inflate(R.layout.connection_fhemweb, null);
        } else if (connectionType == ServerType.TELNET) {
            view = inflater.inflate(R.layout.connection_telnet, null);
        } else {
            throw new IllegalArgumentException("cannot handle connection type " + connectionType);
        }

        assert view != null;

        CheckBox showPasswordCheckbox = (CheckBox) view.findViewById(R.id.showPasswordCheckbox);
        final EditText passwordView = (EditText) view.findViewById(R.id.password);
        if (showPasswordCheckbox != null && passwordView != null) {
            showPasswordCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox radio = (CheckBox) view;
                    boolean checked = radio.isChecked();
                    if (checked) {
                        passwordView.setTransformationMethod(null);
                    } else {
                        passwordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                }
            });
        }

        View layout = getView();
        LinearLayout connectionPreferences = (LinearLayout) layout.findViewById(R.id.connectionPreferences);
        connectionPreferences.removeAllViews();
        connectionPreferences.addView(view);

        if (detailChangedListener != null) detailChangedListener.onChanged();
    }

    @Override
    public void update(boolean doUpdate) {
        if (! isModify) {
            Log.e(TAG, "I can only update if a connection is being modified!");
            hideUpdatingBar();
            return;
        }

        Intent intent = new Intent(Actions.CONNECTION_GET);
        intent.putExtra(BundleExtraKeys.CONNECTION_ID, connectionId);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != SUCCESS || ! resultData.containsKey(CONNECTION)) {
                    return;
                }

                Serializable serializable = resultData.getSerializable(CONNECTION);
                if (!(serializable instanceof FHEMServerSpec)) {
                    Log.e(TAG, "expected an FHEMServerSpec, but got " + serializable);
                    return;
                }

                setValuesForCurrentConnection((FHEMServerSpec) serializable);

                Intent intent = new Intent(Actions.DISMISS_UPDATING_DIALOG);
                FragmentActivity activity = getActivity();
                if (activity != null) activity.sendBroadcast(intent);
            }
        });
        getActivity().startService(intent);
    }

    private void setValuesForCurrentConnection(final FHEMServerSpec connection) {
        final View view = getView();
        if (view == null) return;

        if (connection.getServerType() == connectionType) {
            fillDetail(connection);
        } else {
            detailChangedListener = new ConnectionTypeDetailChangedListener() {
                @Override
                public void onChanged() {
                    detailChangedListener = null;

                    fillDetail(connection);
                }
            };
            Spinner connectionTypeSpinner = (Spinner) view.findViewById(R.id.connectionType);
            connectionTypeSpinner.setSelection(selectionIndexFor(connection.getServerType()), true);
        }
    }

    private void fillDetail(FHEMServerSpec connection) {
        setTextViewContent(R.id.name, connection.getName());

        switch(connectionType) {
            case FHEMWEB:
                fillFHEMWEB(connection);
                break;
            case TELNET:
                fillTelnet(connection);
                break;
        }
    }

    private void fillTelnet(FHEMServerSpec connection) {
        View view = getView();
        if (view == null) return;

        setTextViewContent(view, R.id.ip, connection.getIp());
        setTextViewContent(view, R.id.port, connection.getPort() + "");
        setTextViewContent(view, R.id.password, connection.getPassword());
    }

    private void fillFHEMWEB(FHEMServerSpec connection) {
        View view = getView();
        if (view == null) return;

        setTextViewContent(view, R.id.url, connection.getUrl());
        setTextViewContent(view, R.id.username, connection.getUsername() + "");
        setTextViewContent(view, R.id.password, connection.getPassword());
    }

    private void setTextViewContent(int id, String value) {
        setTextViewContent(getView(), id, value);
    }

    private void setTextViewContent(View view, int id, String value) {
        if (view == null) return;

        TextView textView = (TextView) view.findViewById(id);
        if (textView != null) {
            textView.setText(value);
        }
    }

    private String getTextViewContent(int id) {
        View view = getView();
        if (view == null) return null;

        TextView textView = (TextView) view.findViewById(id);
        if (textView == null) {
            Log.e(TAG, "cannot find " + id);
            return null;
        }
        return String.valueOf(textView.getText());
    }

    private void handleSave() {
        Intent intent = new Intent();
        if (isModify) {
            intent.setAction(Actions.CONNECTION_UPDATE);
            intent.putExtra(BundleExtraKeys.CONNECTION_ID, connectionId);
        } else {
            intent.setAction(Actions.CONNECTION_CREATE);
        }

        intent.putExtra(BundleExtraKeys.CONNECTION_TYPE, connectionType.name());

        String name = getTextViewContent(R.id.name);
        if (enforceNotEmpty(R.string.connectionName, name)) return;
        intent.putExtra(BundleExtraKeys.CONNECTION_NAME, name);

        intent.putExtra(BundleExtraKeys.CONNECTION_PASSWORD, getTextViewContent(R.id.password));

        switch (connectionType) {
            case TELNET:
                if (! handleTelnetSave(intent)) {
                    return;
                }
                break;

            case FHEMWEB:
                if (! handleFHEMWEBSave(intent)) {
                    return;
                }
                break;
        }

        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                Intent intent = new Intent(Actions.BACK);
                getActivity().sendBroadcast(intent);
            }
        });

        getActivity().startService(intent);
    }

    private boolean handleTelnetSave(Intent intent) {
        String ip = getTextViewContent(R.id.ip);
        if (enforceNotEmpty(R.string.connectionIP, ip)) return false;
        intent.putExtra(BundleExtraKeys.CONNECTION_IP, ip);

        String port = getTextViewContent(R.id.port);
        if (enforceNotEmpty(R.string.connectionPort, port)) return false;
        intent.putExtra(BundleExtraKeys.CONNECTION_PORT, port);

        return true;
    }

    private boolean handleFHEMWEBSave(Intent intent) {
        String url = getTextViewContent(R.id.url);
        if (enforceNotEmpty(R.string.connectionURL, url)) return false;
        intent.putExtra(BundleExtraKeys.CONNECTION_URL, url);


        String username = getTextViewContent(R.id.username);
        intent.putExtra(BundleExtraKeys.CONNECTION_USERNAME, username);

        return true;
    }

    private boolean enforceNotEmpty(int fieldName, String value) {
        if (value != null && value.trim().length() > 0) {
            return false;
        }

        Context context = getActivity();
        String emptyError = context.getString(R.string.connectionEmptyError);
        String errorMessage = String.format(emptyError, context.getString(fieldName));

        DialogUtil.showAlertDialog(context, R.string.error, errorMessage);

        return true;
    }
}
