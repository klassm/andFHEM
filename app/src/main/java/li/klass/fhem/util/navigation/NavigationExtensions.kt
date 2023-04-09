package li.klass.fhem.util.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import li.klass.fhem.R

fun AppCompatActivity.navController(): NavController = (supportFragmentManager
    .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
    .navController

fun Fragment.loadFragmentInto(id: Int, fragment: Fragment) {
    childFragmentManager.beginTransaction()
            .apply {
                replace(id, fragment)
                disallowAddToBackStack()
                commitAllowingStateLoss()
            }
}
