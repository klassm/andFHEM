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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import li.klass.fhem.R;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.DialogUtil;

import static com.google.common.base.Strings.isNullOrEmpty;
import static li.klass.fhem.constants.PreferenceKeys.STARTUP_PASSWORD;

public class StartupActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isNullOrEmpty(getPassword())) {
            gotoMainActivity();
        } else {
            setContentView(R.layout.startup);

            Button loginButton = (Button) findViewById(R.id.login);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText passwordInput = (EditText) findViewById(R.id.password);
                    String password = passwordInput.getText().toString();
                    if (password.equals(getPassword())) {
                        gotoMainActivity();
                    } else {
                        DialogUtil.showAlertDialog(StartupActivity.this, null,
                                getString(R.string.wrongPassword));
                    }
                    passwordInput.setText("");
                }
            });
        }
    }

    private void gotoMainActivity() {
        startActivity(new Intent(this, AndFHEMMainActivity.class));
    }

    private String getPassword() {
        return ApplicationProperties.INSTANCE.getStringSharedPreference(STARTUP_PASSWORD, "");
    }
}
