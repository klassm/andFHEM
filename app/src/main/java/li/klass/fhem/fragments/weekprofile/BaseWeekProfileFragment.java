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

package li.klass.fhem.fragments.weekprofile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.heating.HeatingDevice;
import li.klass.fhem.domain.heating.schedule.WeekProfile;
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.widget.NestedListView;
import li.klass.fhem.widget.NestedListViewAdapter;

public abstract class BaseWeekProfileFragment<H extends BaseHeatingInterval> extends BaseFragment {
    private String deviceName;

    @SuppressWarnings("unused")
    public BaseWeekProfileFragment(Bundle bundle) {
        super(bundle);
        deviceName = bundle.getString(BundleExtraKeys.DEVICE_NAME);
    }

    @SuppressWarnings("unused")
    public BaseWeekProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;

        beforeCreateView();

        View view = inflater.inflate(R.layout.weekprofile, container, false);

        Button saveButton = (Button) view.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Actions.DEVICE_SET_WEEK_PROFILE);
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
                Intent intent = new Intent(Actions.DEVICE_RESET_WEEK_PROFILE);
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

        NestedListView nestedListView = (NestedListView) view.findViewById(R.id.weekprofile);
        nestedListView.setAdapter(getAdapter());

        return view;
    }

    @Override
    public void update(boolean doUpdate) {

        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doUpdate);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, deviceName);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                if (resultCode == ResultCodes.SUCCESS && getView() != null) {
                    Device device = (Device) resultData.getSerializable(BundleExtraKeys.DEVICE);
                    if (device == null || !(device instanceof HeatingDevice)) {
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    HeatingDevice<?, ?, H, ? extends Device> heatingDevice = (HeatingDevice) device;

                    View holder = getView().findViewById(R.id.changeValueButtonHolder);
                    if (holder == null) return;

                    WeekProfile<H, ?, ? extends Device> weekProfile = heatingDevice.getWeekProfile();
                    updateAdapterWith(weekProfile);

                    if (weekProfile.getChangedDayProfiles().size() > 0) {
                        holder.setVisibility(View.VISIBLE);
                    } else {
                        holder.setVisibility(View.GONE);
                    }
                }
            }
        });
        getActivity().startService(intent);
    }

    protected abstract void updateAdapterWith(WeekProfile<H, ?, ? extends Device> weekProfile);

    protected abstract NestedListViewAdapter getAdapter();

    protected void beforeCreateView() {
    }
}
