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

package li.klass.fhem.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import com.actionbarsherlock.view.MenuItem;
import li.klass.fhem.ApplicationUrls;
import li.klass.fhem.R;
import li.klass.fhem.activities.core.FragmentBaseActivity;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.fragments.ConversionFragment;
import li.klass.fhem.fragments.PremiumFragment;
import li.klass.fhem.fragments.SendCommandFragment;
import li.klass.fhem.fragments.TimerListFragment;
import li.klass.fhem.update.UpdateHandler;
import li.klass.fhem.util.DialogUtil;

import static li.klass.fhem.constants.BundleExtraKeys.DO_REFRESH;

public class AndFHEMMainActivity extends FragmentBaseActivity {

    public static final String TAG = AndFHEMMainActivity.class.getName();

    @Override
    protected void onResume() {
        super.onResume();
        UpdateHandler.INSTANCE.onUpdate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
        if (id == android.R.id.home) {
			onBackPressed();
		} else if (id == R.id.menu_refresh) {
			Intent refreshIntent = new Intent(Actions.DO_UPDATE);
			refreshIntent.putExtra(DO_REFRESH, true);
			sendBroadcast(refreshIntent);
			return true;
		} else if (id == R.id.menu_settings) {
			Intent settingsIntent = new Intent(this, PreferencesActivity.class);
			startActivityForResult(settingsIntent, RESULT_OK);
			return true;
		} else if (id == R.id.menu_help) {
			Uri helpUri = Uri.parse(ApplicationUrls.HELP_PAGE);
			Intent helpIntent = new Intent(Intent.ACTION_VIEW, helpUri);
			startActivity(helpIntent);
			return true;
		} else if (id == R.id.menu_premium) {
			Intent premiumIntent = new Intent(Actions.SHOW_FRAGMENT);
			premiumIntent.putExtra(BundleExtraKeys.FRAGMENT_NAME, PremiumFragment.class.getName());
			sendBroadcast(premiumIntent);
			return true;
		} else if (id == R.id.menu_command) {
			Intent commandIntent = new Intent(Actions.SHOW_FRAGMENT);
			commandIntent.putExtra(BundleExtraKeys.FRAGMENT_NAME, SendCommandFragment.class.getName());
			sendBroadcast(commandIntent);
			return true;
		} else if (id == R.id.menu_conversion) {
			Intent conversion = new Intent(Actions.SHOW_FRAGMENT);
			conversion.putExtra(BundleExtraKeys.FRAGMENT_NAME, ConversionFragment.class.getName());
			sendBroadcast(conversion);
			return true;
		} else if (id == R.id.menu_timer) {
			if (Build.VERSION.SDK_INT < 11) {
			    String text = String.format(getString(R.string.feature_requires_android_version), 3);
			    DialogUtil.showAlertDialog(this, R.string.android_version, text);
			    return true;
			}
			Intent timer = new Intent(Actions.SHOW_FRAGMENT);
			timer.putExtra(BundleExtraKeys.FRAGMENT_NAME, TimerListFragment.class.getName());
			sendBroadcast(timer);
			return true;
		} else if (id == R.id.menu_about) {
			String version;
			try {
			    String pkg = getPackageName();
			    version = getPackageManager().getPackageInfo(pkg, 0).versionName;
			} catch (PackageManager.NameNotFoundException e) {
			    version = "?";
			}
			DialogUtil.showAlertDialog(this, R.string.about, "Matthias Klass\r\nVersion: " + version + "\r\n" +
			        "andFHEM.klass.li\r\nandFHEM@klass.li");
			return true;
		}

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void update(boolean doUpdate) {
    }
}
