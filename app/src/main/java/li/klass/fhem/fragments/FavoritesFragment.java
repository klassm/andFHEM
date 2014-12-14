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

package li.klass.fhem.fragments;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.fragments.core.DeviceListFragment;
import li.klass.fhem.fragments.core.TopLevelFragment;
import li.klass.fhem.service.intent.FavoritesIntentService;
import li.klass.fhem.util.Reject;

public class FavoritesFragment extends DeviceListFragment implements TopLevelFragment {

    @Override
    protected String getUpdateAction() {
        return Actions.FAVORITE_ROOM_LIST;
    }

    @Override
    protected void fillEmptyView(LinearLayout view, ViewGroup viewGroup) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Reject.ifNull(inflater);
        view.addView(inflater.inflate(R.layout.favorites_empty_view, viewGroup, false));
    }

    @Override
    protected Class<?> getUpdateActionIntentTargetClass() {
        return FavoritesIntentService.class;
    }
}
