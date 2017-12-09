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

package li.klass.fhem.connection.ui

import android.content.Context
import android.view.View
import li.klass.fhem.R
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.connection.backend.SaveData
import li.klass.fhem.util.DialogUtil.showAlertDialog

abstract class ConnectionStrategy(val context: Context) {
    abstract fun saveDataFor(view: View): SaveData?

    abstract fun fillView(view: View, fhemServerSpec: FHEMServerSpec)

    fun enforceNotEmpty(fieldName: Int, value: String?): Boolean {
        if (value != null) {
            return true
        }

        showError(String.format(context.getString(R.string.connectionEmptyError), context.getString(fieldName)))
        return false
    }

    open fun showError(errorMessage: String) {
        showAlertDialog(context, R.string.error, errorMessage)
    }
}