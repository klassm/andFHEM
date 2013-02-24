/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import li.klass.fhem.R;

public class DialogUtil {
    public interface InputDialogListener {
        void onClick(String text);
    }

    public interface AlertOnClickListener {
        void onClick();
    }

    public static void showAlertDialog(Context context, int title, int text, final AlertOnClickListener onClickListener) {
        String titleText = title != -1 ? context.getString(title) : null;
        String contentText = context.getString(text);

        showAlertDialog(context, titleText, contentText, onClickListener);
    }

    public static void showAlertDialog(Context context, int title, int text) {
        showAlertDialog(context, context.getString(title), context.getString(text));
    }

    public static void showAlertDialog(Context context, int title, String text) {
        showAlertDialog(context, context.getString(title), text);
    }

    public static void showAlertDialog(Context context, String title, String text) {
        showAlertDialog(context, title, text, null);
    }

    public static void showAlertDialog(Context context, String title, String text, final AlertOnClickListener onClickListener) {
        final AlertDialog alert = new AlertDialog.Builder(context).create();
        if (title != null) alert.setTitle(title);
        alert.setCancelable(false);
        alert.setMessage(text);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.okButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alert.dismiss();
                if (onClickListener != null) onClickListener.onClick();
            }
        });
        alert.show();
    }

    public static void showContentDialog(Context context, String title, View content, final AlertOnClickListener onClickListener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null) builder.setTitle(title);
        builder.setCancelable(true);
        builder.setView(content);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.okButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
                if (onClickListener != null) onClickListener.onClick();
            }
        });
        alertDialog.show();
    }

    public static void showInputBox(Context context, String title, String defaultText, final InputDialogListener positiveOnClickListener) {
        final EditText input = new EditText(context);
        input.setText(defaultText);

        showConfirmBox(context, title, input, new AlertOnClickListener() {
            @Override
            public void onClick() {
                String text = input.getText().toString();
                positiveOnClickListener.onClick(text);
            }
        });
    }

    public static void showConfirmBox(Context context, int title, int text, AlertOnClickListener positiveOnClickListener) {
        String titleContent = context.getString(title);

        TextView view = new TextView(context);
        view.setPadding(5, 5, 5, 5);
        view.setText(text);

        showConfirmBox(context, titleContent, view, positiveOnClickListener);
    }

    private static void showConfirmBox(Context context, String title, View view, final AlertOnClickListener positiveOnClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        positiveOnClickListener.onClick();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }
}
