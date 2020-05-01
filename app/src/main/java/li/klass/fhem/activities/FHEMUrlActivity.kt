//package li.klass.fhem.activities
//
//import android.app.Activity
//import android.content.Intent
//import android.os.Bundle
//import androidx.core.app.TaskStackBuilder
//import androidx.navigation.NavDeepLinkBuilder
//import li.klass.fhem.R
//import li.klass.fhem.activities.AndFHEMMainActivity
//import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragmentArgs
//import li.klass.fhem.constants.Actions
//import li.klass.fhem.constants.BundleExtraKeys
//import li.klass.fhem.ui.FragmentType
//
//class FHEMUrlActivity : Activity() {
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        val data = intent.data ?: return
////        if (data.host == null) return
////        val host = data.host
////        if (host!!.startsWith("room=")) {
////            show(FragmentType.ROOM_DETAIL, BundleExtraKeys.ROOM_NAME, host.replace("room=", ""))
////        } else if (host.startsWith("device=")) {
////            show(FragmentType.DEVICE_DETAIL, BundleExtraKeys.DEVICE_NAME, host.replace("device=", ""))
////        } else if (host.equals("all_devices", ignoreCase = true)) {
////            show(FragmentType.ALL_DEVICES)
////        } else if (host.equals("room_list", ignoreCase = true)) {
////            show(FragmentType.ROOM_LIST)
////        } else if (host.equals("favorites", ignoreCase = true)) {
////            show(FragmentType.FAVORITES)
////        } else {
////            val intent = Intent(Actions.EXECUTE_COMMAND)
////            intent.putExtra(BundleExtraKeys.COMMAND, host.replace("cmd=", ""))
////            startService(intent)
////        }
////        finish()
////    }
//
////    private fun show(fragmentType: FragmentType, extraKey: String? = null, extraValue: String? = null) {
////        TaskStackBuilder.create(this).run {
////            addNextIntentWithParentStack(Intent(this, AndFHEMMainActivity::class.java).apply {
////                putExtras(DestinationActivityArgs.Builder(jobId).build().toBundle())
////            })
////        NavDeepLinkBuilder(this)
////                .setComponentName(AndFHEMMainActivity::class.java)
////                .setGraph(R.navigation.nav_graph)
////                .setDestination(R.id.deviceDetailRedirectFragment)
////                .setArguments(null)
////
////        val intent = Intent(this, AndFHEMMainActivity::class.java)
////        intent.putExtra(BundleExtraKeys.FRAGMENT, fragmentType)
////        if (extraKey != null) {
////            intent.putExtra(extraKey, extraValue)
////        }
////        startActivity(intent)
////    }
//}