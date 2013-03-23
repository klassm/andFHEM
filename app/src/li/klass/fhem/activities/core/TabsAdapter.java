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
// * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
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
//package li.klass.fhem.activities.core;
//
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//import android.support.v4.view.ViewPager;
//import com.actionbarsherlock.app.ActionBar;
//import li.klass.fhem.fragments.FragmentType;
//
//public class TabsAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
//    public TabsAdapter(FragmentManager fm) {
//        super(fm);
//    }
//
//    @Override
//    public Fragment getItem(int i) {
//        return FragmentType.getTopLevelFragments().get(i);
//    }
//
//    @Override
//    public int getCount() {
//        return FragmentType.getTopLevelFragments().size();
//    }
//
//    @Override
//    public void onPageScrolled(int i, float v, int i2) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public void onPageSelected(int i) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public void onPageScrollStateChanged(int i) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//}
