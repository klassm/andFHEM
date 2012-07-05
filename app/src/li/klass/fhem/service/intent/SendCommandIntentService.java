/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
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
 */

package li.klass.fhem.service.intent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.ResultReceiver;
import android.util.Log;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.service.CommandExecutionService;

import java.util.ArrayList;

public class SendCommandIntentService extends ConvenientIntentService  {
    private static final String PREFERENCES_NAME = "SendCommandStorage";
    private static final String CURRENT_STORAGE_POINTER_NAME = "currentPointer";
    private static final String STORAGE_PREFIX = "RECENT_COMMAND_";
    private static final int COMMAND_STORAGE_SIZE = 6;

    public SendCommandIntentService() {
        super(SendCommandIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        Log.d(SendCommandIntentService.class.getName(), intent.getAction());
        String action = intent.getAction();

        if (action.equals(Actions.EXECUTE_COMMAND)) {
            executeCommand(intent, resultReceiver);
        } else if (action.equals(Actions.RECENT_COMMAND_LIST))  {
            sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, BundleExtraKeys.RECENT_COMMANDS, getRecentCommands());
        }

        return STATE.SUCCESS;
    }

    private void executeCommand(Intent intent, ResultReceiver resultReceiver) {
        String command = intent.getStringExtra(BundleExtraKeys.COMMAND);
        String result = CommandExecutionService.INSTANCE.executeSafely(command);

        sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, BundleExtraKeys.COMMAND_RESULT, result);
        storeRecentCommand(command);
    }

    private ArrayList<String> getRecentCommands() {
        ArrayList<String> result = new ArrayList<String>();
        int currentStoragePointer = getCurrentStoragePointer();
        for (int i = 1; i <= COMMAND_STORAGE_SIZE; i++) {
            String commandKey = STORAGE_PREFIX + ((currentStoragePointer - i + COMMAND_STORAGE_SIZE) % COMMAND_STORAGE_SIZE);
            String command = getRecentCommandsPreferences().getString(commandKey, null);
            if (command != null) {
                result.add(command);
            }
        }
        return result;
    }

    private void storeRecentCommand(String command) {
        int currentStoragePointer = getCurrentStoragePointer();
        getRecentCommandsPreferences().edit().putString(STORAGE_PREFIX + currentStoragePointer, command).commit();
        incrementCurrentStoragePointer();
    }

    private int getCurrentStoragePointer() {
        return getRecentCommandsPreferences().getInt(CURRENT_STORAGE_POINTER_NAME, 0);
    }

    private void incrementCurrentStoragePointer() {
        int currentStoragePointer = getCurrentStoragePointer();
        getRecentCommandsPreferences().edit().putInt(CURRENT_STORAGE_POINTER_NAME, (currentStoragePointer + 1) % COMMAND_STORAGE_SIZE).commit();
    }

    private SharedPreferences getRecentCommandsPreferences() {
        return this.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
    }
}
