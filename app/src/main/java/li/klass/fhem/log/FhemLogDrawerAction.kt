/*
 *  AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2011, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 *    Boston, MA  02110-1301  USA
 */

package li.klass.fhem.log

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import li.klass.fhem.R
import li.klass.fhem.activities.drawer.actions.AbstractDrawerAction
import li.klass.fhem.constants.Actions
import li.klass.fhem.util.DialogUtil
import li.klass.fhem.util.PermissionUtil
import java.io.File
import javax.inject.Inject

class FhemLogDrawerAction @Inject constructor(
        private val fhemLogService: FhemLogService
) : AbstractDrawerAction(R.id.fhem_log, returnHandle = false) {
    override fun execute(activity: AppCompatActivity) {
        if (!PermissionUtil.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            DialogUtil.showAlertDialog(activity, R.string.fhem_log, R.string.fhem_log_error_permission_external_storage)
            return
        }
        activity.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG))
        GlobalScope.launch(Dispatchers.Main) {
            val temporaryFile = async(Dispatchers.IO) {
                fhemLogService.getLogAndWriteToTemporaryFile()
            }.await()
            activity.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
            handle(activity, temporaryFile)
        }
    }

    private fun handle(activity: Activity, file: File?) {
        if (file != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.fromFile(file)
            intent.setDataAndType(uri, "text/plain")
            activity.startActivity(intent)
        } else {
            DialogUtil.showAlertDialog(activity, R.string.fhem_log, R.string.fhem_log_error)
        }
    }
}