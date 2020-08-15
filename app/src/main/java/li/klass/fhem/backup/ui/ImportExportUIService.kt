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
package li.klass.fhem.backup.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import li.klass.fhem.R
import li.klass.fhem.backup.ImportExportService
import li.klass.fhem.backup.ImportExportService.ImportStatus
import li.klass.fhem.util.DialogUtil.DISMISSING_LISTENER
import li.klass.fhem.util.PermissionUtil
import net.lingala.zip4j.ZipFile
import org.apache.commons.lang3.StringUtils
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportExportUIService @Inject constructor(private val importExportService: ImportExportService) {
    interface OnBackupPasswordSelected {
        fun backupPasswordSelected(password: String?)
    }

    fun handleImport(activity: Activity) {
        if (!PermissionUtil.checkPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return
        }
        val properties = DialogProperties()
        properties.selection_mode = DialogConfigs.SINGLE_MODE
        properties.selection_type = DialogConfigs.FILE_SELECT
        properties.extensions = arrayOf("backup")
        properties.root = importExportService.exportDirectory
        val dialog = FilePickerDialog(activity, properties)
        dialog.setTitle(R.string.selectFile)
        dialog.setDialogSelectionListener { files ->
            if (files.isNotEmpty()) {
                onImportFileSelected(File(files[0]), activity)
            }
        }
        dialog.show()
    }

    private fun onImportFileSelected(file: File, activity: Activity) {
        val zipFile = importExportService.toZipFile(file)
        if (zipFile == null) {
            AlertDialog.Builder(activity).setTitle(R.string.error)
                    .setMessage(R.string.errorNotAValidBackupFile)
                    .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()
            return
        }
        if (zipFile.isEncrypted) {
            selectPasswordWith(activity, object : OnBackupPasswordSelected {
                override fun backupPasswordSelected(password: String?) {
                    importWith(activity, zipFile, password, activity)
                }
            }, R.string.importPasswordDescription)
        } else {
            importWith(activity, zipFile, null, activity)
        }
    }

    private fun importWith(activity: Activity, file: ZipFile, password: String?, context: Context) {
        val status = importExportService.importSettings(file, password, context)
        if (status === ImportStatus.SUCCESS) {
            onImportSuccess(activity)
        } else {
            onImportError(activity, status)
        }
    }

    private fun onImportSuccess(activity: Activity) {
        @SuppressLint("InflateParams") val layout = activity.layoutInflater.inflate(R.layout.import_success, null)
        AlertDialog.Builder(activity)
                .setView(layout).setCancelable(false)
                .setPositiveButton(R.string.okButton) { dialog, _ ->
                    activity.finish()
                    activity.startActivity(activity.intent)
                    dialog.dismiss()
                }.show()
    }

    private fun onImportError(activity: Activity, status: ImportStatus) {
        @SuppressLint("InflateParams") val layout = activity.layoutInflater.inflate(R.layout.import_error, null)
        (layout.findViewById<View>(R.id.text) as TextView).setText(ERROR_TO_TEXT[status]
                ?: error("cannot set text"))
        AlertDialog.Builder(activity)
                .setView(layout).setCancelable(false)
                .setPositiveButton(R.string.okButton, DISMISSING_LISTENER).show()
    }

    fun handleExport(activity: Activity) {
        if (!PermissionUtil.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return
        }
        selectPasswordWith(activity, object : OnBackupPasswordSelected {
            override fun backupPasswordSelected(password: String?) {
                val file = importExportService.exportSettings(password, activity)
                @SuppressLint("InflateParams") val layout = activity.layoutInflater.inflate(R.layout.export_success, null)
                (layout.findViewById<View>(R.id.export_location) as TextView).text = file.absolutePath
                AlertDialog.Builder(activity)
                        .setView(layout).setCancelable(false)
                        .setPositiveButton(R.string.okButton, DISMISSING_LISTENER).show()
            }
        }, R.string.exportPasswordDescription)
    }

    private fun selectPasswordWith(activity: Activity, passwordSelectedListener: OnBackupPasswordSelected, description: Int) {
        @SuppressLint("InflateParams") val layout = activity.layoutInflater.inflate(R.layout.import_export_password_dialog, null)
        (layout.findViewById<View>(R.id.description) as TextView).setText(description)
        AlertDialog.Builder(activity)
                .setView(layout).setCancelable(true)
                .setPositiveButton(R.string.okButton) { dialog, _ ->
                    dialog.dismiss()
                    val editText = layout.findViewById<View>(R.id.password) as EditText
                    passwordSelectedListener.backupPasswordSelected(StringUtils.trimToNull(editText.text.toString()))
                }.show()
    }

    companion object {
        private val ERROR_TO_TEXT: Map<ImportStatus, Int> = mapOf(
                ImportStatus.WRONG_PASSWORD to R.string.wrongPassword,
                ImportStatus.INVALID_FILE to R.string.importErrorInvalidFile
        )
    }
}