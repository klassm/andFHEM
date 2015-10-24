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

package li.klass.fhem.accesibility;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.service.intent.VoiceCommandIntentService;

public class MyAccessibilityService extends AccessibilityService {

    public static final Logger LOGGER = LoggerFactory.getLogger(MyAccessibilityService.class);

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        Optional<String> text;
        if (Build.VERSION.SDK_INT < 14) {
            text = getTextForOldAndroidVersions(accessibilityEvent);
        } else {
            text = getTextForNewAndroidVersions(accessibilityEvent);
        }

        if (!text.isPresent()) {
            return;
        }

        String command = text.get();
        LOGGER.info("command: {}", command);
        command = command.toLowerCase(Locale.getDefault());
        startService(new Intent(Actions.RECOGNIZE_VOICE_COMMAND)
                .setClass(this, VoiceCommandIntentService.class)
                .putExtra(BundleExtraKeys.COMMAND, command));
        Log.d(MyAccessibilityService.class.getName(), command);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Optional<String> getTextForNewAndroidVersions(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo source = accessibilityEvent.getSource();
        if (source == null || source.getText() == null) {
            return Optional.absent();
        }
        return Optional.of(source.getText().toString());
    }

    private Optional<String> getTextForOldAndroidVersions(AccessibilityEvent accessibilityEvent) {
        List<CharSequence> texts = accessibilityEvent.getText();
        if (texts.isEmpty()) return Optional.absent();

        return Optional.of(texts.get(0).toString());
    }

    @Override
    public void onInterrupt() {
    }
}
