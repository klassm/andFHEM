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
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.fragments.core;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.util.advertisement.AdvertisementUtil;

import java.io.Serializable;
import java.util.Map;

public class DeviceDetailFragment extends BaseFragment {

    @SuppressWarnings("unused")
    public DeviceDetailFragment(Bundle bundle) {
        super(bundle);
    }

    @SuppressWarnings("unused")
    public DeviceDetailFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;

        View view = inflater.inflate(R.layout.device_detail_view, container, false);
        AdvertisementUtil.addAd(view, getActivity());

        return view;
    }

    @Override
    public void update(boolean doUpdate) {
        Intent intent = new Intent(Actions.GET_DEVICE_FOR_NAME);
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doUpdate);
        intent.putExtra(BundleExtraKeys.DEVICE_NAME, creationAttributes.get(BundleExtraKeys.DEVICE_NAME));
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                if (resultCode == ResultCodes.SUCCESS && getView() != null) {
                    Device device = (Device) resultData.getSerializable(BundleExtraKeys.DEVICE);
                    if (device == null) return;

                    DeviceAdapter adapter = DeviceType.getAdapterFor(device);
                    ScrollView scrollView = (ScrollView) getView().findViewById(R.id.deviceDetailView);
                    if (scrollView != null) {
                        scrollView.removeAllViews();
                        scrollView.addView(adapter.createDetailView(getActivity(), device));
                    }
                }
            }
        });
        getActivity().startService(intent);
    }

    @Override
    protected void onContentChanged(Map<String, Serializable> oldCreationAttributes, Map<String, Serializable> newCreationAttributes) {
        super.onContentChanged(oldCreationAttributes, newCreationAttributes);
        updateIfAttributesDoNotMatch(oldCreationAttributes, newCreationAttributes, BundleExtraKeys.DEVICE_NAME);
    }
}
