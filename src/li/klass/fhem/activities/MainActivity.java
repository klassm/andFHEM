package li.klass.fhem.activities;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import li.klass.fhem.R;
import li.klass.fhem.util.ApplicationProperties;

public class MainActivity extends TabActivity {
    public static MainActivity INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        INSTANCE = this;

        setContentView(R.layout.tablayout);

        Resources res = getResources();
        TabHost tabHost = getTabHost();

        Intent favoritesIntent = new Intent().setClass(this, FavoritesActivity.class);
        TabHost.TabSpec favoritesTabSpec = tabHost.newTabSpec("favorites")
                .setIndicator(res.getString(R.string.tab_favorites), res.getDrawable(R.drawable.favorites_tab))
                .setContent(favoritesIntent);
        tabHost.addTab(favoritesTabSpec);

        Intent roomListIntent = new Intent().setClass(this, RoomListActivity.class);
        TabHost.TabSpec roomListTabSpec = tabHost.newTabSpec("roomList")
                .setIndicator(res.getString(R.string.tab_roomList), res.getDrawable(R.drawable.roomlist_tab))
                .setContent(roomListIntent);
        tabHost.addTab(roomListTabSpec);

        Intent allDevices = new Intent().setClass(this, AllDevicesActivity.class);
        TabHost.TabSpec allDevicesTabSpec = tabHost.newTabSpec("allDevices")
                .setIndicator(res.getString(R.string.tab_alldevices), res.getDrawable(R.drawable.alldevices_tab))
                .setContent(allDevices);
        tabHost.addTab(allDevicesTabSpec);

        tabHost.setCurrentTab(1);

        boolean isFirstStart = ApplicationProperties.INSTANCE.getProperty("FIRST_START", true);
        if (isFirstStart) {
            onFirstStart();
            ApplicationProperties.INSTANCE.setProperty("FIRST_START", false);
        }
    }

    private void onFirstStart() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(R.string.welcomeMessageTitle);
        alertDialog.setMessage(getResources().getString(R.string.welcomeMessage));
        alertDialog.setButton(getResources().getString(R.string.okButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.hide();
            }
        });
        alertDialog.show();
    }
}
