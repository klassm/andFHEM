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

package li.klass.fhem.activities.core;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.util.Reject;

import static com.google.common.collect.Lists.newArrayList;

public class NavigationDrawerAdapter extends BaseAdapter {

    private final Context context;

    private class NavigationItem {
        FragmentType fragmentType;
        int iconResource;

        private NavigationItem(FragmentType fragmentType, int iconResource) {
            this.fragmentType = fragmentType;
            this.iconResource = iconResource;
        }
    }

    private List<NavigationItem> navigationItems = newArrayList();

    public NavigationDrawerAdapter(Context context) {
        this.context = context;

        navigationItems.add(new NavigationItem(FragmentType.ALL_DEVICES, R.drawable.all_devices));
        navigationItems.add(new NavigationItem(FragmentType.FAVORITES, R.drawable.favorites));
        navigationItems.add(new NavigationItem(FragmentType.ROOM_LIST, R.drawable.room_list));
        navigationItems.add(new NavigationItem(FragmentType.CONVERSION, R.drawable.conversion));
        navigationItems.add(new NavigationItem(FragmentType.TIMER_OVERVIEW, R.drawable.timer));
        navigationItems.add(new NavigationItem(FragmentType.SEND_COMMAND, R.drawable.send_command));
    }

    @Override
    public int getCount() {
        return navigationItems.size();
    }

    @Override
    public Object getItem(int i) {
        return navigationItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.navigation_drawer_item, viewGroup, false);
        }
        final NavigationItem item = navigationItems.get(i);

        TextView textView = (TextView) view.findViewById(R.id.item);
        Reject.ifNull(textView);

        textView.setText(item.fragmentType.getFragmentTitle());

        if (item.iconResource != -1) {
            Drawable drawable = context.getResources().getDrawable(item.iconResource);
            Reject.ifNull(drawable);

            drawable.setBounds(0, 0, 80, 80);
            textView.setCompoundDrawables(drawable, null, null, null);
        }
        view.setTag(item.fragmentType);

        return view;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // Workaround for a silly bug in Android 4
        // see http://code.google.com/p/android/issues/detail?id=22946 for details
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }
}
