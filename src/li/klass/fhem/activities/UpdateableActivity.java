package li.klass.fhem.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Toast;
import li.klass.fhem.R;
import li.klass.fhem.dataprovider.FHEMService;
import li.klass.fhem.dataprovider.FavoritesService;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FS20Device;
import li.klass.fhem.domain.RoomDeviceList;

public abstract class UpdateableActivity extends Activity {
    protected static final int OPTION_UPDATE = 1;
    protected static final int DIALOG_UPDATE = 1;
    public static final int CONTEXT_MENU_FAVORITES_ADD = 1;
    public static final int CONTEXT_MENU_FAVORITES_DELETE = 2;

    protected Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateData(roomDeviceList);
        }
    };

    protected RoomDeviceList roomDeviceList;
    protected Device contextMenuClickedDevice;

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
    protected void onStop() {
        super.onStop();
        FHEMService.INSTANCE.storeDeviceListMap();
        FavoritesService.INSTANCE.storeFavorites();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Object tag = info.targetView.getTag();

        if (tag == null) return;
        if (tag instanceof Device) {
            contextMenuClickedDevice = (Device) tag;
            Resources resources = getResources();
            menu.add(0, CONTEXT_MENU_FAVORITES_ADD, 0, resources.getString(R.string.context_addtofavorites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case CONTEXT_MENU_FAVORITES_ADD:
                FavoritesService.INSTANCE.addFavorite(contextMenuClickedDevice);
                Toast.makeText(this, R.string.context_favoriteadded, Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    protected abstract RoomDeviceList getCurrentRoomDeviceList(boolean refresh);
    protected abstract void updateData(RoomDeviceList roomDeviceList);
}
