///*
// * AndFHEM - Open Source Android application to control a FHEM home automation
// * server.
// *
// * Copyright (c) 2011, Matthias Klass or third-party contributors as
// * indicated by the @author tags or express copyright attribution
// * statements applied by the authors.  All third-party contributions are
// * distributed under license by Red Hat Inc.
// *
// * This copyrighted material is made available to anyone wishing to use, modify,
// * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
// * for more details.
// *
// * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
// * along with this distribution; if not, write to:
// *   Free Software Foundation, Inc.
// *   51 Franklin Street, Fifth Floor
// *   Boston, MA  02110-1301  USA
// */
//
//package li.klass.fhem.activities.base;
//
//import android.support.v4.app.FragmentActivity;
//
//public class ActionBarActivity extends FragmentActivity {
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////
////        ActionBar actionBar = getSupportActionBar();
////        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
////
////        ActionBar.Tab favoritesTab = actionBar.newTab();
////        favoritesTab.setTag(FavoritesActivity.class);
////        favoritesTab.setText(R.string.tab_favorites);
////        actionBar.addTab(favoritesTab);
////
////        ActionBar.Tab roomsTab = actionBar.newTab();
////        roomsTab.setTag(RoomListActivity.class);
////        roomsTab.setText(R.string.tab_roomList);
////        actionBar.addTab(roomsTab);
////
////        ActionBar.Tab allDevicesTab = actionBar.newTab();
////        allDevicesTab.setTag(AllDevicesActivity.class);
////        allDevicesTab.setText(R.string.tab_alldevices);
////        allDevicesTab.set
////        actionBar.addTab(allDevicesTab);
////
////        allDevicesTab.setTabListener(this);
////        roomsTab.setTabListener(this);
////        favoritesTab.setTabListener(this);
////
////        Intent intent = getIntent();
////        String tabTarget = intent.getStringExtra(BundleExtraKeys.TAB_TARGET);
////        Log.e(ActionBarActivity.class.getName(), "tab target: " +  tabTarget);
////        if (tabTarget != null && tabTarget.equals(AllDevicesActivity.class.getName())) {
////            actionBar.setSelectedNavigationItem(1);
////        } else if (tabTarget != null && tabTarget.equals(RoomListActivity.class.getName())) {
////            actionBar.setSelectedNavigationItem(2);
////        } else {
////            actionBar.setSelectedNavigationItem(0);
////        }
////    }
////
////    @Override
////    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
////    }
////
////    @Override
////    @SuppressWarnings("unchecked")
////    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
////        Class<? extends Activity> targetClass = (Class<? extends Activity>) tab.getTag();
////        Intent intent = new Intent(this, targetClass);
////        intent.putExtra(BundleExtraKeys.TAB_TARGET, targetClass.getName());
////        startActivity(intent);
////    }
////
////    @Override
////    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
////    }
//}
