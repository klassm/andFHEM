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

import android.annotation.SuppressLint;
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

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.service.intent.ConnectionsIntentService;
import li.klass.fhem.ui.FileDialog;
import li.klass.fhem.util.DialogUtil;

import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_CLIENT_CERTIFICATE_PASSWORD;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_CLIENT_CERTIFICATE_PATH;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ENABLE_CLIENT_CERTIFICATE;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_SERVER_CERTIFICATE_PATH;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_URL;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_USERNAME;
import static li.klass.fhem.constants.ResultCodes.SUCCESS;

public class ConnectionDetailFragment extends BaseFragment {

    private static final String TAG = ConnectionDetailFragment.class.getName();
    private String connectionId;
    private boolean isModify = false;
    private ServerType connectionType;
    private ConnectionTypeDetailChangedListener detailChangedListener = null;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        if (args.containsKey(BundleExtraKeys.CONNECTION_ID)) {
            connectionId = args.getString(BundleExtraKeys.CONNECTION_ID);
            isModify = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            return view;
        }

        view = inflater.inflate(R.layout.connection_detail, container, false);
        assert (view != null);

        Spinner connectionTypeSpinner = (Spinner) view.findViewById(R.id.connectionType);
        if (isModify) {
            connectionTypeSpinner.setEnabled(false);
        }

        final List<ServerType> connectionTypes = getServerTypes();

        ArrayAdapter<ServerType> adapter = new ArrayAdapter<>(getActivity(),
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

    private List<ServerType> getServerTypes() {
        final List<ServerType> connectionTypes = newArrayList();
        connectionTypes.addAll(Arrays.asList(ServerType.values()));
        connectionTypes.remove(ServerType.DUMMY);
        return connectionTypes;
    }

    @SuppressLint("InflateParams")
    private void handleConnectionTypeChange(ServerType connectionType) {
        if (getView() == null) return;

        this.connectionType = connectionType;
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view;
        if (connectionType == ServerType.FHEMWEB) {
            view = inflater.inflate(R.layout.connection_fhemweb, null);
            handleFHEMWEBView(view);
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

        LinearLayout connectionPreferences = (LinearLayout) getView().findViewById(R.id.connectionPreferences);
        connectionPreferences.removeAllViews();
        connectionPreferences.addView(view);

        if (detailChangedListener != null) detailChangedListener.onChanged();
    }

    private void handleSave() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), ConnectionsIntentService.class);
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
                if (!handleTelnetSave(intent)) {
                    return;
                }
                break;

