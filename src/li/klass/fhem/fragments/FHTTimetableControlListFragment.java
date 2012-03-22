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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import li.klass.fhem.R;
import li.klass.fhem.adapter.fhtControl.FHTTimetableControlListAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.FHTDevice;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.widget.NestedListView;

public class FHTTimetableControlListFragment extends BaseFragment {
    private String deviceName;
    private transient volatile FHTTimetableControlListAdapter adapter;

    @SuppressWarnings("unused")
    public FHTTimetableControlListFragment(Bundle bundle) {
        deviceName = bundle.getString(BundleExtraKeys.DEVICE_NAME);
    }

    @SuppressWarnings("unused")
    public FHTTimetableControlListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;

        adapter = new FHTTimetableControlListAdapter(getActivity());
        
        View view = inflater.inflate(R.layout.control_fht_list, container, false);

        Button saveButton = (Button) view.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.DEVICE_SET_TIMETABLE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
                intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                        update(false);
                    }
                });
                getActivity().startService(intent);
            }
        });

        Button resetButton = (Button) view.findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.DEVICE_RESET_TIMETABLE);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
                intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                        update(false);
                    }
                });
                getActivity().startService(intent);
            }
        });

        NestedListView nestedListView = (NestedListView) view.findViewById(R.id.control_fht_list);
        nestedListView.setAdapter(adapter);

        update(false);

        return view;
    }

    @Override
    public void update(boolean doUpdate) {

        if (adapter == null) return;

        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doUpdate);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                if (resultCode == ResultCodes.SUCCESS && getView() != null) {
                    FHTDevice fhtDevice = (FHTDevice) resultData.getSerializable(BundleExtraKeys.DEVICE);
                    View holder = getView().findViewById(R.id.changeValueButtonHolder);

                    if (holder == null || fhtDevice == null) return;

                    adapter.updateData(fhtDevice.getDayControlMap());

                    if (fhtDevice.hasChangedDayControlMapValues()) {
                        holder.setVisibility(View.VISIBLE);
                    } else {
                        holder.setVisibility(View.GONE);
                    }
                }
            }
        });
        getActivity().startService(intent);
    }
}
