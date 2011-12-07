package li.klass.fhem.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import li.klass.fhem.R;

public class MainActivity extends TabActivity {
    public static MainActivity INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        INSTANCE = this;

        setContentView(R.layout.tablayout);

        Resources res = getResources();
        TabHost tabHost = getTabHost();

        Intent roomListIntent = new Intent().setClass(this, RoomListActivity.class);
        TabHost.TabSpec roomListTabSpec = tabHost.newTabSpec("roomList")
                .setIndicator(res.getString(R.string.tab_roomList), res.getDrawable(R.drawable.roomlist_tab))
                .setContent(roomListIntent);
        tabHost.addTab(roomListTabSpec);

        tabHost.setCurrentTab(0);

    }
}
