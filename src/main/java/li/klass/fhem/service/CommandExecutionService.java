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

import com.google.common.base.Optional;
import com.google.common.io.CharStreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.constants.Actions;
import li.klass.fhem.exception.CommandExecutionException;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.fhem.FHEMConnection;
import li.klass.fhem.fhem.FHEMWEBConnection;
import li.klass.fhem.fhem.RequestResult;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.Cache;
import li.klass.fhem.util.CloseableUtil;

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
    DataConnectionSwitch dataConnectionSwitch;

    @Inject
    ApplicationProperties applicationProperties;

    private transient ScheduledExecutorService scheduledExecutorService = null;
    private transient String lastFailedCommand = null;
    private transient Cache<Bitmap> imageCache = getImageCache();

    @Inject
    public CommandExecutionService() {
    }

    public void resendLastFailedCommand(Context context) {
        if (lastFailedCommand != null) {
            String command = lastFailedCommand;
            lastFailedCommand = null;
            executeSafely(command, context);
        }
    }

    public String executeSafely(String command, Context context) {
        return executeSafely(command, 0, context);
    }

    public String executeSafely(String command, int delay, Context context) {
        command = command.replaceAll("  ", " ");
        LOG.info("executeSafely(command={}, delay={})", command, delay);
        if (delay == 0) {
            return executeImmediately(command, context);
        } else {
            executeDelayed(command, delay, context);
            return null;
        }
    }

    private void executeDelayed(String command, int delay, Context context) {
        schedule(delay, new ResendCommand(command, 0, context));
    }

    private String executeImmediately(String command, Context context) {
        showExecutingDialog(context);

        RequestResult<String> result = execute(command, context);
        if (result.handleErrors()) {
            lastFailedCommand = command;
            throw new CommandExecutionException();
        }
        return result.content;
    }

    private void showExecutingDialog(Context context) {
        context.sendBroadcast(new Intent(SHOW_EXECUTING_DIALOG));
    }

    /**
     * Execute a command without catching any exception or showing an update dialog. Executes synchronously.
     *
     * @param command command to execute
     * @param context context
     */
    private RequestResult<String> execute(String command, Context context) {
        return execute(command, 0, context);
    }

    private RequestResult<String> execute(String command, int currentTry, Context context) {
        FHEMConnection currentProvider = dataConnectionSwitch.getCurrentProvider(context);
        RequestResult<String> result = currentProvider.executeCommand(command, context);

        LOG.info("execute() - executing command={}, try={}", command, currentTry);

        try {
            if (result.error == null) {
                sendBroadcastWithAction(Actions.CONNECTION_ERROR_HIDE, context);
            } else if (shouldTryResend(command, result, currentTry, context)) {
                int timeoutForNextTry = secondsForTry(currentTry);

                ResendCommand resendCommand = new ResendCommand(command, currentTry + 1, context);
                schedule(timeoutForNextTry, resendCommand);
            }
        } finally {
            if (!command.equalsIgnoreCase("xmllist")) {
                hideExecutingDialog(context);
            }
        }

        return result;
    }

    public ScheduledFuture<?> schedule(int timeoutForNextTry, ResendCommand resendCommand) {
        LOG.info("schedule() - schedule {} in {} seconds", resendCommand, timeoutForNextTry);
        return getScheduledExecutorService().schedule(resendCommand,
                timeoutForNextTry, SECONDS);
    }

    private boolean shouldTryResend(String command, RequestResult<?> result, int currentTry, Context context) {
        if (!command.startsWith("set") && !command.startsWith("attr")) return false;
        if (result.error == null) return false;
        if (result.error != CONNECTION_TIMEOUT &&
                result.error != HOST_CONNECTION_ERROR) return false;
        if (currentTry > getNumberOfRetries(context)) return false;

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

    private void hideExecutingDialog(Context context) {
        context.sendBroadcast(new Intent(DISMISS_EXECUTING_DIALOG));
    }

    private int getNumberOfRetries(Context context) {
        return applicationProperties.getIntegerSharedPreference(
                COMMAND_EXECUTION_RETRIES, DEFAULT_NUMBER_OF_RETRIES,
                context);
    }

    public String getLastFailedCommand() {
        return lastFailedCommand;
    }

    public Bitmap getBitmap(String relativePath, Context context) {
        try {
            Cache<Bitmap> cache = getImageCache();
            if (cache.containsKey(relativePath)) {
                return cache.get(relativePath);
            } else {
                showExecutingDialog(context);

                FHEMConnection provider = dataConnectionSwitch.getCurrentProvider(context);
                RequestResult<Bitmap> result = provider.requestBitmap(relativePath);

                if (result.handleErrors()) return null;
                Bitmap bitmap = result.content;
                cache.put(relativePath, bitmap);
                return bitmap;
            }
        } finally {
            hideExecutingDialog(context);
        }
    }

    public Optional<String> executeRequest(String relativPath, Context context) {
        FHEMConnection provider = dataConnectionSwitch.getCurrentProvider(context);
        if (!(provider instanceof FHEMWEBConnection)) {
            return Optional.absent();
        }

        RequestResult<InputStream> result = ((FHEMWEBConnection) provider).executeRequest(relativPath);
        if (result.handleErrors()) {
            return Optional.absent();
        }
        try {
            return Optional.of(CharStreams.toString(new InputStreamReader(result.content)));
        } catch (IOException e) {
            LOG.error("executeRequest() - cannot read stream", e);
            return Optional.absent();
        } finally {
            CloseableUtil.close(result.content);
        }
    }

    private Cache<Bitmap> getImageCache() {
        if (imageCache == null) {
            imageCache = new Cache<>(IMAGE_CACHE_SIZE);
        }

        return imageCache;
    }

    private class ResendCommand implements Runnable {

        private final Context context;
        int currentTry;
        String command;

        protected ResendCommand(String command, int currentTry, Context context) {
            this.command = command;
            this.currentTry = currentTry;
            this.context = context;
        }

        @Override
        public void run() {
            execute(command, currentTry, context);
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
