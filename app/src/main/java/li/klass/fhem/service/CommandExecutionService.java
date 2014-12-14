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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.dagger.ForApplication;
import li.klass.fhem.exception.CommandExecutionException;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.fhem.FHEMConnection;
import li.klass.fhem.fhem.RequestResult;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.Cache;

import static java.util.concurrent.TimeUnit.SECONDS;
import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG;
import static li.klass.fhem.constants.PreferenceKeys.COMMAND_EXECUTION_RETRIES;
import static li.klass.fhem.fhem.RequestResultError.CONNECTION_TIMEOUT;
import static li.klass.fhem.fhem.RequestResultError.HOST_CONNECTION_ERROR;

@Singleton
public class CommandExecutionService extends AbstractService {

    public static final int DEFAULT_NUMBER_OF_RETRIES = 3;
    public static final int IMAGE_CACHE_SIZE = 20;

    private static final Logger LOG = LoggerFactory.getLogger(CommandExecutionService.class);

    @Inject
    @ForApplication
    Context applicationContext;

    @Inject
    DataConnectionSwitch dataConnectionSwitch;

    @Inject
    ApplicationProperties applicationProperties;

    private transient ScheduledExecutorService scheduledExecutorService = null;
    private transient String lastFailedCommand = null;
    private transient Cache<Bitmap> imageCache = getImageCache();

    public void resendLastFailedCommand() {
        if (lastFailedCommand != null) {
            String command = lastFailedCommand;
            lastFailedCommand = null;
            executeSafely(command);
        }
    }

    public String executeSafely(String command) {
        command = command.replaceAll("  ", " ");
        showExecutingDialog();

        RequestResult<String> result = execute(command);
        if (result.handleErrors()) {
            lastFailedCommand = command;
            throw new CommandExecutionException();
        }
        return result.content;
    }

    private void showExecutingDialog() {
        applicationContext.sendBroadcast(new Intent(SHOW_EXECUTING_DIALOG));
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
        FHEMConnection currentProvider = dataConnectionSwitch.getCurrentProvider();
        RequestResult<String> result = currentProvider.executeCommand(command);

        LOG.info("execute() - executing command={}, try={}", command, currentTry);

        try {
            if (result.error == null) {
                sendBroadcastWithAction(Actions.CONNECTION_ERROR_HIDE);
            } else if (shouldTryResend(command, result, currentTry)) {
                int timeoutForNextTry = secondsForTry(currentTry);

                ResendCommand resendCommand = new ResendCommand(command, currentTry + 1);
                schedule(timeoutForNextTry, resendCommand);
            }
        } finally {
            if (!command.equalsIgnoreCase("xmllist")) {
                hideExecutingDialog();
            }
        }

        return result;
    }

    private ScheduledFuture<?> schedule(int timeoutForNextTry, ResendCommand resendCommand) {
        LOG.info("schedule() - schedule {} in {} seconds", resendCommand, timeoutForNextTry);
        return getScheduledExecutorService().schedule(resendCommand,
                timeoutForNextTry, SECONDS);
    }

    private boolean shouldTryResend(String command, RequestResult<?> result, int currentTry) {
        if (!command.startsWith("set") && !command.startsWith("attr")) return false;
        if (result.error == null) return false;
        if (result.error != CONNECTION_TIMEOUT &&
                result.error != HOST_CONNECTION_ERROR) return false;
        if (currentTry > getNumberOfRetries()) return false;

        return true;
    }

    public static int secondsForTry(int executionTry) {
        return (int) Math.pow(3, executionTry);
    }

    private ScheduledExecutorService getScheduledExecutorService() {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
        }
        return scheduledExecutorService;
    }

    private void hideExecutingDialog() {
        applicationContext.sendBroadcast(new Intent(DISMISS_EXECUTING_DIALOG));
    }

    private int getNumberOfRetries() {
        return applicationProperties.getIntegerSharedPreference(
                COMMAND_EXECUTION_RETRIES, DEFAULT_NUMBER_OF_RETRIES
        );
    }

    public String getLastFailedCommand() {
        return lastFailedCommand;
    }

    public Bitmap getBitmap(String relativePath) {
        try {
            Cache<Bitmap> cache = getImageCache();
            if (cache.containsKey(relativePath)) {
                return cache.get(relativePath);
            } else {
                showExecutingDialog();

                FHEMConnection provider = dataConnectionSwitch.getCurrentProvider();
                RequestResult<Bitmap> result = provider.requestBitmap(relativePath);

                if (result.handleErrors()) return null;
                Bitmap bitmap = result.content;
                cache.put(relativePath, bitmap);
                return bitmap;
            }
        } finally {
            hideExecutingDialog();
        }
    }

    private Cache<Bitmap> getImageCache() {
        if (imageCache == null) {
            imageCache = new Cache<>(IMAGE_CACHE_SIZE);
        }

        return imageCache;
    }

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

        @Override
        public String toString() {
            return "ResendCommand{" +
                    "currentTry=" + currentTry +
                    ", command='" + command + '\'' +
                    '}';
        }
    }
}
