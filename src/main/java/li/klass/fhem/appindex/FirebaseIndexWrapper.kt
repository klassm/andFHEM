package li.klass.fhem.appindex

import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.Indexable

class FirebaseIndexWrapper {
    fun update(indexables: Collection<Indexable>) {
        if (indexables.isNotEmpty()) {
            val index = FirebaseAppIndex.getInstance()
            index.removeAll()
            index.update(*(indexables.toTypedArray()))
        }
    }
}