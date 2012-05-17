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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.exception.AndFHEMException;
import li.klass.fhem.fhem.DataConnectionSwitch;

import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG;

/**
 * Service serving as central interface to FHEM.
 */
public class CommandExecutionService extends AbstractService {

    public static final CommandExecutionService INSTANCE = new CommandExecutionService();

    private CommandExecutionService() {}

    /**
     * Execute a command without catching any exception or showing an update dialog. Executes synchronously.
     * @param command command to execute
     */
    public String executeUnsafeCommand(String command) {
        return DataConnectionSwitch.INSTANCE.getCurrentProvider().executeCommand(command);
    }

    /**
     * Executes a command safely by catching exceptions. Shows an update dialog during execution. Shows an update
     * dialog.
     * @param command command to execute
     * 
     */
    public String executeSafely(String command) {
        Context context = AndFHEMApplication.getContext();
        context.sendBroadcast(new Intent(SHOW_EXECUTING_DIALOG));

        try {
            return executeUnsafeCommand(command);
        } catch (AndFHEMException e) {
            Bundle bundle = new Bundle();
            bundle.putInt(BundleExtraKeys.TOAST_STRING_ID, e.getErrorMessageStringId());
            sendBroadcastWithAction(Actions.SHOW_TOAST, bundle);
            
            Log.e(CommandExecutionService.class.getName(), "error occurred while executing command " + command, e);
            return "";
        } finally {
            context.sendBroadcast(new Intent(DISMISS_EXECUTING_DIALOG));
        }
    }
}
