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

package li.klass.fhem.ui.service.importExport;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.service.importexport.ImportExportService;
import li.klass.fhem.ui.FileDialog;
import li.klass.fhem.util.DialogUtil;

@Singleton
public class ImportExportUIService {
    private interface OnBackupPasswordSelected {
        void backupPasswordSelected(String password);
    }

    private static final Map<ImportExportService.ImportStatus, Integer> ERROR_TO_TEXT = ImmutableMap.<ImportExportService.ImportStatus, Integer>builder()
            .put(ImportExportService.ImportStatus.WRONG_PASSWORD, R.string.wrongPassword)
            .put(ImportExportService.ImportStatus.INVALID_FILE, R.string.importErrorInvalidFile).build();

    @Inject
    ImportExportService importExportService;

    public void handleImport(final Activity activity) {
        new FileDialog(activity, importExportService.getExportDirectory()).addFileListener(new FileDialog.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                onImportFileSelected(file, activity);
            }
        }).setFileFilter(new Predicate<File>() {
            @Override
            public boolean apply(File input) {
                return importExportService.isValidZipFile(input);
            }
        }).showDialog();
    }

    private void onImportFileSelected(final File file, final Activity activity) {
        if (importExportService.isEncryptedFile(file)) {
            selectPasswordWith(activity, new OnBackupPasswordSelected() {
                @Override
                public void backupPasswordSelected(String password) {
                    importWith(activity, file, password);
                }
            }, R.string.importPasswordDescription);
        } else {
            importWith(activity, file, null);
        }
    }

    private void importWith(Activity activity, File file, String password) {
        ImportExportService.ImportStatus status = importExportService.importSettings(file, password);
        if (status == ImportExportService.ImportStatus.SUCCESS) {
            onImportSuccess(activity);
        } else {
            onImportError(activity, status);
        }
    }

    private void onImportSuccess(final Activity activity) {
        @SuppressLint("InflateParams") View layout = activity.getLayoutInflater().inflate(R.layout.import_success, null);
        new AlertDialog.Builder(activity)
                .setView(layout).setCancelable(false)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                        activity.startActivity(activity.getIntent());
                        dialog.dismiss();
                    }
                }).show();
    }

    private void onImportError(final Activity activity, ImportExportService.ImportStatus status) {
        @SuppressLint("InflateParams") View layout = activity.getLayoutInflater().inflate(R.layout.import_error, null);
        ((TextView) layout.findViewById(R.id.text)).setText(ERROR_TO_TEXT.get(status));
        new AlertDialog.Builder(activity)
                .setView(layout).setCancelable(false)
                .setPositiveButton(R.string.okButton, DialogUtil.DISMISSING_LISTENER).show();
    }

    public void handleExport(final Activity activity) {

        selectPasswordWith(activity, new OnBackupPasswordSelected() {
            @Override
            public void backupPasswordSelected(String password) {
                File file = importExportService.exportSettings(password);

                @SuppressLint("InflateParams") View layout = activity.getLayoutInflater().inflate(R.layout.export_success, null);
                ((TextView) layout.findViewById(R.id.export_location)).setText(file.getAbsolutePath());
                new AlertDialog.Builder(activity)
                        .setView(layout).setCancelable(false)
                        .setPositiveButton(R.string.okButton, DialogUtil.DISMISSING_LISTENER).show();
            }
        }, R.string.exportPasswordDescription);
    }

    public void selectPasswordWith(Activity activity, final OnBackupPasswordSelected passwordSelectedListener, int description) {
        @SuppressLint("InflateParams") final View layout = activity.getLayoutInflater().inflate(R.layout.import_export_password_dialog, null);
        ((TextView) layout.findViewById(R.id.description)).setText(description);

        new AlertDialog.Builder(activity)
                .setView(layout).setCancelable(true)
                .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        EditText editText = (EditText) layout.findViewById(R.id.password);
                        passwordSelectedListener.backupPasswordSelected(Strings.emptyToNull(editText.getText().toString()));
                    }
                }).show();
    }
}
