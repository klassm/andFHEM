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

package li.klass.fhem.service.intent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.ResultReceiver;
import android.util.Log;

import java.util.ArrayList;

import javax.inject.Inject;

import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.StringUtil;

import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.constants.Actions.EXECUTE_COMMAND;
import static li.klass.fhem.constants.Actions.RECENT_COMMAND_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.RECENT_COMMANDS;

public class SendCommandIntentService extends ConvenientIntentService {
    private static final String PREFERENCES_NAME = "SendCommandStorage";
    private static final String CURRENT_STORAGE_POINTER_NAME = "currentPointer";
    private static final String STORAGE_PREFIX = "RECENT_COMMAND_";
    private static final int COMMAND_STORAGE_SIZE = 6;

    @Inject
    ConnectionService connectionService;

    @Inject
    CommandExecutionService commandExecutionService;

    public SendCommandIntentService() {
        super(SendCommandIntentService.class.getName());
    }

    @Override
    protected STATE handleIntent(Intent intent, long updatePeriod, ResultReceiver resultReceiver) {
        Log.d(SendCommandIntentService.class.getName(), intent.getAction());
        String action = intent.getAction();

        if (EXECUTE_COMMAND.equals(action)) {
            executeCommand(intent, resultReceiver);
        } else if (RECENT_COMMAND_LIST.equals(action)) {
            sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, RECENT_COMMANDS, getRecentCommands());
        }

        return STATE.SUCCESS;
    }

    private void executeCommand(Intent intent, ResultReceiver resultReceiver) {
        String command = intent.getStringExtra(BundleExtraKeys.COMMAND);
        String connectionId = intent.getStringExtra(BundleExtraKeys.CONNECTION_ID);

        if (!StringUtil.isBlank(connectionId) && connectionService.exists(connectionId)) {
            connectionService.setSelectedId(connectionId);
        }

        String result = commandExecutionService.executeSafely(command);

        sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, BundleExtraKeys.COMMAND_RESULT, result);
        storeRecentCommand(command);
    }

    private ArrayList<String> getRecentCommands() {
        ArrayList<String> result = newArrayList();
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
