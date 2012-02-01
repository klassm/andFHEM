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
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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

package li.klass.fhem.fragments.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import li.klass.fhem.activities.core.FragmentBaseActivity;
import li.klass.fhem.activities.base.Updateable;
import li.klass.fhem.util.UIBroadcastReceiver;

public abstract class BaseFragment extends Fragment implements Updateable {

    private UIBroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(SupportActivity activity) {
        super.onAttach(activity);

        if (activity instanceof FragmentBaseActivity) {
            FragmentBaseActivity baseActivity = (FragmentBaseActivity) activity;
            broadcastReceiver = new UIBroadcastReceiver(baseActivity, this);
            broadcastReceiver.attach();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (broadcastReceiver != null) {
            broadcastReceiver.detach();
        }
    }
}
