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
import android.graphics.Bitmap;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.fhem.FHEMConnection;
import li.klass.fhem.fhem.RequestResult;

import static java.util.concurrent.TimeUnit.SECONDS;
import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG;
import static li.klass.fhem.fhem.RequestResultError.CONNECTION_TIMEOUT;
import static li.klass.fhem.fhem.RequestResultError.HOST_CONNECTION_ERROR;

/**
 * Service serving as central interface to FHEM.
 */
public class CommandExecutionService extends AbstractService {

    public static final CommandExecutionService INSTANCE = new CommandExecutionService();

    public static final int MAX_TRIES = 3;
    private transient ScheduledExecutorService scheduledExecutorService = null;

    private class ResendCommand implements Runnable {
        int currentTry;
        String command;

        protected ResendCommand(String command, int currentTry) {
            this.command = command;
            this.currentTry = currentTry;
        }

        @Override
        public void run() {
            execute(command, currentTry);
        }
    }

    private CommandExecutionService() {
    }

    /**
     * Executes a command safely by catching exceptions. Shows an update dialog during execution. Shows an update
     * dialog.
     *
     * @param command command to execute
     */
    public String executeSafely(String command) {
        command = command.replaceAll("  ", " ");
        Context context = showExecutingDialog();

        try {
            RequestResult<String> result = execute(command);
            if (result.handleErrors()) return null;
            return result.content;
        } finally {
            context.sendBroadcast(new Intent(DISMISS_EXECUTING_DIALOG));
        }
    }

    /**
     * Execute a command without catching any exception or showing an update dialog. Executes synchronously.
     *
     * @param command command to execute
     */
    private RequestResult<String> execute(String command) {
        return execute(command, 0);
    }

    private RequestResult<String> execute(String command, int currentTry) {
        FHEMConnection currentProvider = DataConnectionSwitch.INSTANCE.getCurrentProvider();
        RequestResult<String> result = currentProvider.executeCommand(command);

        if (result.error == null) {
            sendBroadcastWithAction(Actions.CONNECTION_ERROR_HIDE);
        } else if (shouldTryResend(command, result, currentTry)) {
            int timeoutForNextTry = secondsForTry(currentTry);
            Log.e(CommandExecutionService.class.getName(),
                    String.format("scheduling next resend of '%s' in %d seconds (try %d)",
                            command, timeoutForNextTry, currentTry));

            getScheduledExecutorService().schedule(new ResendCommand(command, currentTry + 1),
                    timeoutForNextTry, SECONDS);
        }

        return result;
    }

    private int secondsForTry(int executionTry) {
        return (int) Math.pow(3, executionTry);
    }

    private boolean shouldTryResend(String command, RequestResult<?> result, int currentTry) {
        if (! command.startsWith("set") && ! command.startsWith("attr")) return false;
        if (result.error == null) return false;
        if (result.error != CONNECTION_TIMEOUT &&
                result.error != HOST_CONNECTION_ERROR) return false;
        if (currentTry > MAX_TRIES) return false;

        return true;
    }

    public Bitmap getBitmap(String relativePath) {
        Context context = showExecutingDialog();

        try {
            FHEMConnection provider = DataConnectionSwitch.INSTANCE.getCurrentProvider();
            RequestResult<Bitmap> result = provider.requestBitmap(relativePath);

            if (result.handleErrors()) return null;
            return result.content;
        } finally {
            context.sendBroadcast(new Intent(DISMISS_EXECUTING_DIALOG));
        }
    }

    private Context showExecutingDialog() {
        Context context = AndFHEMApplication.getContext();
        context.sendBroadcast(new Intent(SHOW_EXECUTING_DIALOG));
        return context;
    }

    private ScheduledExecutorService getScheduledExecutorService() {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
        }
        return scheduledExecutorService;
    }
}
