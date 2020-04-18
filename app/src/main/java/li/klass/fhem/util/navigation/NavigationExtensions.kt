package li.klass.fhem.util.navigation

import androidx.annotation.IdRes
import androidx.navigation.NavController

fun NavController.updateStartFragment(@IdRes startDestId: Int) {
    graph = graph.apply {
        startDestination = startDestId
    }
}
