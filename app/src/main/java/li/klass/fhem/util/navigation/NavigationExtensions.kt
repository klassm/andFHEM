package li.klass.fhem.util.navigation

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import li.klass.fhem.R

fun NavController.updateStartFragment(@IdRes startDestId: Int) {
    graph = graph.apply {
        startDestination = startDestId
    }
}

fun AppCompatActivity.navController(): NavController? = (supportFragmentManager
        .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?)
        ?.navController