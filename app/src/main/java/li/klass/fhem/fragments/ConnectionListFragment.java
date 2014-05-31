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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.adapter.ConnectionListAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.fragments.core.TopLevelFragment;
import li.klass.fhem.license.LicenseManager;
import li.klass.fhem.util.Reject;
import li.klass.fhem.util.advertisement.AdvertisementUtil;

import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_LIST;

public class ConnectionListFragment extends BaseFragment implements TopLevelFragment {

    public static final String TAG = ConnectionListFragment.class.getName();
    private String clickedConnectionId;

    public static final int CONTEXT_MENU_DELETE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;

        ConnectionListAdapter adapter = new ConnectionListAdapter(getActivity(),
                new ArrayList<FHEMServerSpec>());
        View layout = inflater.inflate(R.layout.connection_list, container, false);
        AdvertisementUtil.addAd(layout, getActivity());

        LinearLayout emptyView = (LinearLayout) layout.findViewById(R.id.emptyView);
        fillEmptyView(emptyView);

        assert layout != null;
        ListView connectionList = (ListView) layout.findViewById(R.id.connectionList);
        Reject.ifNull(connectionList);
        connectionList.setAdapter(adapter);

        connectionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String connectionId = (String) view.getTag();
                onClick(connectionId);
            }
        });
        registerForContextMenu(connectionList);


        Button createButton = (Button) layout.findViewById(R.id.create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = getAdapter().getData().size();
                if (! LicenseManager.INSTANCE.isPro() && size >= AndFHEMApplication.PREMIUM_ALLOWED_FREE_CONNECTIONS) {
                    Intent intent = new Intent(Actions.SHOW_ALERT);
                    intent.putExtra(BundleExtraKeys.ALERT_CONTENT_ID, R.string.premium_multipleConnections);
                    intent.putExtra(BundleExtraKeys.ALERT_TITLE_ID, R.string.premium);
                    AndFHEMApplication.getContext().sendBroadcast(intent);

                    return;
                }

                Intent intent = new Intent(Actions.SHOW_FRAGMENT);
                intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.CONNECTION_DETAIL);

                getActivity().sendBroadcast(intent);
            }
        });

        return layout;
    }

    protected void onClick(String connectionId) {
        Intent intent = new Intent(Actions.SHOW_FRAGMENT);
        intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.CONNECTION_DETAIL);
        intent.putExtra(BundleExtraKeys.CONNECTION_ID, connectionId);

        getActivity().sendBroadcast(intent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(boolean doUpdate) {
        if (getView() == null) return;

        hideEmptyView();
        showUpdatingBar();

        Intent intent = new Intent(Actions.CONNECTIONS_LIST);
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doUpdate);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                if (getView() == null) return;

                hideUpdatingBar();

                if (resultCode == ResultCodes.SUCCESS && resultData != null &&
                        resultData.containsKey(CONNECTION_LIST)) {

                    List<FHEMServerSpec> connectionList = (ArrayList<FHEMServerSpec>)
                            resultData.getSerializable(CONNECTION_LIST);
                    assert connectionList != null;

                    for (FHEMServerSpec serverSpec : new ArrayList<FHEMServerSpec>(connectionList)) {
                        if (serverSpec.getServerType() == ServerType.DUMMY) {
                            connectionList.remove(serverSpec);
                        }
                    }

                    if (connectionList.size() == 0) {
                        showEmptyView();
                    }
                    String selectedId = creationBundle.getString(CONNECTION_ID);
                    getAdapter().updateData(connectionList, selectedId);
                    scrollToSelected(selectedId, getAdapter().getData());
                }
            }
        });
        getActivity().startService(intent);
    }

    private void scrollToSelected(String selectedConnectionId, List<FHEMServerSpec> serverList) {
        if (selectedConnectionId == null) return;

        View view = getView();
        if (view == null) return;

        ListView connectionListView = (ListView) view.findViewById(R.id.connectionList);
        if (connectionListView == null) return;

        for (int i = 0; i < serverList.size(); i++) {
            FHEMServerSpec serverSpec = serverList.get(i);
            if (serverSpec.getId().equals(selectedConnectionId)) {
                connectionListView.setSelection(i);
                return;
            }
        }
    }

    private ConnectionListAdapter getAdapter() {
        ListView listView = (ListView) getView().findViewById(R.id.connectionList);
        return (ConnectionListAdapter) listView.getAdapter();
    }

    @Override
    protected void fillEmptyView(LinearLayout view) {
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view, null);
        assert emptyView != null;
        TextView emptyText = (TextView) emptyView.findViewById(R.id.emptyText);
        emptyText.setText(R.string.noConnections);

        view.addView(emptyView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Object tag = info.targetView.getTag();

        if (tag == null || ! (tag instanceof String)) return;

        clickedConnectionId = (String) tag;

        menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        if (clickedConnectionId == null) return false;

        switch (item.getItemId()) {
            case CONTEXT_MENU_DELETE:
                Intent intent = new Intent(Actions.CONNECTION_DELETE);
                intent.putExtra(BundleExtraKeys.CONNECTION_ID, clickedConnectionId);
                intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);

                        if (resultCode != ResultCodes.SUCCESS) return;

                        update(false);
                    }
                });
                getActivity().startService(intent);
                return true;
        }
        return false;
    }
}
