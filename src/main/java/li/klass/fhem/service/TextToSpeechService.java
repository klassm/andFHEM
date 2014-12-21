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

package li.klass.fhem.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;

import static android.speech.tts.TextToSpeech.LANG_MISSING_DATA;
import static android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED;
import static android.speech.tts.TextToSpeech.OnInitListener;
import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;
import static android.speech.tts.TextToSpeech.SUCCESS;

public class TextToSpeechService extends Service implements OnInitListener {

    private TextToSpeech textToSpeech;
    private static final Logger LOG = LoggerFactory.getLogger(TextToSpeechService.class);
    private boolean initDone = false;
    private String queuedVoiceCommand = null;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setSpeechRate(1);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (Actions.SAY.equals(action)) {
            say(intent.getStringExtra(BundleExtraKeys.TEXT));
            return ResultCodes.SUCCESS;
        } else {
            return super.onStartCommand(intent, flags, startId);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == SUCCESS) {
            int result = textToSpeech.setLanguage(getResources().getConfiguration().locale);
            if (result == LANG_MISSING_DATA ||
                    result == LANG_NOT_SUPPORTED) {
                LOG.debug("Language is not available.");
            } else {
                initDone = true;
                if (queuedVoiceCommand != null) {
                    say(queuedVoiceCommand);
                    queuedVoiceCommand = null;
                }
            }
        } else {
            LOG.debug("Could not initialize TextToSpeech.");
        }
    }

    private void say(String str) {
        if (!initDone) {
            queuedVoiceCommand = str;
        } else {
            textToSpeech.speak(str, QUEUE_FLUSH, null);
        }
    }
}