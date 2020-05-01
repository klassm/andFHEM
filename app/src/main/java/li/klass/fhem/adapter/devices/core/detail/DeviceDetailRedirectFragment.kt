package li.klass.fhem.adapter.devices.core.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragmentDirections.Companion.actionDeviceDetailRedirectFragmentToDeviceDetailFragment
import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragmentDirections.Companion.actionDeviceDetailRedirectFragmentToFloorplanFragment
import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragmentDirections.Companion.actionDeviceDetailRedirectFragmentToWebViewFragment
import li.klass.fhem.update.backend.DeviceListService
import javax.inject.Inject

class DeviceDetailRedirectFragment @Inject constructor(
        private val deviceListService: DeviceListService
) : Fragment() {

    val args: DeviceDetailRedirectFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val device = deviceListService.getDeviceForName(args.deviceName, args.connectionId)
        device ?: throw RuntimeException("cannot find device for name $args")

        val action = when(device.xmlListDevice.type) {
            "weblink" -> {
                actionDeviceDetailRedirectFragmentToWebViewFragment(
                        device.xmlListDevice.getInternal("LINK")!!
                )
            }
            "FLOORPLAN" -> {
                actionDeviceDetailRedirectFragmentToFloorplanFragment(
                        args.deviceName
                )
            }
            else -> {
                actionDeviceDetailRedirectFragmentToDeviceDetailFragment(
                        deviceName = device.name,
                        connectionId = args.connectionId
                )
            }
        }

        val navController = findNavController()
        navController.navigate(action)
    }
}