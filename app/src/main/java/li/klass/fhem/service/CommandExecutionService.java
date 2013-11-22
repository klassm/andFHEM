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

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.fhem.DataConnectionSwitch;
import li.klass.fhem.fhem.FHEMConnection;
import li.klass.fhem.fhem.RequestResult;

import static li.klass.fhem.constants.Actions.DISMISS_EXECUTING_DIALOG;
import static li.klass.fhem.constants.Actions.SHOW_EXECUTING_DIALOG;

/**
 * Service serving as central interface to FHEM.
 */
public class CommandExecutionService extends AbstractService {

    public static final CommandExecutionService INSTANCE = new CommandExecutionService();

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
        return DataConnectionSwitch.INSTANCE.getCurrentProvider().executeCommand(command);
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
}
