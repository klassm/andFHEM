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
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

import org.apache.commons.io.IOUtils;

import java.util.List;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.util.DialogUtil;

import static com.google.common.collect.Iterables.filter;
import static li.klass.fhem.util.StackTraceUtil.exceptionAsString;
import static li.klass.fhem.util.StackTraceUtil.whereAmI;

public class ErrorHolder {

    public static final Predicate<String> LOG_INCLUDE_PREDICATE = new Predicate<String>() {
        @Override
        public boolean apply(String input) {
            return !input.contains("D/li.klass") &&
                    ! input.contains("dalvikvm") &&
                    ! input.contains("W/Resources") &&
                    ! input.contains("requestLayout() improperly") &&
                    ! input.contains("V/li.klass");
        }
    };

    private enum TailHandling {
        FIRST, LAST
    }

    private static final String TAG = ErrorHolder.class.getName();

    private volatile transient Exception errorException;
    private volatile transient String errorMessage;

    public static ErrorHolder ERROR_HOLDER = new ErrorHolder();

    private ErrorHolder() {}

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
                        String text = ErrorHolder.getText();
                        if (text == null) {
                            DialogUtil.showAlertDialog(context, R.string.error_send, R.string.error_send_no_error);
                            return;
                        }

                        sendMail(context, "Send last error", "Error encountered!", text, TailHandling.FIRST);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public static void sendApplicationLogAsMail(Context context) {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            List<String> logLines = IOUtils.readLines(process.getInputStream());
            String log = Joiner.on("\r\n").join(filter(logLines, LOG_INCLUDE_PREDICATE));

            sendMail(context, context.getString(R.string.application_log_send), "Send app log",
                    log, TailHandling.LAST);
        }
        catch (Exception e) {
            Log.e(TAG, "Error while reading application log", e);
        }
    }

    private static void sendMail(Context context, String chooserText, String subject, String text,
                                 TailHandling tailHandling) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {AndFHEMApplication.ANDFHEM_MAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        // we have to limit the transaction size, see http://www.lonestarprod.com/?p=34
        // for details.
        if (text.length() > 20000) {
            if (tailHandling == TailHandling.FIRST) {
                text = text.substring(0, 20000);
            } else {
                text = text.substring(text.length() - 20000, text.length());
            }
        }
        intent.putExtra(Intent.EXTRA_TEXT, text);

        if (context == null) return;

        context.startActivity(Intent.createChooser(intent, chooserText));
    }
}
