package li.klass.fhem.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import li.klass.fhem.R;
import li.klass.fhem.dataprovider.FHEMService;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.RoomDeviceList;

public abstract class UpdateableActivity extends Activity {
    protected static final int OPTION_UPDATE = 1;
    protected static final int DIALOG_UPDATE = 1;

    protected Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateData(roomDeviceList);
        }
    };

    protected RoomDeviceList roomDeviceList;

    @SuppressWarnings("unchecked")
    public void update(boolean refresh) {
        showDialog(DIALOG_UPDATE);
        new UpdateAction(refresh).execute();
    }

    protected void updateContent(boolean refresh) {
        roomDeviceList = getCurrentRoomDeviceList(refresh);
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


    public void onFS20Click(View view) {
        FS20Device device = (FS20Device) view.getTag();
        device.toggleState();
        update(false);
    }


    class UpdateAction extends AsyncTask<Void, Void, Void> {

        private boolean refresh;

        public UpdateAction(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                UpdateableActivity.this.updateContent(refresh);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            UpdateableActivity.this.dismissDialog(DIALOG_UPDATE);
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

    protected abstract RoomDeviceList getCurrentRoomDeviceList(boolean refresh);
    protected abstract void updateData(RoomDeviceList roomDeviceList);
}
