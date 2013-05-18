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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.FragmentType;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.util.ViewUtil;

import java.io.Serializable;
import java.lang.reflect.Constructor;

public class TopLevelFragment extends Fragment implements Serializable {

    public static final String INITIAL_FRAGMENT_TYPE_KEY = "initialFragmentType";
    public static final String LAST_SWITCH_TO_BUNDLE_KEY = "lastBundle";
    public static final String NAVIGATION_TAG = "NAVIGATION";
    public static final String CONTENT_TAG = "CONTENT";
    private transient FragmentType initialFragmentType;
    private int topLevelId;
    private BroadcastReceiver broadcastReceiver;
    public static final IntentFilter FILTER = new IntentFilter();

    static {
        FILTER.addAction(Actions.SWITCH_TO_INITIAL_FRAGMENT);
        FILTER.addAction(Actions.SHOW_FRAGMENT);
        FILTER.addAction(Actions.TOP_LEVEL_BACK);
    }

    private Bundle lastSwitchToBundle;
    private int contentId;
    private int navigationId;

    private static final String TAG = TopLevelFragment.class.getName();

    public TopLevelFragment() {
        setRetainInstance(true);
    }

    public TopLevelFragment(FragmentType initialFragmentType) {
        this();
        this.initialFragmentType = initialFragmentType;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(INITIAL_FRAGMENT_TYPE_KEY)) {
                initialFragmentType = FragmentType.valueOf(savedInstanceState.getString(INITIAL_FRAGMENT_TYPE_KEY));
            }

            if (savedInstanceState.containsKey(LAST_SWITCH_TO_BUNDLE_KEY)) {
                lastSwitchToBundle = savedInstanceState.getBundle(LAST_SWITCH_TO_BUNDLE_KEY);
            }
        }
        View view = inflater.inflate(R.layout.content_view, null);
        View navigationView = view.findViewById(R.id.navigation);
        View contentView = view.findViewById(R.id.content);

        contentId = ViewUtil.getPseudoUniqueId(view, container);
        contentView.setId(contentId);

        navigationId = ViewUtil.getPseudoUniqueId(view, container);
        if (navigationView != null)
            navigationView.setId(navigationId);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(INITIAL_FRAGMENT_TYPE_KEY, initialFragmentType.name());
        outState.putBundle(LAST_SWITCH_TO_BUNDLE_KEY, lastSwitchToBundle);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (lastSwitchToBundle != null) {
            switchTo(lastSwitchToBundle);
        } else {
            switchTo(initialFragmentType, null);
        }
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

    private void switchTo(Bundle bundle) {
        FragmentType fragmentType;
        if (bundle.containsKey(BundleExtraKeys.FRAGMENT)) {
            fragmentType = (FragmentType) bundle.getSerializable(BundleExtraKeys.FRAGMENT);
        } else {
            String fragmentName = bundle.getString(BundleExtraKeys.FRAGMENT_NAME);
            fragmentType = FragmentType.getFragmentFor(fragmentName);
        }

        lastSwitchToBundle = bundle;
        switchTo(fragmentType, bundle);
    }

    public void switchTo(FragmentType fragmentType, Bundle data) {

        BaseFragment contentFragment = createContentFragment(fragmentType, data);
        BaseFragment navigationFragment = createNavigationFragment(fragmentType, data);

        setContent(navigationFragment, contentFragment);
    }

    public boolean back(Bundle data) {

        if (getFragmentManager().getBackStackEntryCount() == 0) {
            getActivity().finish();
            return false;
        } else {
            String currentName = getLastTransactionName();
            boolean found = false;
            while (getFragmentManager().getBackStackEntryCount() != 0) {
                String lastTransactionName = getLastTransactionName();
                int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
                Log.e(TAG, backStackEntryCount + "");

                if (!currentName.equals(lastTransactionName)) {
                    found = true;
                    break;
                }
                getFragmentManager().popBackStackImmediate();
            }

            BaseFragment contentFragment = (BaseFragment) getFragmentManager().findFragmentByTag(CONTENT_TAG);
            if (!found) {
                getActivity().finish();
                return true;
            }

            BaseFragment navigationFragment = (BaseFragment) getFragmentManager().findFragmentByTag(NAVIGATION_TAG);
            updateNavigationVisibility(navigationFragment);
            if (navigationFragment != null) {
                navigationFragment.onBackPressResult(data);
            }

            if (contentFragment != null) {
                contentFragment.onBackPressResult(data);
            }

            return true;
        }
    }

    private String getLastTransactionName() {
        return getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1).getName();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (!isVisible()) return;

                    String action = intent.getAction();
                    if (action.equals(Actions.SWITCH_TO_INITIAL_FRAGMENT)) {
                        String fragmentName = intent.getStringExtra(BundleExtraKeys.FRAGMENT);
                        FragmentType fragment = FragmentType.valueOf(fragmentName);

                        if (fragment == initialFragmentType) {
                            switchToInitialFragment();
                        }
                    } else if (action.equals(Actions.SHOW_FRAGMENT)) {
                        if (!isVisible()) return;

                        Bundle bundle = intent.getExtras();
                        switchTo(bundle);


                    } else if (action.equals(Actions.TOP_LEVEL_BACK)) {
                        if (!isVisible()) return;

                        String fragmentName = intent.getStringExtra(BundleExtraKeys.FRAGMENT);
                        FragmentType fragment = FragmentType.valueOf(fragmentName);

                        if (fragment == initialFragmentType) {
                            back(intent.getExtras());
                        }
                    }
                }
            };

        }

        getActivity().registerReceiver(broadcastReceiver, FILTER);
    }


    private BaseFragment createNavigationFragment(FragmentType fragmentType, Bundle data) {
        View navigationView = getView().findViewById(navigationId);
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


    private void setContent(BaseFragment navigationFragment, BaseFragment contentFragment) {
        boolean hasNavigation = updateNavigationVisibility(navigationFragment);

        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction()
                .addToBackStack(contentFragment.getClass().getName());

        if (hasNavigation) {
            transaction
                    .replace(navigationId, navigationFragment, NAVIGATION_TAG)
                    .replace(contentId, contentFragment, CONTENT_TAG);
        } else {
            transaction
                    .replace(contentId, contentFragment);
        }

        transaction.commit();
    }

    private boolean updateNavigationVisibility(BaseFragment navigationFragment) {
        View view = getView();
        boolean hasNavigation = false;
        View navigationView = view.findViewById(navigationId);
        if (navigationView != null) {
            if (navigationFragment == null) {
                navigationView.setVisibility(View.GONE);
            } else {
                navigationView.setVisibility(View.VISIBLE);
                hasNavigation = true;
            }
        }
        return hasNavigation;
    }

    private BaseFragment createContentFragment(FragmentType fragmentType, Bundle data) {
        if (fragmentType == null) {
            getActivity().sendBroadcast(new Intent(Actions.RELOAD));
            return null;
        }
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

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unregisterReceiver(broadcastReceiver);
    }
}
