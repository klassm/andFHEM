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

package li.klass.fhem.error;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.util.CloseableUtil;
import li.klass.fhem.util.DialogUtil;

import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.util.StackTraceUtil.exceptionAsString;
import static li.klass.fhem.util.StackTraceUtil.whereAmI;

public class ErrorHolder {

    private static final String TAG = ErrorHolder.class.getName();

    private volatile transient Exception errorException;
    private volatile transient String errorMessage;

    public static ErrorHolder ERROR_HOLDER = new ErrorHolder();

    private ErrorHolder() {
    }

    public static void setError(Exception exception, String errorMessage) {
        ErrorHolder holder = ErrorHolder.ERROR_HOLDER;
        holder.errorException = exception;
        holder.errorMessage = errorMessage;
    }

    public static void setError(String errorMessage) {
        setError(null, errorMessage);
    }

    public static String getText() {
        ErrorHolder holder = ErrorHolder.ERROR_HOLDER;
        String text = holder.errorMessage;

        String exceptionString;
        if (holder.errorException != null) {
            exceptionString = exceptionAsString(holder.errorException);
        } else {
            exceptionString = whereAmI();
        }

        if (holder.errorException != null) {
            text += "\r\n --------- \r\n\r\n" + exceptionString;
        }
        return text;
    }

    public static void sendLastErrorAsMail(final Context context) {
        DialogUtil.showConfirmBox(context, R.string.error_send,
                R.string.error_send_content, new DialogUtil.AlertOnClickListener() {
                    @Override
                    public void onClick() {
                        handleSendLastError(context);
                    }
                });
    }

    private static void handleSendLastError(Context context) {
        if (!handleExternalStorageState(context)) return;
        try {
            String lastError = ErrorHolder.getText();
            if (lastError == null) {
                DialogUtil.showAlertDialog(context, R.string.error_send, R.string.error_send_no_error);
                return;
            }

            File attachment = writeToDisk(context, lastError);
            sendMail(context, "Send last error", "Error encountered!", deviceInformation(), Uri.fromFile(attachment));

        } catch (Exception e) {
            Log.e(TAG, "error while sending last error");
        }
    }

    @SuppressWarnings("unchecked")
    public static void sendApplicationLogAsMail(Context context) {
        if (!handleExternalStorageState(context)) return;
        try {
            File file = writeApplicationLogToDisk(context);

            sendMail(context, context.getString(R.string.application_log_send), "Send app log",
                    deviceInformation(), Uri.fromFile(file));
        } catch (Exception e) {
            Log.e(TAG, "Error while reading application log", e);
        }
    }

    private static void sendMail(Context context, String chooserText, String subject, String text,
                                 Uri attachment) throws IOException {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{AndFHEMApplication.ANDFHEM_MAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);

        intent.putExtra(android.content.Intent.EXTRA_STREAM, attachment);

        if (context == null) return;

        context.startActivity(Intent.createChooser(intent, chooserText));
    }

    private static File writeApplicationLogToDisk(Context context) throws IOException {
        InputStreamReader reader = null;
        try {
            File outputDir = context.getCacheDir();
            File outputFile = File.createTempFile("andFHEM-", "log", outputDir);
            outputFile.deleteOnExit();

            Process process = Runtime.getRuntime().exec("logcat -d");
            reader = new InputStreamReader(process.getInputStream());

            List<String> logLines = CharStreams.readLines(reader);
            String log = Joiner.on("\r\n").join(logLines);
            return writeToDisk(context, log);
        } finally {
            CloseableUtil.close(reader);
        }
    }

    private static File writeToDisk(Context context, String content) throws IOException {
        File outputDir = context.getExternalFilesDir(null);
        File outputFile = File.createTempFile("andFHEM-", ".log", outputDir);
        outputFile.setReadable(true, true);
        outputFile.deleteOnExit();

        Files.write(content, outputFile, Charsets.UTF_8);

        return outputFile;
    }

    private static String deviceInformation() {
        return Joiner.on("\r\n").join(newArrayList(
                "Device information:",
                "OS-Version: " + System.getProperty("os.version"),
                "API-Level: " + Build.VERSION.SDK_INT,
                "Device: " + android.os.Build.DEVICE,
                "Manufacturer: " + Build.MANUFACTURER,
                "Model: " + android.os.Build.MODEL,
                "Product: " + android.os.Build.PRODUCT
        ));
    }

    private static boolean handleExternalStorageState(Context context) {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            DialogUtil.showAlertDialog(context, R.string.error, R.string.errorExternalStorageNotPresent);
            return false;
        }
        return true;
    }
}
