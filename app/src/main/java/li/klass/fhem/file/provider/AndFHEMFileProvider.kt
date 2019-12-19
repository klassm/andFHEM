package li.klass.fhem.file.provider

import androidx.core.content.FileProvider
import li.klass.fhem.BuildConfig

class AndFHEMFileProvider : FileProvider() {
    companion object {
        val AUTHORITY =
                BuildConfig.APPLICATION_ID + "." + AndFHEMFileProvider::class.java.simpleName
    }
}