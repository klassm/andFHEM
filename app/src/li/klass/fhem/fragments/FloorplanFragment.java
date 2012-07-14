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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.floorplan.Coordinate;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.util.device.DeviceActionUtil;
import li.klass.fhem.widget.TouchImageView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FloorplanFragment extends BaseFragment {

    private transient TouchImageView floorplanView;
    private String floorplanName;
    private transient Map<Device, View> deviceViewMap = new HashMap<Device, View>();
    private int tickCounter = 0;

    public float currentImageViewScale;

    public Coordinate currentImageViewCoordinate;
    private transient DeviceMoveTouchListener deviceMoveTouchListener = new DeviceMoveTouchListener();
    private transient DeviceOnLongClickListener deviceOnLongClickListener = new DeviceOnLongClickListener();

    public static Coordinate FLOORPLAN_OFFSET = new Coordinate(190, 15);
    public static final float DEFAULT_ITEM_ZOOM_FACTOR = 0.3f;

    @SuppressWarnings("unused")
    public FloorplanFragment() {
    }

    @SuppressWarnings("unused")
    public FloorplanFragment(Bundle bundle) {
        super(bundle);
        floorplanName = bundle.getString(BundleExtraKeys.DEVICE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;

        View view = inflater.inflate(R.layout.floorplan, null);

        TouchImageView.OnTouchImageViewChangeListener listener = new TouchImageView.OnTouchImageViewChangeListener() {
            @Override
            public void onTouchImageViewChange(float newScale, float newX, float newY) {
                currentImageViewScale = newScale * floorplanView.getBaseScale();
                currentImageViewCoordinate = new Coordinate(newX, newY);

                if (tickCounter++ % 2 != 0) return;
                for (Device device : deviceViewMap.keySet()) {
                    updateViewFor(device, newScale);
                }
            }
        };

        floorplanView = (TouchImageView) view.findViewById(R.id.floorplan);
        floorplanView.setListener(listener);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (floorplanView == null) return;
        floorplanView.postDelayed(new Runnable() {
            @Override
            public void run() {
                floorplanView.manualTouch();
            }
        }, 1000);
    }

    @Override
    protected void onContentChanged(Map<String, Serializable> oldCreationAttributes, Map<String, Serializable> newCreationAttributes) {
        if (oldCreationAttributes == null) {
            setBackground();
        }
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
                    if (! device.isOnFloorplan(floorplanName)) continue;
                    DeviceAdapter<Device> adapter = DeviceType.getAdapterFor(device);
                    if (! adapter.supportsFloorplan(device)) continue;

                    View view = adapter.getFloorplanView(getActivity(), device);
                    view.setVisibility(View.INVISIBLE);
                    view.setTag(device);
                    view.setOnLongClickListener(deviceOnLongClickListener);
                    layout.addView(view);

                    deviceViewMap.put(device, view);
                }
                floorplanView.manualTouch();

                Intent intent = new Intent(Actions.DISMISS_UPDATING_DIALOG);
                getActivity().sendBroadcast(intent);
            }
        });
        getActivity().startService(intent);
    }

    private void updateViewFor(Device device, float newScale) {
        if (! deviceViewMap.containsKey(device)) return;

        View view = deviceViewMap.get(device);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();

        Coordinate deviceCoordinate = device.getFloorplanPositionFor(floorplanName);
        Coordinate margin = floorplanToMargin(deviceCoordinate);

        view.setPivotX(0);
        view.setPivotY(0);

        view.setScaleX(newScale * DEFAULT_ITEM_ZOOM_FACTOR);
        view.setScaleY(newScale * DEFAULT_ITEM_ZOOM_FACTOR);
        params.setMargins((int) margin.x, (int) margin.y, 0, 0);

        view.setVisibility(View.VISIBLE);

        view.requestLayout();
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
        intent.putExtra(BundleExtraKeys.FLOORPLAN_IMAGE_RELATIVE_PATH, "/fp_" + floorplanName + ".png");
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS || !resultData.containsKey(BundleExtraKeys.FLOORPLAN_IMAGE))
                    return;

                floorplanView.setImageBitmap((Bitmap) resultData.getParcelable(BundleExtraKeys.FLOORPLAN_IMAGE));

                Intent intent = new Intent(Actions.DISMISS_UPDATING_DIALOG);
                getActivity().startService(intent);

                requestFloorplanDevices(false);
            }
        });
        getActivity().startService(intent);
    }

    private Coordinate floorplanToMargin(Coordinate deviceCoordinate) {
        return floorplanToMargin(deviceCoordinate, currentImageViewScale, currentImageViewCoordinate);
    }

    private static Coordinate floorplanToMargin(Coordinate deviceCoordinate, float currentImageViewScale, Coordinate currentImageViewCoordinate) {
        return deviceCoordinate.subtract(FLOORPLAN_OFFSET).scale(currentImageViewScale).add(currentImageViewCoordinate);
    }

    private Coordinate marginToFloorplan(Coordinate margin) {
        return marginToFloorplan(margin, currentImageViewScale, currentImageViewCoordinate);
    }

    private static Coordinate marginToFloorplan(Coordinate margin, float currentImageViewScale, Coordinate currentImageViewCoordinate) {
        return margin.subtract(currentImageViewCoordinate).scale(1 / currentImageViewScale).add(FLOORPLAN_OFFSET);
    }

    private class DeviceOnLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(final View view) {
            final Context context = getActivity();

            final Device device = (Device) view.getTag();

            final AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
            contextMenu.setTitle(device.getAliasOrName());
            contextMenu.setItems(R.array.floorplanDeviceDetail, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position) {
                    switch(position) {
                        case 0:
                            DeviceAdapter<?> adapter = DeviceType.getAdapterFor(device);
                            adapter.gotoDetailView(context, device);

                            Log.d(FloorplanFragment.class.getName(), "Details");
                            break;
                        case 1:
                            view.setBackgroundColor(getResources().getColor(R.color.focusedColor));
                            view.setOnTouchListener(deviceMoveTouchListener);

                            Log.d(FloorplanFragment.class.getName(), "Move " + device.getName());
                            break;
                        default:
                            Log.d(FloorplanFragment.class.getName(), "unknown " + position);
                    }
                    dialogInterface.dismiss();
                }
            });
            contextMenu.show();
            return true;
        }
    }

    private class DeviceMoveTouchListener implements View.OnTouchListener {
        private float lastX;
        private float lastY;
        private boolean moved = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Object tag = view.getTag();
            if (! (tag instanceof Device)) return false;

            Device<?> device = (Device<?>) tag;
            float x = motionEvent.getRawX();
            float y = motionEvent.getRawY();

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            int leftMargin = layoutParams.leftMargin;
            int topMargin = layoutParams.topMargin;

            int newLeftMargin = (int) (leftMargin + (x - lastX));
            int newTopMargin = (int) (topMargin + (y - lastY));

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d(FloorplanFragment.class.getName(), "down " + new Coordinate(x, y));
            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE && moved) {
                layoutParams.setMargins(newLeftMargin, newTopMargin, 0, 0);
                view.requestLayout();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                final Intent intent = new Intent(Actions.DEVICE_FLOORPLAN_MOVE);
                intent.putExtra(BundleExtraKeys.FLOORPLAN_NAME, floorplanName);
                intent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());

                Coordinate coordinate = marginToFloorplan(new Coordinate(newLeftMargin, newTopMargin));
                intent.putExtra(BundleExtraKeys.COORDINATE, coordinate);

                intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode != ResultCodes.SUCCESS) return;
                        update(false);
                    }
                });

                String text = getActivity().getResources().getString(R.string.floorplan_move_confirmation);
                text = String.format(text, device.getAliasOrName(), "x=" + coordinate.x + ", y=" + coordinate.y);
                DeviceActionUtil.showConfirmation(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().startService(intent);
                    }
                }, text);

                view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                Log.d(FloorplanFragment.class.getName(), "up " + new Coordinate(newLeftMargin, newTopMargin));

                moved = false;
            }
            lastX = x;
            lastY = y;

            moved = true;
            return true;
        }
    }
}
