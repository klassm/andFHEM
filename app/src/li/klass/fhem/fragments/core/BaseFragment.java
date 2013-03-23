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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import li.klass.fhem.activities.core.FragmentBaseActivity;
import li.klass.fhem.activities.core.Updateable;
import li.klass.fhem.util.BundleUtil;
import li.klass.fhem.util.UIBroadcastReceiver;

import java.io.Serializable;
import java.util.Map;

public abstract class BaseFragment extends Fragment implements Updateable, Serializable {

    private transient UIBroadcastReceiver broadcastReceiver;
    private transient View contentView;
    protected transient Bundle fragmentIntentResultData;
    protected Map<String, Serializable> creationAttributes;

    private transient Bundle originalCreationBundle;

    public BaseFragment() {
    }

    public BaseFragment(Bundle bundle) {
        this.originalCreationBundle = bundle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (creationAttributes == null) {
            onContentChanged(originalCreationBundle);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (contentView != null) {
            contentView.clearFocus();
        }
    }

    @Override
    public void onPause() {
        contentView = getView();
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
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

    public final void onBackPressResult(Bundle resultData) {
        this.fragmentIntentResultData = resultData;
    }

    public Bundle getCreationAttributesAsBundle() {
        return BundleUtil.mapToBundle(creationAttributes);
    }

    public final boolean onContentChanged(Bundle bundle) {
        Map<String, Serializable> oldAttributes = creationAttributes;
        Map<String, Serializable> newCreationAttributes = BundleUtil.bundleToMap(bundle);
        return onContentChanged(oldAttributes, newCreationAttributes);
    }

    protected boolean onContentChanged(Map<String, Serializable> oldCreationAttributes, Map<String, Serializable> newCreationAttributes) {
        creationAttributes = newCreationAttributes;
        if (oldCreationAttributes == null) {
            update(false);
            return true;
        }
        return false;
    }

    protected boolean doContentChangedAttributesMatch(Map<String, Serializable> oldCreationAttributes,
                                                      Map<String, Serializable> newCreationAttributes, String key) {
        if (oldCreationAttributes == null && newCreationAttributes == null) {
            return true;
        }

        if ((oldCreationAttributes == null) || (newCreationAttributes == null)) {
            return false;
        }

        Serializable oldValue = oldCreationAttributes.get(key);
        Serializable newValue = newCreationAttributes.get(key);

        if (oldValue == null && newValue == null) {
            return true;
        }

        if (oldValue == null || newValue == null) {
            return false;
        }

        if (oldValue.equals(newValue)) {
            return true;
        }
        return false;
    }

    protected boolean updateIfAttributesDoNotMatch(Map<String, Serializable> oldCreationAttributes,
                                                   Map<String, Serializable> newCreationAttributes, String key) {
        if (!doContentChangedAttributesMatch(oldCreationAttributes, newCreationAttributes, key)) {
            update(false);
            return true;
        }
        return false;
    }
}
