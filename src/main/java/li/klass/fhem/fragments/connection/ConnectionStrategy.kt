package li.klass.fhem.fragments.connection

import android.content.Context
import android.view.View
import li.klass.fhem.R
import li.klass.fhem.fhem.connection.FHEMServerSpec
import li.klass.fhem.service.connection.SaveData
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