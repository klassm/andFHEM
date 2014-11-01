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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.rooms.RoomListAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.fragments.core.TopLevelFragment;
import li.klass.fhem.service.advertisement.AdvertisementService;
import li.klass.fhem.service.intent.RoomListIntentService;
import li.klass.fhem.util.FhemResultReceiver;
import li.klass.fhem.util.Reject;

import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.constants.BundleExtraKeys.EMPTY_TEXT_ID;
import static li.klass.fhem.constants.BundleExtraKeys.ON_CLICKED_CALLBACK;
import static li.klass.fhem.constants.BundleExtraKeys.RESULT_RECEIVER;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_NAME;
import static li.klass.fhem.constants.BundleExtraKeys.ROOM_SELECTABLE_CALLBACK;

public class RoomListFragment extends BaseFragment implements TopLevelFragment {

    @Inject
    AdvertisementService advertisementService;
    private String roomName;
    private int emptyTextId = R.string.noRooms;
    private RoomSelectableCallback roomSelectableCallback;
    private RoomClickedCallback roomClickedCallback;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        roomName = args.getString(ROOM_NAME);
        emptyTextId = args.containsKey(EMPTY_TEXT_ID) ? args.getInt(EMPTY_TEXT_ID) : R.string.noRooms;
        roomSelectableCallback = (RoomSelectableCallback) args.getSerializable(ROOM_SELECTABLE_CALLBACK);
        roomClickedCallback = (RoomClickedCallback) args.getSerializable(ON_CLICKED_CALLBACK);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View superView = super.onCreateView(inflater, container, savedInstanceState);
        if (superView != null) return superView;

        RoomListAdapter adapter = new RoomListAdapter(getActivity(), R.layout.room_list_name, new ArrayList<String>());
        View layout = inflater.inflate(R.layout.room_list, container, false);
        advertisementService.addAd(layout, getActivity());

        assert layout != null;

        LinearLayout emptyView = (LinearLayout) layout.findViewById(R.id.emptyView);
        fillEmptyView(emptyView, getEmptyTextId(), container);

        ListView roomList = (ListView) layout.findViewById(R.id.roomList);
        Reject.ifNull(roomList);
        roomList.setAdapter(adapter);

        roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String roomName = String.valueOf(view.getTag());
                onClick(roomName);
            }
        });

        return layout;
    }

    protected int getEmptyTextId() {
        return emptyTextId;
    }

    protected void onClick(String roomName) {
        if (roomClickedCallback != null) {
            roomClickedCallback.onRoomClicked(roomName);
        } else {
            Intent intent = new Intent(Actions.SHOW_FRAGMENT);
            intent.putExtra(BundleExtraKeys.FRAGMENT, FragmentType.ROOM_DETAIL);
            intent.putExtra(ROOM_NAME, roomName);

            getActivity().sendBroadcast(intent);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(boolean doUpdate) {
        if (getView() == null) return;

        hideEmptyView();
        if (doUpdate) getActivity().sendBroadcast(new Intent(Actions.SHOW_EXECUTING_DIALOG));

        Intent intent = new Intent(Actions.GET_ROOM_NAME_LIST);
        intent.setClass(getActivity(), RoomListIntentService.class);
        intent.putExtras(new Bundle());
        intent.putExtra(BundleExtraKeys.DO_REFRESH, doUpdate);
        intent.putExtra(RESULT_RECEIVER, new FhemResultReceiver() {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (getView() == null) return;

                getActivity().sendBroadcast(new Intent(Actions.DISMISS_EXECUTING_DIALOG));

                if (resultCode == ResultCodes.SUCCESS) {
                    List<String> roomList = (ArrayList<String>) resultData.getSerializable(ROOM_LIST);
                    roomList = newArrayList(Iterables.filter(roomList, new Predicate<String>() {
                        @Override
                        public boolean apply(String input) {
                            return isRoomSelectable(roomName);
                        }
                    }));

                    assert roomList != null;
                    if (roomList.size() == 0) {
                        showEmptyView();
                        getAdapter().updateData(roomList);
                    } else {
                        getAdapter().updateData(roomList, roomName);
                        scrollToSelectedRoom(roomName, getAdapter().getData());
                    }
                }
            }
        });
        getActivity().startService(intent);
    }

    protected boolean isRoomSelectable(String roomName) {
        return roomSelectableCallback == null || roomSelectableCallback.isRoomSelectable(roomName);
    }

    private RoomListAdapter getAdapter() {
        if (getView() == null) return null;

        ListView listView = (ListView) getView().findViewById(R.id.roomList);
        return (RoomListAdapter) listView.getAdapter();
    }

    private void scrollToSelectedRoom(String selectedRoom, List<String> roomList) {
        if (selectedRoom == null) return;

        View view = getView();
        if (view == null) return;

        ListView roomListView = (ListView) view.findViewById(R.id.roomList);
        if (roomListView == null) return;

        for (int i = 0; i < roomList.size(); i++) {
            String roomName = roomList.get(i);
            if (roomName.equals(selectedRoom)) {
                roomListView.setSelection(i);
                return;
            }
        }
    }

    public interface RoomSelectableCallback extends Serializable {
        boolean isRoomSelectable(String roomName);
    }

    public interface RoomClickedCallback extends Serializable {
        void onRoomClicked(String roomName);
    }
}
