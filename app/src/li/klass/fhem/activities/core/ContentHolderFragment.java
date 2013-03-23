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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import li.klass.fhem.R;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.fragments.core.BaseFragment;

import java.io.Serializable;
import java.lang.reflect.Constructor;

public class ContentHolderFragment extends Fragment implements Serializable {

    private final FragmentType fragmentType;
    private static final String TAG = ContentHolderFragment.class.getName();
    private transient Bundle data = null;
    private BaseFragment navigationFragment;
    private BaseFragment contentFragment;

    private transient View contentView;

    public ContentHolderFragment(FragmentType fragmentType, Bundle data) {
        this.fragmentType = fragmentType;
        this.data = data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (contentView != null) return contentView;

        return inflater.inflate(R.layout.content_view, null);
    }

    @Override
    public void onPause() {
        this.contentView = getView();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        fillView();
    }

    public void fillView() {
        if (contentFragment == null) {
            contentFragment = createContentFragment();
            navigationFragment = createNavigationFragment();
        }

        try {
            setContentFragment(contentFragment);
            setNavigationFragment(navigationFragment);

        } catch (IllegalStateException e) {
            Log.e(TAG, "error while switching to fragment " + fragmentType.getContentClass().getSimpleName(), e);
        }
    }

    private void setContentFragment(BaseFragment contentFragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, contentFragment)
                .commitAllowingStateLoss();
    }


    private void setNavigationFragment(BaseFragment navigationFragment) {
        try {
            View view = getView();
            if (view == null) return;

            View navigationView = view.findViewById(R.id.navigation);
            if (navigationView == null) return;

            if (navigationFragment == null) {
                navigationView.setVisibility(View.GONE);
                return;
            }
            navigationView.setVisibility(View.VISIBLE);


            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.navigation, navigationFragment)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e(TAG, "cannot instantiate navigation fragment", e);
        }
    }


    private BaseFragment createNavigationFragment() {
        View navigationView = getView().findViewById(R.id.navigation);
        if (navigationView == null) {
            return null;
        }

        try {
            Class<? extends BaseFragment> navigationClass = fragmentType.getNavigationClass();
            if (navigationClass == null) {
                navigationView.setVisibility(View.GONE);
                return null;
            }
            navigationView.setVisibility(View.VISIBLE);
            return createFragmentForClass(data, navigationClass);
        } catch (Exception e) {
            Log.e(TAG, "cannot instantiate fragment", e);
            return null;
        }
    }

    private BaseFragment createContentFragment() {
        try {
            Class<? extends BaseFragment> fragmentClass = fragmentType.getContentClass();
            return createFragmentForClass(data, fragmentClass);
        } catch (Exception e) {
            Log.e(TAG, "cannot instantiate fragment", e);
            return null;
        }
    }

    private BaseFragment createFragmentForClass(Bundle data, Class<? extends BaseFragment> fragmentClass) throws Exception {
        if (fragmentClass == null) return null;

        Constructor<? extends BaseFragment> constructor = fragmentClass.getConstructor(Bundle.class);
        return constructor.newInstance(data);
    }

    public void onContentChanged(Bundle newData) {
        data = newData;

        if (contentFragment != null) contentFragment.onContentChanged(newData);
        if (navigationFragment != null) navigationFragment.onContentChanged(newData);
    }

    public void onBackPressResult(Bundle data) {
        if (contentFragment != null) contentFragment.onBackPressResult(data);
        if (navigationFragment != null) navigationFragment.onBackPressResult(data);
    }

    public Bundle getCreationAttributesAsBundle() {
        return data;
    }

    public FragmentType getFragmentType() {
        return fragmentType;
    }
}
