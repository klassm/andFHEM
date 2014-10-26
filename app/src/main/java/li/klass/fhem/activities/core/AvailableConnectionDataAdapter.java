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

package li.klass.fhem.activities.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.ListDataAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.service.intent.ConnectionsIntentService;

import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_LIST;
import static li.klass.fhem.constants.ResultCodes.SUCCESS;

public class AvailableConnectionDataAdapter extends ListDataAdapter<FHEMServerSpec>
        implements ActionBar.OnNavigationListener {

    private final ActionBar actionBar;

    private int currentlySelectedPosition = -1;

    private static class ManagementPill extends FHEMServerSpec {
        private ManagementPill() {
            super(ConnectionService.MANAGEMENT_DATA_ID);
            setName("managementDummy");
        }

        @Override
        public int compareTo(@NotNull FHEMServerSpec fhemServerSpec) {
            return 1;
        }
    }

    private static final ManagementPill MANAGEMENT_PILL = new ManagementPill();

    public AvailableConnectionDataAdapter(Context context, ActionBar actionBar) {
        super(context, R.layout.connection_spinner_item, new ArrayList<FHEMServerSpec>());
        this.actionBar = actionBar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FHEMServerSpec server = data.get(position);

        if (server instanceof ManagementPill) {
            return handleManagementView(parent);
        } else {
            return handleServerView(server);
        }
    }

    private View handleManagementView(ViewGroup parent) {
        return inflater.inflate(R.layout.connection_manage_item, parent, false);
    }

    private View handleServerView(FHEMServerSpec server) {
        View view = inflater.inflate(resource, null);
        assert view != null;

        TextView nameView = (TextView) view.findViewById(R.id.name);
        nameView.setText(server.getName());

        TextView typeView = (TextView) view.findViewById(R.id.type);
        typeView.setText(server.getServerType().name());

        return view;
    }

    @SuppressWarnings("unchecked")
    public void doLoad() {
        Intent intent = new Intent(Actions.CONNECTIONS_LIST);
        intent.setClass(context, ConnectionsIntentService.class);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == SUCCESS && resultData != null &&
                        resultData.containsKey(CONNECTION_LIST)) {
                    Serializable content = resultData.getSerializable(CONNECTION_LIST);
                    if (!(content instanceof List)) {
                        throw new IllegalArgumentException(CONNECTION_LIST + " always has to be a list!");
                    }

                    updateData((List<FHEMServerSpec>) content);

                    if (resultData.containsKey(CONNECTION_ID)) {
                        String selectedId = resultData.getString(CONNECTION_ID);
                        select(selectedId);
                    } else {
                        select(ConnectionService.DUMMY_DATA_ID);
                    }
                }
            }
        });
        context.startService(intent);
    }

    private void select(String id) {
        for (int i = 0; i < data.size(); i++) {
            Log.v(AvailableConnectionDataAdapter.class.getName(), data.get(i) + " - " + id);
            if (data.get(i).getId().equals(id)) {
                actionBar.setSelectedNavigationItem(i);
            }
        }
    }

    @Override
    public void updateData(List<FHEMServerSpec> newData) {
        newData.add(MANAGEMENT_PILL);
        super.updateData(newData);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (itemPosition == data.size() - 1) {
            actionBar.setSelectedNavigationItem(currentlySelectedPosition);

            Intent intent = new Intent(Actions.SHOW_FRAGMENT);
            intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.CONNECTION_LIST);
            context.sendBroadcast(intent);

            return true;
        }

        Intent intent = new Intent(Actions.CONNECTION_SET_SELECTED);
        intent.setClass(context, ConnectionsIntentService.class);
        intent.putExtra(BundleExtraKeys.CONNECTION_ID, data.get(itemPosition).getId());
        context.startService(intent);

        currentlySelectedPosition = itemPosition;
        return true;
    }
}
