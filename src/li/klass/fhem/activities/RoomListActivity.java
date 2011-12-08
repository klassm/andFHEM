package li.klass.fhem.activities;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import li.klass.fhem.R;
import li.klass.fhem.adapter.RoomListAdapter;
import li.klass.fhem.dataprovider.FHEMService;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomListActivity extends ListActivity {

    protected static final int OPTION_UPDATE = 1;
    protected static final int DIALOG_UPDATE = 1;

    protected Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            adapter.updateData(roomList);
        }
    };


    private List<String> roomList = new ArrayList<String>();
    private RoomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        update(false);
        adapter = new RoomListAdapter(this, R.layout.room, roomList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String roomName = String.valueOf(v.getTag());
        Intent intent = new Intent();
        intent.setClass(this, RoomDetailActivity.class);
        intent.putExtras(new Bundle());
        intent.putExtra("roomName", roomName);

        startActivity(intent);
    }


    class UpdateAction extends AsyncTask<Void, Void, Void> {

        private boolean refresh;

        public UpdateAction(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                RoomListActivity.this.updateContent(refresh);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            RoomListActivity.this.dismissDialog(DIALOG_UPDATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        update(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FHEMService.INSTANCE.storeDeviceListMap();
    }


    @SuppressWarnings("unchecked")
    public void update(boolean refresh) {
        showDialog(DIALOG_UPDATE);
        new UpdateAction(refresh).execute();
    }

    protected void updateContent(boolean refresh) {
        roomList = FHEMService.INSTANCE.getRoomList(refresh);
        updateHandler.sendMessage(Message.obtain());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DIALOG_UPDATE:
                return ProgressDialog.show(this, "", getResources().getString(R.string.updating), false, true);
            default:
                return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, OPTION_UPDATE, 0, getResources().getString(R.string.update));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        int itemId = item.getItemId();

        switch (itemId) {
            case OPTION_UPDATE:
                update(true);
                break;
        }

        return true;
    }

}
