package li.klass.fhem.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.adapter.RoomDetailAdapter;
import li.klass.fhem.dataprovider.FHEMService;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.RoomDeviceList;
import li.klass.fhem.widget.NestedListView;

public class RoomDetailActivity extends Activity {

    protected Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            roomDetailAdapter.updateData(deviceList);
        }
    };

    private String roomName;
    private RoomDetailAdapter roomDetailAdapter;

    private static final int OPTION_UPDATE = 1;
    private static final int DIALOG_UPDATE = 1;
    private RoomDeviceList deviceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.room_detail);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        roomName = extras.getString("roomName");

        String roomTitlePrefix = getResources().getString(R.string.roomTitlePrefix);
        setTitle(roomTitlePrefix + " " + roomName);

        roomDetailAdapter = new RoomDetailAdapter(this, new RoomDeviceList(roomName));
        NestedListView nestedListView = (NestedListView) findViewById(R.id.deviceMap);
        nestedListView.setAdapter(roomDetailAdapter);

        update(false);
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

    public void onFS20Click(View view) {
        FS20Device device = (FS20Device) view.getTag();
        device.toggleState();
        update(false);
    }


    @SuppressWarnings("unchecked")
    public void update(boolean refresh) {
        showDialog(DIALOG_UPDATE);
        new UpdateAction(refresh).execute();
    }

    private void updateContent(boolean refresh) {
        deviceList = FHEMService.INSTANCE.deviceListForRoom(roomName);
        updateHandler.sendMessage(Message.obtain());
    }

    class UpdateAction extends AsyncTask<Void, Void, Void> {

        private boolean refresh;

        public UpdateAction(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                RoomDetailActivity.this.updateContent(refresh);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            RoomDetailActivity.this.dismissDialog(DIALOG_UPDATE);
        }
    }
}
