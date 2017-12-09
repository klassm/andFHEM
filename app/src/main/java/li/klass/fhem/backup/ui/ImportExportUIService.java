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

package li.klass.fhem.backup.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.backup.ImportExportService;
import li.klass.fhem.util.DialogUtil;
import li.klass.fhem.util.PermissionUtil;

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

    @Inject
    public ImportExportUIService() {
    }

    public void handleImport(final Activity activity) {
        if (!PermissionUtil.checkPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return;
        }

        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = importExportService.getExportDirectory();

        FilePickerDialog dialog = new FilePickerDialog(activity, properties);
        dialog.setTitle(R.string.selectFile);
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files.length > 0) {
                    onImportFileSelected(new File(files[0]), activity);
                }
            }
        });
        dialog.show();
    }

    private void onImportFileSelected(final File file, final Activity activity) {
        if (importExportService.isEncryptedFile(file)) {
            selectPasswordWith(activity, new OnBackupPasswordSelected() {
                @Override
                public void backupPasswordSelected(String password) {
                    importWith(activity, file, password, activity);
                }
            }, R.string.importPasswordDescription);
        } else {
            importWith(activity, file, null, activity);
        }
    }

    private void importWith(Activity activity, File file, String password, Context context) {
        ImportExportService.ImportStatus status = importExportService.importSettings(file, password, context);
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

        if (!PermissionUtil.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return;
        }
        selectPasswordWith(activity, new OnBackupPasswordSelected() {
            @Override
            public void backupPasswordSelected(String password) {
                File file = importExportService.exportSettings(password, activity);

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
