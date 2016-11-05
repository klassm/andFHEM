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
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.util.preferences.SharedPreferencesService;

import static com.google.common.base.Preconditions.checkArgument;
import static li.klass.fhem.constants.Actions.EXECUTE_COMMAND;
import static li.klass.fhem.constants.Actions.RECENT_COMMAND_DELETE;
import static li.klass.fhem.constants.Actions.RECENT_COMMAND_EDIT;
import static li.klass.fhem.constants.Actions.RECENT_COMMAND_LIST;
import static li.klass.fhem.constants.BundleExtraKeys.RECENT_COMMANDS;

public class SendCommandIntentService extends ConvenientIntentService {
    public static final String PREFERENCES_NAME = "SendCommandStorage";
    public static final String COMMANDS_JSON_PROPERTY = "commands";
    public static final String COMMANDS_PROPERTY = "RECENT_COMMANDS";
    public static final int MAX_NUMBER_OF_COMMANDS = 10;

    private static final Logger LOG = LoggerFactory.getLogger(SendCommandIntentService.class);

    @Inject
    ConnectionService connectionService;

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    SharedPreferencesService sharedPreferencesService;

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
        } else if (RECENT_COMMAND_DELETE.equals(action)) {
            deleteCommand(intent.getStringExtra(BundleExtraKeys.COMMAND));
        } else if (RECENT_COMMAND_EDIT.equals(action)) {
            editCommand(intent.getStringExtra(BundleExtraKeys.COMMAND), intent.getStringExtra(BundleExtraKeys.COMMAND_NEW_NAME));
        }

        return STATE.SUCCESS;
    }

    private void editCommand(String oldCommand, String newCommand) {
        ArrayList<String> commands = getRecentCommands();
        int index = commands.indexOf(oldCommand);
        checkArgument(index != -1);
        commands.add(index, newCommand);
        commands.remove(oldCommand);
        storeRecentCommands(commands);
    }

    private void deleteCommand(String command) {
        ArrayList<String> commands = getRecentCommands();
        commands.remove(command);
        storeRecentCommands(commands);
    }

    private void executeCommand(Intent intent, final ResultReceiver resultReceiver) {
        final String command = intent.getStringExtra(BundleExtraKeys.COMMAND);
        Optional<String> connectionId = Optional.fromNullable(intent.getStringExtra(BundleExtraKeys.CONNECTION_ID));
        commandExecutionService.executeSafely(command, connectionId, this, handleResult(resultReceiver, command));
    }

    @NonNull
    private CommandExecutionService.ResultListener handleResult(final ResultReceiver resultReceiver, final String command) {
        return new CommandExecutionService.SuccessfulResultListener() {
            @Override
            public void onResult(String result) {
                storeRecentCommand(command);
                sendSingleExtraResult(resultReceiver, ResultCodes.SUCCESS, BundleExtraKeys.COMMAND_RESULT, result);
            }
        };
    }

    ArrayList<String> getRecentCommands() {
        String recentCommandsValue = getRecentCommandsPreferences().getString(COMMANDS_PROPERTY, null);
        ArrayList<String> commandsResult = new ArrayList<>();
        if (recentCommandsValue == null) {
            return commandsResult;
        }
        try {
            JSONArray commandsJson = new JSONObject(recentCommandsValue).optJSONArray(COMMANDS_JSON_PROPERTY);
            if (commandsJson != null) {
                for (int i = 0; i < commandsJson.length(); i++) {
                    commandsResult.add(commandsJson.get(i).toString());
                }
            }
            return commandsResult;
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    private void storeRecentCommand(String command) {
        List<String> commands = getRecentCommands();
        if (commands.contains(command)) {
            commands.remove(command);
        }
        commands.add(0, command);

        storeRecentCommands(commands);
    }

    private void storeRecentCommands(List<String> commands) {
        try {
            if (commands.size() > MAX_NUMBER_OF_COMMANDS) {
                commands = commands.subList(0, MAX_NUMBER_OF_COMMANDS);
            }
            JSONObject json = new JSONObject();
            JSONArray commandsJsonArray = new JSONArray(commands);
            json.put(COMMANDS_JSON_PROPERTY, commandsJsonArray);

            getRecentCommandsPreferences().edit().putString(COMMANDS_PROPERTY, json.toString()).apply();
        } catch (JSONException e) {
            LOG.error("cannot store " + commands, e);
        }
    }

    private SharedPreferences getRecentCommandsPreferences() {
        return sharedPreferencesService.getPreferences(PREFERENCES_NAME, getBaseContext());
    }

    @Override
    protected void inject(ApplicationComponent applicationComponent) {
        applicationComponent.inject(this);
    }
}
