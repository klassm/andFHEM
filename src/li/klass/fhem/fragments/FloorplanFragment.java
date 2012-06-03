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

package li.klass.fhem.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.DeviceType;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.domain.floorplan.Coordinate;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.widget.TouchImageView;

import java.util.HashMap;
import java.util.Map;

public class FloorplanFragment extends BaseFragment {

    private transient TouchImageView floorplanView;
    private String deviceName;
    private transient Map<Device, View> deviceViewMap = new HashMap<Device, View>();
    public static final float DEFAULT_ITEM_ZOOM_FACTOR = 0.3f;

    private int tickCounter = 0;

    @SuppressWarnings("unused")
    public FloorplanFragment() {
    }

    @SuppressWarnings("unused")
    public FloorplanFragment(Bundle bundle) {
        deviceName = bundle.getString(BundleExtraKeys.DEVICE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.floorplan, null);

        TouchImageView.OnTouchImageViewChangeListener listener = new TouchImageView.OnTouchImageViewChangeListener() {
            @Override
            public void onTouchImageViewChange(float newScale, float newX, float newY) {
                if (tickCounter++ % 2 != 0) return;
                for (Device device : deviceViewMap.keySet()) {
                    updateViewFor(device, newScale, newX, newY);
                }
            }
        };

        floorplanView = (TouchImageView) view.findViewById(R.id.floorplan);
        floorplanView.setListener(listener);

        return view;
    }

    private void requestFloorplanDevices(boolean doUpdate) {
        Intent intent = new Intent(Actions.GET_ALL_ROOMS_DEVICE_LIST);
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doUpdate);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (getView() == null) return;
                RelativeLayout layout = (RelativeLayout) getView().findViewById(R.id.floorplanHolder);

                RoomDeviceList deviceList = (RoomDeviceList) resultData.getSerializable(BundleExtraKeys.DEVICE_LIST);
                for (Device device : deviceList.getAllDevices()) {
                    if (! device.isOnFloorplan(deviceName)) continue;
                    DeviceAdapter<Device> adapter = DeviceType.getAdapterFor(device);
                    if (! adapter.supportsFloorplan(device)) continue;

                    View view = adapter.getFloorplanView(getActivity(), device);
                    view.setVisibility(View.INVISIBLE);
                    view.setTag(device);
                    layout.addView(view);

                    deviceViewMap.put(device, view);
                }
                floorplanView.manualTouch();

                Intent intent = new Intent(Actions.DISMISS_UPDATING_DIALOG);
                getActivity().startService(intent);
            }
        });
        getActivity().startService(intent);
    }

    private void updateViewFor(Device device, float newScale, float newX, float newY) {
        if (! deviceViewMap.containsKey(device)) return;

        View view = deviceViewMap.get(device);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();

        // negate the coordinate (all android coordinates are negative) and renive the image offset from the floorplan FHEM UI
        Coordinate coordinate = device.getCoordinateFor(deviceName).add(new Coordinate(-190, -15)).negate();

        float baseScale = floorplanView.getBaseScale();
        float requiredX = (int) (coordinate.x * baseScale * newScale);
        float requiredY = (int) (coordinate.y * baseScale * newScale);

        int newLeftMargin = (int) ((requiredX - newX) * -1);
        int newTopMargin = (int) ((requiredY - newY) * -1);

        params.setMargins(newLeftMargin, newTopMargin, 0, 0);
        view.setScaleX(newScale * DEFAULT_ITEM_ZOOM_FACTOR);
        view.setScaleY(newScale * DEFAULT_ITEM_ZOOM_FACTOR);

        view.setPivotX(0);
        view.setPivotY(0);

        view.setVisibility(View.VISIBLE);

        view.requestLayout();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackground();
    }

    @Override
    public void update(boolean doUpdate) {
        tickCounter = 0;
        RelativeLayout layout = (RelativeLayout) getView().findViewById(R.id.floorplanHolder);
        layout.removeAllViews();
        layout.addView(floorplanView);

        requestFloorplanDevices(doUpdate);
    }

    private void setBackground() {
        Intent intent = new Intent(Actions.FLOORPLAN_IMAGE);
        intent.putExtra(BundleExtraKeys.FLOORPLAN_IMAGE_RELATIVE_PATH, "/fp_" + deviceName + ".png");
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS || ! resultData.containsKey(BundleExtraKeys.FLOORPLAN_IMAGE)) return;

                floorplanView.setImageBitmap((Bitmap) resultData.getParcelable(BundleExtraKeys.FLOORPLAN_IMAGE));

                Intent intent = new Intent(Actions.DISMISS_UPDATING_DIALOG);
                getActivity().startService(intent);

                requestFloorplanDevices(false);
            }
        });
        getActivity().startService(intent);
    }
}
