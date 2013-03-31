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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.util.ViewUtil;

import java.io.Serializable;

public class TopLevelFragment extends Fragment implements Serializable {

    private transient FragmentType initialFragmentType;
    private int topLevelId;

    public TopLevelFragment() {
    }

    public TopLevelFragment(FragmentType initialFragmentType) {
        this.initialFragmentType = initialFragmentType;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.top_level_view, null);
        View topLevelContent = view.findViewById(R.id.topLevelContent);

        topLevelId = ViewUtil.getPseudoUniqueId(view, container);
        topLevelContent.setId(topLevelId);

        return view;
    }

    public void switchToInitialFragment() {
        if (getFragmentManager() == null) {
            AndFHEMApplication.getContext().sendBroadcast(new Intent(Actions.RELOAD));
            return;
        }
        int entryCount = getFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < entryCount; i++) {
            getFragmentManager().popBackStack();
        }

        switchTo(initialFragmentType, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchTo(initialFragmentType, null);
    }

    public void switchTo(FragmentType fragmentType, Bundle data) {

        ContentHolderFragment content = new ContentHolderFragment(fragmentType, data);

        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null) return;

        fragmentManager
                .beginTransaction()
                .replace(topLevelId, content)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public boolean back(Bundle data) {
        getFragmentManager().popBackStackImmediate();

        ContentHolderFragment contentFragment = getCurrentContent();
        if (contentFragment != null) {
            contentFragment.onBackPressResult(data);
            return true;
        }
        return false;
    }

    public ContentHolderFragment getCurrentContent() {
        if (getFragmentManager() == null) return null;
        return (ContentHolderFragment) getFragmentManager().findFragmentById(topLevelId);
    }

    public FragmentType getInitialFragmentType() {
        return initialFragmentType;
    }
}