            case FHEMWEB:
                if (!handleFHEMWEBSave(intent)) {
                    return;
                }
                break;
        }

        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                if (resultCode != ResultCodes.SUCCESS) {
                    Log.e(TAG, "could not save! resultCode=" + resultCode + ",resultData=" + resultData);
                    return;
                }

                Intent intent = new Intent(Actions.BACK);
                getActivity().sendBroadcast(intent);
            }
        });

        getActivity().startService(intent);
    }

    private void handleFHEMWEBView(View view) {
        Button setClientCertificate = (Button) view.findViewById(R.id.setClientCertificatePath);
        setClientCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getView() == null) return;

                final TextView clientCertificatePath = (TextView) getView().findViewById(R.id.clientCertificatePath);
                File initialPath = new File(clientCertificatePath.getText().toString());

                FileDialog fileDialog = new FileDialog(view.getContext(), initialPath);
                fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        clientCertificatePath.setText(file.getAbsolutePath());
                    }
                });
                fileDialog.showDialog();
            }
        });

        Button setServerCertificate = (Button) view.findViewById(R.id.setServerCertificatePath);
        setServerCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getView() == null) return;

                final TextView serverCertificatePath = (TextView) getView().findViewById(R.id.serverCertificatePath);
                File initialPath = new File(serverCertificatePath.getText().toString());

                FileDialog fileDialog = new FileDialog(view.getContext(), initialPath);
                fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        serverCertificatePath.setText(file.getAbsolutePath());
                    }
                });
                fileDialog.showDialog();
            }
        });
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
        if (getView() == null) return false;

        String url = getTextViewContent(R.id.url);
        if (enforceNotEmpty(R.string.connectionURL, url)) return false;
        if (enforceUrlStartsWithHttp(url)) return false;

        intent.putExtra(CONNECTION_URL, url);

        String username = getTextViewContent(R.id.username);
        intent.putExtra(CONNECTION_USERNAME, username);

        String clientCertificatePath = getTextViewContent(R.id.clientCertificatePath);
        String serverCertificatePath = getTextViewContent(R.id.serverCertificatePath);

        CheckBox clientCertificateCheckbox = (CheckBox) getView().findViewById(R.id.enableCertificateAuthentication);
        boolean clientCertificateEnabled = clientCertificateCheckbox.isChecked();

        intent.putExtra(CONNECTION_CLIENT_CERTIFICATE_PATH, clientCertificatePath);
        intent.putExtra(CONNECTION_SERVER_CERTIFICATE_PATH, serverCertificatePath);
        intent.putExtra(CONNECTION_ENABLE_CLIENT_CERTIFICATE, clientCertificateEnabled);
        intent.putExtra(CONNECTION_CLIENT_CERTIFICATE_PASSWORD, getTextViewContent(R.id.clientCertificatePassword));

        return true;
    }

    private boolean enforceUrlStartsWithHttp(String url) {
        if (!url.startsWith("http")) {
            Context context = getActivity();
            String emptyError = context.getString(R.string.connectionUrlHttp);

            DialogUtil.showAlertDialog(context, R.string.error, emptyError);

            return true;
        }
        return false;
    }

    @Override
    public void update(boolean doUpdate) {
        if (!isModify) {
            Log.e(TAG, "I can only update if a connection is being modified!");
            getActivity().sendBroadcast(new Intent(Actions.DISMISS_EXECUTING_DIALOG));
            return;
        }

        Intent intent = new Intent(Actions.CONNECTION_GET);
        intent.setClass(getActivity(), ConnectionsIntentService.class);
        intent.putExtra(BundleExtraKeys.CONNECTION_ID, connectionId);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != SUCCESS || !resultData.containsKey(CONNECTION)) {
                    return;
                }

                Serializable serializable = resultData.getSerializable(CONNECTION);
                if (!(serializable instanceof FHEMServerSpec)) {
                    Log.e(TAG, "expected an FHEMServerSpec, but got " + serializable);
                    return;
                }

                setValuesForCurrentConnection((FHEMServerSpec) serializable);

                FragmentActivity activity = getActivity();
                if (activity != null)
                    activity.sendBroadcast(new Intent(Actions.DISMISS_EXECUTING_DIALOG));
            }
        });
        getActivity().startService(intent);
    }

    private void setValuesForCurrentConnection(final FHEMServerSpec connection) {
        final View view = getView();
        if (view == null) return;

        // We do not need to change the type selector here, as the right one is already selected.
        // We just overwrite values within the edit fields.
        if (connection.getServerType() == connectionType) {
            fillDetail(connection);
        } else {
            // We have to change the detail view to the one which is right for the current
            // connection type. However, we do not know when the selection changed listener
            // of the combo box fires. This is why we register a global listener, which is called
            // when the new view has been attached to the root view.
            // Afterwards we can continue with filling the fields with the respective values
            // of the current connection!
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

        switch (connectionType) {
            case FHEMWEB:
                fillFHEMWEB(connection);
                break;
            case TELNET:
                fillTelnet(connection);
                break;
        }
    }

    private int selectionIndexFor(ServerType serverType) {
        List<ServerType> serverTypes = getServerTypes();
        for (int i = 0; i < serverTypes.size(); i++) {
            if (serverType == serverTypes.get(i)) return i;
        }
        return -1;
    }

    private void setTextViewContent(int id, String value) {
        setTextViewContent(getView(), id, value);
    }

    private void fillFHEMWEB(FHEMServerSpec connection) {
        View view = getView();
        if (view == null) return;

        setTextViewContent(view, R.id.url, connection.getUrl());
        setTextViewContent(view, R.id.username, connection.getUsername() + "");
        setTextViewContent(view, R.id.password, connection.getPassword());
        setTextViewContent(view, R.id.clientCertificatePath, connection.getClientCertificatePath());
        setTextViewContent(view, R.id.serverCertificatePath, connection.getServerCertificatePath());
        setTextViewContent(view, R.id.clientCertificatePassword, connection.getClientCertificatePassword());

        CheckBox clientCertificateCheckbox = (CheckBox) getView().findViewById(R.id.enableCertificateAuthentication);
        clientCertificateCheckbox.setChecked(connection.isClientCertificateEnabled());
    }

    private void fillTelnet(FHEMServerSpec connection) {
        View view = getView();
        if (view == null) return;

        setTextViewContent(view, R.id.ip, connection.getIp());
        setTextViewContent(view, R.id.port, connection.getPort() + "");
        setTextViewContent(view, R.id.password, connection.getPassword());
    }

    private void setTextViewContent(View view, int id, String value) {
        if (view == null) return;

        TextView textView = (TextView) view.findViewById(id);
        if (textView != null) {
            textView.setText(value);
        }
    }

    private interface ConnectionTypeDetailChangedListener {
        void onChanged();
    }
}
