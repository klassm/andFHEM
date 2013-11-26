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

package li.klass.fhem.fragments.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Serializable;

import li.klass.fhem.R;
import li.klass.fhem.activities.core.Updateable;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.error.ErrorHolder;

import static li.klass.fhem.constants.Actions.CONNECTION_ERROR;
import static li.klass.fhem.constants.Actions.CONNECTION_ERROR_HIDE;
import static li.klass.fhem.constants.Actions.DEVICE_LIST_REMOTE_NOTIFY;
import static li.klass.fhem.constants.Actions.DO_UPDATE;
import static li.klass.fhem.constants.Actions.TOP_LEVEL_BACK;
import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;
import static li.klass.fhem.constants.BundleExtraKeys.STRING;
import static li.klass.fhem.constants.BundleExtraKeys.STRING_ID;

public abstract class BaseFragment extends Fragment implements Updateable, Serializable {

    private boolean isNavigation = false;

    public class UIBroadcastReceiver extends BroadcastReceiver {

        private final IntentFilter intentFilter;
        private FragmentActivity activity;
        private Updateable updateable;

        public UIBroadcastReceiver(FragmentActivity activity, Updateable updateable) {
            this.activity = activity;
            this.updateable = updateable;

            intentFilter = new IntentFilter();
            intentFilter.addAction(DO_UPDATE);
            intentFilter.addAction(TOP_LEVEL_BACK);
            intentFilter.addAction(CONNECTION_ERROR);
            intentFilter.addAction(CONNECTION_ERROR_HIDE);
            intentFilter.addAction(DEVICE_LIST_REMOTE_NOTIFY);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Log.d(UIBroadcastReceiver.class.getName(), "received action " + action);

                    if (action == null) return;

                    try {
                        if (action.equals(DO_UPDATE)) {
                            hideConnectionError();
                            boolean doUpdate = intent.getBooleanExtra(DO_REFRESH, false);
                            updateable.update(doUpdate);
                        } else if (action.equals(TOP_LEVEL_BACK)) {
                            if (!isVisible()) return;
                            if (!backPressCalled) {
                                backPressCalled = true;
                                onBackPressResult();
                            }
                        } else if (action.equals(DEVICE_LIST_REMOTE_NOTIFY)) {
                            update(false);
                        } else if (action.equals(CONNECTION_ERROR)) {
                            String content;
                            if (intent.hasExtra(STRING_ID)) {
                                content = context.getString(intent.getIntExtra(STRING_ID, -1));
                            } else {
                                content = intent.getStringExtra(STRING);
                            }
                            showConnectionError(content);
                        } else if (action.equals(CONNECTION_ERROR_HIDE)) {
                            hideConnectionError();
                        }
                    } catch (Exception e) {
                        Log.e(UIBroadcastReceiver.class.getName(), "error occurred", e);
                    }
                }
            });
        }

        public void attach() {
            activity.registerReceiver(this, intentFilter);
        }

        public void detach() {
            try {
                activity.unregisterReceiver(this);
            } catch (IllegalArgumentException e) {
                Log.e(UIBroadcastReceiver.class.getName(), "error while detaching", e);
            }
        }
    }

    public static final String CREATION_BUNDLE_KEY = "creationBundle";

    private transient UIBroadcastReceiver broadcastReceiver;
    private transient View contentView;
    private boolean backPressCalled = false;
    protected transient Bundle creationBundle;

    public BaseFragment() {
    }

    public BaseFragment(Bundle bundle) {
        this.creationBundle = bundle;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle("creationBundle", creationBundle);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(CREATION_BUNDLE_KEY)) {
            creationBundle = savedInstanceState.getBundle("creationBundle");
        }

        if (creationBundle == null) {
            creationBundle = new Bundle();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button retryButton = (Button) view.findViewById(R.id.retry);
        if (retryButton != null) {
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideConnectionError();
                    update(true);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {
            broadcastReceiver = new UIBroadcastReceiver(getActivity(), this);
        }
        broadcastReceiver.attach();

        if (contentView != null) {
            contentView.clearFocus();
        }
        backPressCalled = false;

        update(false);
    }

    @Override
    public void onPause() {
        contentView = getView();
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (broadcastReceiver != null) {
            broadcastReceiver.detach();
            broadcastReceiver = null;
        }
    }

    public void onBackPressResult() {
        update(false);
    }

    public boolean isNavigation() {
        return isNavigation;
    }

    public void setNavigation(boolean isNavigation) {
        this.isNavigation = isNavigation;
    }


    protected ProgressBar getUpdatingBar(View view) {
        return (ProgressBar) view.findViewById(R.id.updateProgress);
    }

    protected void hideEmptyView() {
        View emptyView = getView().findViewById(R.id.emptyView);
        if (emptyView == null) return;
        emptyView.setVisibility(View.GONE);
    }

    protected void showEmptyView() {
        if (isNavigation) return;

        View emptyView = getView().findViewById(R.id.emptyView);
        if (emptyView == null) return;
        emptyView.setVisibility(View.VISIBLE);
    }

    protected void hideUpdatingBar() {
        View view = getView();
        if (view == null) return;

        ProgressBar updatingBar = getUpdatingBar(view);
        if (updatingBar == null) return;

        updatingBar.setVisibility(View.GONE);

        Intent intent = new Intent(Actions.DISMISS_UPDATING_DIALOG);
        getActivity().sendBroadcast(intent);
    }

    protected void showUpdatingBar() {
        getUpdatingBar(getView()).setVisibility(View.VISIBLE);
    }

    protected void fillEmptyView(LinearLayout view) {}

    private void hideConnectionError() {
        if (isNavigation) return;

        View view = getView();
        if (view == null) return;

        View errorLayout = view.findViewById(R.id.errorLayout);
        if (errorLayout == null) return;
        errorLayout.setVisibility(View.GONE);
    }

    private void showConnectionError(String content) {
        if (isNavigation) return;

        View view = getView();
        if (view == null) return;

        View errorLayout = view.findViewById(R.id.errorLayout);
        if (errorLayout == null) return;

        errorLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                ErrorHolder.sendLastErrorAsMail(getActivity());

                return true;
            }
        });

        errorLayout.setVisibility(View.VISIBLE);

        TextView errorView = (TextView) view.findViewById(R.id.errorView);
        errorView.setText(content);
    }
}
