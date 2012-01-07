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
import li.klass.fhem.fhem.DataConnectionSwitch;

/**
 * Service serving as central interface to FHEM.
 */
public class CommandExecutionService {

    public static final CommandExecutionService INSTANCE = new CommandExecutionService();

    private CommandExecutionService() {}

    /**
     * Execute a command without catching any exception or showing an update dialog. Executes synchronously.
     * @param command command to execute
     */
    public void executeUnsafeCommand(String command) {
        DataConnectionSwitch.INSTANCE.getCurrentProvider().executeCommand(command);
    }

    /**
     * Executes a command safely by catching exceptions. Shows an update dialog during execution. Shows an update
     * dialog.
     * @param context execution context
     * @param command command to execute
     * @param executeOnSuccess called if the action succeeds
     */
    public void executeSafely(Context context, String command, ExecuteOnSuccess executeOnSuccess) {
        new CommandExecuteTask(context, executeOnSuccess, command).executeTask();
    }

    /**
     * Executes a command safely by catching exceptions. Shows an update dialog during execution. Shows an update
     * dialog.
     * @param context execution context
     * @param command command to execute
     * @param executeOnSuccess called if the action succeeds
     * @param updateMessageId string id of the update dialog message
     */
    public void executeSafely(Context context, String command, ExecuteOnSuccess executeOnSuccess, int updateMessageId) {
        new CommandExecuteTask(context, executeOnSuccess, command, updateMessageId).executeTask();
    }

    /**
     * Executes a command asynchronously while showing an  update dialog.
     */
    class CommandExecuteTask extends UpdateDialogAsyncTask {

        private String command;
        private int updateMessageId = -1;

        public CommandExecuteTask(Context context, ExecuteOnSuccess executeOnSuccess, String command) {
            super(context, executeOnSuccess);
            this.command = command;
        }

        public CommandExecuteTask(Context context, ExecuteOnSuccess executeOnSuccess, String command, int updateMessageId) {
            this(context, executeOnSuccess, command);
            this.updateMessageId = updateMessageId;
        }

        @Override
        protected void executeCommand() {
            CommandExecutionService.this.executeUnsafeCommand(command);
        }

        @Override
        protected int getExecuteDialogMessage() {
            if (updateMessageId == -1) {
                return super.getExecuteDialogMessage();
            } else {
                return updateMessageId;
            }
        }
    }
}
