package li.klass.fhem.util.view

import android.widget.EditText

fun EditText.updateIfChanged(toSet: String) {
    if (text?.toString() != toSet) {
        setText(toSet)
    }
}