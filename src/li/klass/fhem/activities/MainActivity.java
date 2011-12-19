package li.klass.fhem.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import li.klass.fhem.R;
import li.klass.fhem.activities.devicelist.AllDevicesActivity;
import li.klass.fhem.activities.devicelist.FavoritesActivity;
import li.klass.fhem.util.ApplicationProperties;

public class MainActivity extends TabActivity {
    public static MainActivity INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        INSTANCE = this;

        setContentView(R.layout.tablayout);

        addTab(FavoritesActivity.class, "favorites", R.string.tab_favorites, R.drawable.favorites_tab);
        addTab(RoomListActivity.class, "roomList", R.string.tab_roomList, R.drawable.roomlist_tab);
        addTab(AllDevicesActivity.class, "allDevices", R.string.tab_alldevices, R.drawable.alldevices_tab);

        getTabHost().setCurrentTab(0);

        boolean isFirstStart = ApplicationProperties.INSTANCE.getProperty("FIRST_START", true);
        if (isFirstStart) {
            onFirstStart();
        }
    }
    
    private void addTab(Class<? extends Activity> tabActivityClass, String tabTag, int tabCaption, int tabDrawable) {
        Resources resources = getResources();
        TabHost tabHost = getTabHost();

        Intent intent = new Intent().setClass(this, tabActivityClass);
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tabTag)
                .setIndicator(resources.getString(tabCaption), resources.getDrawable(tabDrawable))
                .setContent(intent);

        tabHost.addTab(tabSpec);
    }

    private void onFirstStart() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(R.string.welcomeMessageTitle);
        alertDialog.setMessage(getResources().getString(R.string.welcomeMessage));
        alertDialog.setButton(getResources().getString(R.string.okButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ApplicationProperties.INSTANCE.setProperty("FIRST_START", false);
                alertDialog.hide();
            }
        });
        alertDialog.show();
    }
}
